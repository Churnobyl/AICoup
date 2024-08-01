from PIL import Image
import numpy as np
import io
import asyncio

# import time

import zipfile
import cv2

from core import *

''' JSON 포멧
[
    {
        "left_card": 4,
        "right_card": 3,
        "extra_card": []
    },
    {
        "left_card": 1,
        "right_card": 2,
        "extra_card": []
    },
    {
        'left_card': 5,
        'right_card': 4,
        'extra_card': []
    },
    {
        'left_card': 1,
        'right_card': 2,
        'extra_card': []
    }
]
'''

# -----------------------------------------------------------
# 객체 탐지 결과 txt 반환
# -----------------------------------------------------------
### 데이터 포멧 1차 변환
def convert_to_dict(all_files_data):
    results = [] 

    # 이미지 3장의 객체 탐지 결과 순차적으로 탐색
    count = 0
    for file_data in all_files_data:
        count += 1
        file_results = []
        for line in file_data:
            class_id, x, y, w, h, conf = map(float, line)
            file_results.append({
                "class_id": int(class_id),
                "x": x,
                "y": y,
                "width": w,
                "height": h,
                "confidence": conf
            })
        
        results.append({
            "image": count,
            "detections": file_results
        })
    
    return results

# -----------------------------------------------------------
### 로그 큐 변환
def convert_queue():
    print("로그 큐 변환 시작")
    
    count = len(DET_LOGS)
    if count == 0:
        print("로그 없음")
        return "이전 객체 탐지 내역 없음"
    
    results = []
    seq = ""
    for log in DET_LOGS:  
        if log:
            res = convert_to_dict(log)
        else:
            res = []
        
        count -= 1
        match count:
            case 0:
                seq = "최신"
            case 1:
                seq = "이전"
            case 2:
                seq = "가장 오래 된"
        
        results.append({
            "time": f"{seq} 탐지 결과",
            "log": res
        })

    print("로그 큐 변환 종료")
    return results    

# -----------------------------------------------------------
### 촬영 이미지 메모리 임시 저장
def add_image(frame):
    try:
        # numpy 배열을 PIL 이미지로 변환
        if isinstance(frame, np.ndarray):
            # cv2의 BGR 배열을, RGB로 변환
            frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            frame = Image.fromarray(frame)
            
        # 이미지를 메모리 버퍼에 저장
        buffer = io.BytesIO()
        frame.save(buffer, format='JPEG')
        buffer.seek(0)
        CAP_IMG_BUFFERS.append(buffer)
        print("촬영 이미지 임시 저장 성공")
    
    except Exception as e:
        # 예외 발생 시 raise로 예외를 호출자에게 전달
        raise RuntimeError(f"촬영 이미지 임시 저장 실패: {str(e)}")

# -----------------------------------------------------------
### 이미지 스트림 변환
async def stream_images_from_buffers(save_buffers):

    for i, buffer in enumerate(save_buffers):
        buffer.seek(0)  # 버퍼의 시작으로 이동
        img = buffer.read()

        if img:
            yield (
                b"--frame\r\n"
                b"Content-Type: image/jpeg\r\n\r\n" + img + b"\r\n"
            )
        print(f"{i+1} 이미지 스트리밍 전송")

        await asyncio.sleep(1)  # 각 이미지 사이에 짧은 지연 추가
        
# ### 이미지 스트림 변환 동기방식
# def stream_images_from_buffers(save_buffers):

#     for i, buffer in enumerate(save_buffers):
#         buffer.seek(0)  # 버퍼의 시작으로 이동
#         img = buffer.read()

#         if img:
#             yield (
#                 b"--frame\r\n"
#                 b"Content-Type: image/jpeg\r\n\r\n" + img + b"\r\n"
#             )
#         print(f"{i+1} 이미지 스트리밍 전송")

#         time.sleep(1)  # 각 이미지 사이에 짧은 지연 추가

# -----------------------------------------------------------
### 이미지 압축

def zip_images_from_buffers(save_buffers) -> io.BytesIO:

    s = io.BytesIO()
    zip_file = zipfile.ZipFile(s, "w")

    for i, buffer in enumerate(save_buffers):
        buffer.seek(0)  # 버퍼의 시작으로 이동

        # 버퍼 데이터를 ZIP 파일에 추가
        img_name = f"photo_{i+1}.jpg"
        zip_file.writestr(img_name, buffer.read())
        print(f"{img_name} 압축 완료")

    zip_file.close()
    s.seek(0)

    return s