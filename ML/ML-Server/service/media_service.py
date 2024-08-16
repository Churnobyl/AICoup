import asyncio
import io

from core import *
from utils_main import *


# -----------------------------------------------------------
# buffers 사용
# 촬영 이미지
# -----------------------------------------------------------
# 이미지 촬영
# buffers에 저장


async def capture_images(save_path=None):
    print("capture_images() 시작")

    try:
        # Singleton
        cap = get_capture_manager()

        # 기본 저장 경로 설정
        if save_path is None:
            save_path = CAP_IMG_BUFFERS

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

            # 촬영 이미지 버퍼 임시 저장
            # util 함수
            add_image(frame, save_path, "cap")
            print(f"이미지 {count} 임시 저장")
            print("버퍼: ", len(save_path), "/3")

            # 대기
            await asyncio.sleep(0.5)

        print("capture_images() 종료")
        return {"이미지 촬영 성공"}

    except Exception as e:
        print(f"Error: {str(e)}")
        return {"error": str(e)}

# -----------------------------------------------------------
# buffers 이미지 파일 스트리밍 만들기


async def create_image_stream(img_type):
    print("create_image_stream() 시작")

    match img_type:
        case "cap":  # 촬영 이미지
            buffer_path = CAP_IMG_BUFFERS
        case "conf":  # 탐지 이미지
            buffer_path = CONF_IMG_BUFFERS
        case "test-cap":  # 테스트 촬영 이미지
            buffer_path = TEST_CAP_IMG_BUFFERS
        case "test-conf":  # 테스트 탐지 이미지
            buffer_path = TEST_CONF_IMG_BUFFERS

    print(buffer_path)

    if len(buffer_path) == 0:
        print("이미지 없음")
        raise ValueError("저장된 이미지 없음")

    try:
        # util 함수
        async for img in stream_images_from_buffers(buffer_path):
            yield img

    except asyncio.CancelledError:  # FastAPI reload할 때 종종 CancelledError 발생
        print("이미지 스트리밍 작업이 취소되었습니다.")
        raise

    print("create_image_stream() 종료")

# -----------------------------------------------------------
# buffers 이미지 파일 압축


async def create_zip_file(img_type) -> io.BytesIO:
    print("이미지 압축 시작")

    match img_type:
        case "cap":  # 촬영 이미지
            buffer_path = CAP_IMG_BUFFERS
        case "conf":  # 탐지 이미지
            buffer_path = CONF_IMG_BUFFERS
        case "test-cap":  # 테스트 촬영 이미지
            buffer_path = TEST_CAP_IMG_BUFFERS
        case "test-conf":  # 테스트 탐지 이미지
            buffer_path = TEST_CONF_IMG_BUFFERS

    # util 함수
    zip_file_buffer = await zip_images_from_buffers(buffer_path)

    print("이미지 압축 완료")
    return zip_file_buffer

# -----------------------------------------------------------
# dir 파일 사용
# sample 이미지
# -----------------------------------------------------------
# sample 이미지 파일 스트리밍 만들기


async def create_image_stream_from_folder(folder_path):
    print("create_image_stream_from_folder() 시작")

    for img in SAMPLE_IMG_FILES:
        image_path = folder_path / img
        image_data = await load_image(image_path)

        if image_data:
            try:
                yield (
                    b"--frame\r\n"
                    b"Content-Type: image/jpeg\r\n\r\n" + image_data + b"\r\n"
                )
            except asyncio.CancelledError:  # FastAPI reload할 때 종종 CancelledError 발생
                print("이미지 스트리밍 작업이 취소되었습니다.")
                raise

        print(f"{img} 스트리밍 전송")
        await asyncio.sleep(1)  # 각 이미지 사이에 짧은 지연 추가

    print("create_image_stream_from_folder() 종료")

# -----------------------------------------------------------
# sample 이미지 파일 압축


async def create_zip_file_from_folder(folder_path) -> io.BytesIO:
    print("이미지 압축 시작")

    s = io.BytesIO()
    zip_file = zipfile.ZipFile(s, "w")

    for img in SAMPLE_IMG_FILES:
        image_path = folder_path / img
        zip_file.write(image_path, img)
        print(f"{img} 압축 완료")

    zip_file.close()
    s.seek(0)

    print("이미지 압축 완료")
    return s
