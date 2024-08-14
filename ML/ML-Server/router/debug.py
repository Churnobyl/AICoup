# 디버깅용
# buffer의 이미지 사용
# 게임 생성 확인
# 웹캠 작동 확인
# 촬영 이미지 확인
# yolo 작동 확인

from fastapi import APIRouter, Query
from fastapi.responses import JSONResponse, StreamingResponse

from service import *
from utils_main import *
from core import *

router = APIRouter(
    prefix='/api/debug'
)

# -----------------------------------------------------------
# game
# -----------------------------------------------------------
# 게임 상태 조회


@router.get('/game-raw-status')
async def get_game_raw_status():
    """_main.app.game 실행 여부_

    Returns:
        _message_: _game 실행 여부_
    """
    return JSONResponse(main.app.game.__dict__)

# -----------------------------------------------------------
# images
# -----------------------------------------------------------
# test 이미지 촬영
# 웹캠 작동 확인


@router.post('/images')
async def capture_images():
    """_웹캠 작동 테스트로 이미지 촬영, TEST_IMG_BUFFERS 이미지 임시 저장_

    Returns:
        _message_: _웹캠 실행 및 촬영 성공 여부_
    """
    print("웹캠 작동 테스트")
    msg = await media_service.capture_images(TEST_CAP_IMG_BUFFERS)
    return msg

# -----------------------------------------------------------
# 이미지 요청
# 이미지 스트리밍 / 다운로드
# cap, conf, test 이미지


@router.get('/images')
async def get_images(
    # conf, cap, test 이외 유효성 검사
    img_type: str = Query(
        default='test-cap', regex='^(cap|conf|test-cap|test-conf)$'),
    # stream, download 이외 유효성 검사
    action: str = Query(default="stream", regex="^(stream|download)$")
):
    """_진행 게임 혹은 테스트 촬영 이미지 스트림 및 다운로드_

    Args:
        img_type (str, optional): _진행 게임 | 웹캠 테스트의 촬영 혹은 객체 탐색 결과 이미지_,
        action (str, optional): _이미지 스트림 혹은 다운로드_

    Returns:
        _media_type_: _이미지 스트림 혹은 다운로드_
    """
    # regex를 통해 유효하지 않은 값이 들어오면, FastAPI가 자동으로 422 Unprocessable Entity 에러 코드 반환

    if action == "stream":
        # 이미지 스트리밍 생성
        image_stream = create_image_stream(img_type)
        return StreamingResponse(
            image_stream,
            media_type="multipart/x-mixed-replace; boundary=frame"
        )

    elif action == "download":
        # 이미지 파일 압축
        zip_file = await create_zip_file(img_type)
        return StreamingResponse(
            zip_file,
            media_type="application/x-zip-compressed",
            headers={"Content-Disposition": "attachment;filename=images.zip"}
        )

# -----------------------------------------------------------
# labels
# -----------------------------------------------------------
# 객체 탐지
# 클러스터링 제외, yolo만


@router.post('/labels')
async def process_inference():
    """_웹캠 테스트 촬영 이미지로, YOLOv9의 detect_dual.run() 객체 탐지 작동 확인_

    Returns:
        _message_: _객체 탐지 성공 여부_
    """
    msg = await inference("buffers", TEST_CAP_IMG_BUFFERS)
    # 클러스터링 제외
    return msg

# -----------------------------------------------------------
# 객체 탐지 결과 요청


@router.get('/labels')
async def get_labels(
    # det, clst, test 이외 유효성 검사
    log_type: str = Query(default='test', regex='^(det|clst|test)$')
):
    """_진행 게임의 YOLOv9 객체 탐지 결과 혹은 클러스터링 결과 재조회 | 웹캠 테스트 촬영 이미지 객체 탐지 결과 조회_

    Args:
        log_type (str, optional): _YOLO 객체 탐지 결과 | 클러스터링 결과 누적 기록_

    Returns:
        _type_: _객체 탐지 | 클러스터링 (누적)결과 반환_
    """
    # regex를 통해 유효하지 않은 값이 들어오면, FastAPI가 자동으로 422 Unprocessable Entity 에러 코드 반환

    match log_type:
        # board.get_game_status() 실행 결과 누적 3회
        case "det":
            # yolo 객체 탐지 결과
            if DET_LOGS:
                detection_results = await convert_queue()
                return JSONResponse(content=detection_results)
            else:
                return {"객체 탐지 누적 결과가 없습니다."}

        case "clst":
            # 클러스터링 결과
            if CLST_LOGS:
                return JSONResponse(CLST_LOGS)
            else:
                return {"클러스터링 누적 결과가 없습니다."}

        # 촬영한 test 이미지 탐지 1회
        case "test":
            # yolo 테스트 결과 (클러스터링 제외)
            if TEST_IMG_DET:
                detection_results = await convert_to_dict(TEST_IMG_DET[-1])
                return JSONResponse(content=detection_results)
            else:
                return {"최신 테스트 이미지의 객체 탐지 결과가 없습니다."}

# -----------------------------------------------------------
