import time
import io

from core import *
from utils_main import *


# -----------------------------------------------------------
# 이미지 촬영


def capture_images():
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
            print(f"사진 {count} 촬영")
            
            # 촬영 사진을 메모리에 임시 저장
            # util 함수
            add_image(frame)
            print(f"사진 {count} 임시 저장")
            
            # 대기
            time.sleep(0.5)

        print("capture_images() 종료")
        return {"message": "Photos captured successfully."}

    except Exception as e:
        print(f"Error: {str(e)}")
        return {"error": str(e)}

# -----------------------------------------------------------
# 이미지 파일 스트리밍 만들기


async def create_image_stream(img_type):
    print("create_image_stream() 시작")
    
    match img_type:
        case "cap": # 촬영 이미지
            save_buffers = CAP_IMG_BUFFERS
        case "conf": # 탐지 이미지
            save_buffers = CONF_IMG_BUFFERS

    count = len(save_buffers)
    if count == 0:
        print("이미지 없음")
        raise ValueError("저장된 이미지 없음")
    
    try:
        # util 함수
        async for img in stream_images_from_buffers(save_buffers):
            yield img
            
    except asyncio.CancelledError: # FastAPI reload할 때 종종 CancelledError 발생
        print("이미지 스트리밍 작업이 취소되었습니다.")
        raise  # 예외를 다시 발생시켜 호출자에게 전달

    print("create_image_stream() 종료")
    
    
# # 이미지 파일 스트리밍 만들기
# def create_image_stream(img_type):
#     print("create_image_stream() 시작")
    
#     match img_type:
#         case "cap":  # 촬영 이미지
#             save_buffers = CAP_IMG_BUFFERS
#         case "conf":  # 탐지 이미지
#             save_buffers = CONF_IMG_BUFFERS

#     count = len(save_buffers)
#     if count == 0:
#         print("이미지 없음")
#         raise ValueError("저장된 이미지 없음")
    
#     try:
#         # util 함수 호출하여 이미지 스트리밍 생성
#         for img in stream_images_from_buffers(save_buffers):
#             yield img
            
#     except Exception as e:  # 동기 방식에서는 일반 예외 처리로 충분
#         print(f"이미지 스트리밍 작업 중 예외 발생: {str(e)}")
#         raise

#     print("create_image_stream() 종료")

# -----------------------------------------------------------
# 이미지 파일 압축
# 작은 작업이라 동기 방식으로


def create_zip_file(img_type) -> io.BytesIO:
    print("이미지 압축 시작")

    match img_type:
        case "cap": # 촬영 이미지
            save_buffers = CAP_IMG_BUFFERS
        case "conf": # 탐지 이미지
            save_buffers = CONF_IMG_BUFFERS

    # util 함수
    zip_file_buffer = zip_images_from_buffers(save_buffers)

    print("이미지 압축 완료")
    return zip_file_buffer

# -----------------------------------------------------------