import os
import time
import cv2
import asyncio
import zipfile
import io

from core import *
from utils_main import *

# 어플리케이션에서 생성된 이미지 파일은 .jpg 고정

# -----------------------------------------------------------
# 이미지 촬영


def capture_images():
    print("capture_images() 시작")

    try:
        # Singleton
        cap = get_capture_manager()

        # 3장 반복 촬영
        photo_count = 3
        for i in range(photo_count):

            # 버퍼에 있는 오래된 프레임 제거
            for _ in range(5):  # 필요한 만큼 버퍼 프레임 제거 (기본적으로 5~10 프레임 정도)
                cap.cap.grab()

            # 촬영
            frame = cap.get_frame()

            # 촬영 사진을 지정된 경로 폴더에 저장
            img_path = os.path.join(IMG_FOLDER, f'photo_{i+1}.jpg')
            print(f'photo_{i+1}.jpg 촬영 성공')
            cv2.imwrite(img_path, frame)
            # Wait for a second before capturing the next image
            time.sleep(0.5)

        print("capture_images() 종료")
        return {"message": "Photos captured successfully."}

    except Exception as e:
        print(f"Error: {str(e)}")
        return {"error": str(e)}

# -----------------------------------------------------------
# 이미지 파일 스트리밍 만들기


async def create_image_stream(folder_path):
    print("create_image_stream() 시작")

    for img in IMG_FILES:
        image_path = folder_path / img
        image_data = await load_image(image_path)

        if image_data:
            yield (
                b"--frame\r\n"
                b"Content-Type: image/jpeg\r\n\r\n" + image_data + b"\r\n"
            )
        print(f"{img} 스트리밍 전송")

        await asyncio.sleep(1)  # 각 이미지 사이에 짧은 지연 추가

    print("create_image_stream() 종료")

# -----------------------------------------------------------
# 이미지 파일 압축
# 작은 작업이라 동기 방식으로


def create_zip_file(folder_path) -> io.BytesIO:
    print("이미지 압축 시작")

    s = io.BytesIO()
    zip_file = zipfile.ZipFile(s, "w")

    for img in IMG_FILES:
        image_path = folder_path / img
        zip_file.write(image_path, img)
        print(f"{img} 압축 완료")

    zip_file.close()
    s.seek(0)

    print("이미지 압축 완료")
    return s

# -----------------------------------------------------------
