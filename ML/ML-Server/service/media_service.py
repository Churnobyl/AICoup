import asyncio
import io

from core import *
from utils_main import *


# -----------------------------------------------------------
### 이미지 촬영


async def capture_images():
    print("capture_images() 시작")

    try:
        # Singleton
        cap = get_capture_manager()

        # 3장 반복 촬영
        count = 0
        for _ in range(3):
            count += 1

            # 버퍼에 있는 오래된 5 프레임 제거
            for _ in range(5):
                cap.cap.grab()

            # 촬영
            # core 웹캠 함수
            frame = cap.get_frame()
            print(f"이미지 {count} 촬영")
            
            # 촬영 이미지 메모리에 임시 저장
            # util 함수
            await add_image(frame, CAP_IMG_BUFFERS, "cap")
            print(f"이미지 {count} 임시 저장")
            print(CAP_IMG_BUFFERS)
            
            # 대기
            await asyncio.sleep(0.5)

        print("capture_images() 종료")
        return {"이미지 촬영 성공"}

    except Exception as e:
        print(f"Error: {str(e)}")
        return {"error": str(e)}

# -----------------------------------------------------------
### 이미지 파일 스트리밍 만들기


async def create_image_stream(img_type):
    print("create_image_stream() 시작")
    
    match img_type:
        case "cap": # 촬영 이미지
            buffer_path = CAP_IMG_BUFFERS
        case "conf": # 탐지 이미지
            buffer_path = CONF_IMG_BUFFERS

    count = len(buffer_path)
    if count == 0:
        print("이미지 없음")
        raise ValueError("저장된 이미지 없음")
    
    try:
        # util 함수
        async for img in stream_images_from_buffers(buffer_path):
            yield img
            
    except asyncio.CancelledError: # FastAPI reload할 때 종종 CancelledError 발생
        print("이미지 스트리밍 작업이 취소되었습니다.")
        raise

    print("create_image_stream() 종료")

# -----------------------------------------------------------
### 이미지 파일 압축


async def create_zip_file(img_type) -> io.BytesIO:
    print("이미지 압축 시작")

    match img_type:
        case "cap": # 촬영 이미지
            save_buffers = CAP_IMG_BUFFERS
        case "conf": # 탐지 이미지
            save_buffers = CONF_IMG_BUFFERS

    # util 함수
    zip_file_buffer = await zip_images_from_buffers(save_buffers)

    print("이미지 압축 완료")
    return zip_file_buffer

# -----------------------------------------------------------