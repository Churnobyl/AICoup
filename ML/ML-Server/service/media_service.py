import os
import time
import cv2
import asyncio
import zipfile
import io

from core import *
from utils_main import *

### 이미지 파일은 .jpg 고정

### 몇 메서드는 utils 폴더로 이동

# -----------------------------------------------------------
### 이미지 촬영
async def capture_images():
    print("capture_images() 시작")

    # 기본 웹캠에 연결
    cap = cv2.VideoCapture(0)

    if not cap.isOpened():
        print("웹캠 연결 끊김")
        return {"error": "Could not open webcam."}

    # 3장 촬영
    photo_count = 3
    for i in range(photo_count):
        ret, frame = cap.read()
        if not ret:
            print("촬영 실패")
            return {"error": "Failed to capture image."}

        # 촬영 사진을 지정된 경로 폴더에 저장
        print("촬영 성공")
        img_path = os.path.join(IMG_FOLDER, f'photo_{i+1}.jpg')
        cv2.imwrite(img_path, frame)
        time.sleep(0.5)  # Wait for a second before capturing the next image

    # Release the webcam
    cap.release()
    print("capture_images() 종료")
    return {"message": "Photos captured successfully."}

# -----------------------------------------------------------

## utils 디렉토리로 이동

# ### 이미지 파일 불러오기
# async def get_image_data(image_path):
#     try:
#         with open(image_path, "rb") as image_file:
#             print("이미지 파일 읽기")
#             return image_file.read()
        
#     except FileNotFoundError:
#         print(f"파일을 찾을 수 없습니다: {image_path}")
#         return None

# -----------------------------------------------------------
### 이미지 파일 스트리밍 만들기
async def create_image_stream(folder_path):
    print("create_image_stream() 시작")
    
    for img in IMG_FILES:
        image_path = folder_path / img
        image_data = await get_image_data(image_path)

        if image_data:
            yield (
                b"--frame\r\n"
                b"Content-Type: image/jpeg\r\n\r\n" + image_data + b"\r\n"
            )
        print("스트리밍 전송")

        await asyncio.sleep(1)  # 각 이미지 사이에 짧은 지연 추가

    print("create_image_stream() 종료")
    
# -----------------------------------------------------------
### 이미지 파일 압축
async def create_zip_file(folder_path) -> io.BytesIO:
    print("이미지 압축 시작")

    s = io.BytesIO()
    zip_file = zipfile.ZipFile(s, "w")

    for img in IMG_FILES:
        image_path = folder_path / img
        zip_file.write(image_path, img)
        print("압축")

    zip_file.close()
    s.seek(0)
    
    print("이미지 압축 완료")
    return s

# -----------------------------------------------------------

## utils 디렉토리로 이동

# ### 텍스트 파일 불러오기 및 리스트 변환
# async def convert_txt_files_to_list():
#     print("텍스트 리스트 변환 시작")
    
#     results = []

#     for txt in TXT_FILES:
#         txt_path = DET_FOLDER/'exp/labels' / txt
#         txt_list = []

#         try:
#             if txt_path.is_file():
#                 with open(txt_path, 'r') as f: # read-only
#                     print("텍스트 파일 읽어오기")
#                     for line in f:
#                         txt_list.append(line.strip().split())
#                 results.append(txt_list)
#             else:
#                 print(f"File {txt_path} does not exist.")
#                 results.append(txt_list)  # Append empty list if file does not exist

#         except Exception as e:
#             print(f"Error reading file {txt_path}: {e}")
#             results.append(txt_list)  # Append empty list if there is an error

#     print("텍스트 리스트 변환 종료")
#     return results