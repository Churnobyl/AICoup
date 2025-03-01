from PIL import Image
import numpy as np
import io
import asyncio
import zipfile
import cv2

from core import *

# -----------------------------------------------------------
# txt 데이터
# -----------------------------------------------------------
# 딕셔너리 변환


async def convert_to_dict(all_files_data):

    if asyncio.iscoroutine(all_files_data):
        all_files_data = await all_files_data

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
# 로그 큐 변환


async def convert_queue():
    print("로그 큐 DET_LOGS 변환 시작")

    count = len(DET_LOGS)
    if count == 0:
        print("로그 없음")
        return "이전 객체 탐지 내역 없음"

    results = [None] * count  # 미리 리스트 크기 설정
    seq = ""
    for i, log in enumerate(DET_LOGS):
        print(f"최근 {i}번째 로그 탐지")
        if log:
            res = await convert_to_dict(log)
        else:
            res = []

        match i:
            case 0:
                seq = "최신"
            case 1:
                seq = "이전"
            case 2:
                seq = "가장 오래 된"

        results[i] = {
            "time": f"{seq} 탐지 결과",
            "log": res
        }

    print("로그 큐 변환 종료")
    return results

# -----------------------------------------------------------
# 이미지 데이터
# -----------------------------------------------------------
# 이미지 버퍼 변환
# 이미지 메모리 임시 저장


def add_image(frame, buffer_path, img_type):

    try:
        # numpy 배열을 PIL 이미지로 변환
        if isinstance(frame, np.ndarray):
            if img_type == "cap":
                # cv2의 BGR 배열을, RGB로 변환
                frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            frame = Image.fromarray(frame)

        # 이미지를 메모리 버퍼에 저장
        # io.BytesIO를 사용하여 메모리 버퍼에 저장된 이미지는 .jpg 파일과 동일한 데이터 형식을 가집니다.
        buffer = io.BytesIO()
        frame.save(buffer, format='JPEG')
        buffer.seek(0)
        buffer_path.append(buffer)

        print("add_image() 성공")

    except Exception as e:
        raise RuntimeError(f"이미지 임시 저장 실패: {str(e)}")

# -----------------------------------------------------------
# 버퍼 이미지 변환
# 버퍼에 저장된 이미지 불러오기


def load_images_from_buffers(buffer_path):

    if not buffer_path:  # 버퍼가 비어있다면
        raise ValueError("utils 함수 오류. buffers가 비어 있습니다.")

    for i, buffer in enumerate(buffer_path):
        # 버퍼에서 이미지 불러오기
        buffer.seek(0)  # 버퍼의 시작으로 이동
        im0 = Image.open(buffer)

        # tensor 형식에 맞게,
        # 1) 이미지 리사이즈
        im0 = im0.resize((1280, 720), Image.Resampling.BILINEAR)
        im0s = np.array(im0)
        im0 = im0.resize((640, 384), Image.Resampling.BILINEAR)
        im = np.array(im0)
        # 2) (H, W, C) 형식의 이미지를 (C, H, W) 형식으로 변환
        im = im.transpose(2, 0, 1)

        # LoadImages 클래스 반환값과 유사한 반환값 생성
        path = f"photo_{i+1}"
        vid_cap = None
        s = f"Image {i+1}: {path}, {im.shape[1]}x{im.shape[2]}"

        yield path, im, im0s, vid_cap, s

# -----------------------------------------------------------
# 이미지 스트림 변환


async def stream_images_from_buffers(buffer_path):

    for i, buffer in enumerate(buffer_path):
        # 버퍼에서 이미지 불러오기
        buffer.seek(0)  # 버퍼의 시작으로 이동
        img = buffer.read()

        if img:
            yield (
                b"--frame\r\n"
                b"Content-Type: image/jpeg\r\n\r\n" + img + b"\r\n"
            )
        print(f"{i+1} 이미지 스트리밍 전송")

        await asyncio.sleep(1)  # 각 이미지 사이에 짧은 지연 추가

# -----------------------------------------------------------
# 이미지 압축


async def zip_images_from_buffers(buffer_path) -> io.BytesIO:

    s = io.BytesIO()
    zip_file = zipfile.ZipFile(s, "w")

    for i, buffer in enumerate(buffer_path):
        buffer.seek(0)  # 버퍼의 시작으로 이동

        # 버퍼 데이터를 ZIP 파일에 추가
        img_name = f"photo_{i+1}.jpg"
        zip_file.writestr(img_name, buffer.read())
        print(f"{img_name} 압축 완료")

    zip_file.close()
    s.seek(0)

    return s

# -----------------------------------------------------------
# 디렉토리 파일 사용
# -----------------------------------------------------------
# 이미지 파일 불러오기


async def load_image(image_path):
    try:
        with open(image_path, "rb") as image_file:
            print(image_path, "이미지 파일 읽기")
            return image_file.read()

    except FileNotFoundError:
        print(f"파일을 찾을 수 없습니다: {image_path}")
        return None

# -----------------------------------------------------------
# 텍스트 파일 불러오기 및 변환


async def convert_txt_file():
    print("텍스트 파일 변환 시작")

    results = []

    for txt in SAMPLE_TXT_FILES:
        txt_path = SAMPLE_DET_FOLDER/'exp/labels' / txt
        txt_list = []

        try:
            if txt_path.is_file():
                with open(txt_path, 'r') as f:  # read-only
                    print(txt_path, "텍스트 파일 읽어오기")
                    for line in f:
                        txt_list.append(line.strip().split())
                results.append(txt_list)
            else:
                print(f"File {txt_path} does not exist.")
                # Append empty list if file does not exist
                results.append(txt_list)

        except Exception as e:
            print(f"Error reading file {txt_path}: {e}")
            results.append(txt_list)  # Append empty list if there is an error

    print("텍스트 파일 변환 종료")
    return results
