import os
import time
import cv2
import asyncio
import zipfile
import io

from core import *

### 이미지 파일은 .jpg 고정

# -----------------------------------------------------------
### 이미지 촬영
def capture_images():
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
### 이미지 파일 불러오기
async def get_image_data(image_path):
    try:
        with open(image_path, "rb") as image_file:
            print("이미지 파일 읽기")
            return image_file.read()
    except FileNotFoundError:
        print(f"파일을 찾을 수 없습니다: {image_path}")
        return None

# -----------------------------------------------------------
### 이미지 파일 스트리밍 만들기
async def image_stream_generator(file_path):
    print("image_stream_generator() 시작")
    
    for img in IMG_FILES:
        image_path = file_path / img
        image_data = await get_image_data(image_path)
        if image_data:
            yield (
                b"--frame\r\n"
                b"Content-Type: image/jpeg\r\n\r\n" + image_data + b"\r\n"
            )
        print("스트리밍 전송")
        await asyncio.sleep(1)  # 각 이미지 사이에 짧은 지연 추가

    print("image_stream_generator() 종료")
# -----------------------------------------------------------