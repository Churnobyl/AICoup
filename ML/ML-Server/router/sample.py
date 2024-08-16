# 샘플 테스트용
# media dir 이미지 사용
# 샘플 이미지 확인
# yolo, 클러스터링 확인

from fastapi import APIRouter, Query
from fastapi.responses import JSONResponse, StreamingResponse
from typing import Optional

from service import *
from utils_main import *
from core import *

router = APIRouter(
    prefix='/api/sample'
)

# -----------------------------------------------------------
# images
# -----------------------------------------------------------

@router.get('/images')
async def get_images(
    # conf, cap 이외 유효성 검사
    img_type: str = Query(default='cap', regex='^(cap|conf)$'),
    # stream, download 이외 유효성 검사
    action: str = Query(default="stream", regex="^(stream|download)$")
):
    """_media 디렉토리의 샘플 이미지 스트림 및 다운로드_

    Args:
        img_type (str, optional): _샘플 촬영 혹은 객체 탐지 결과 이미지_,
        action (str, optional): _이미지 스트림 혹은 다운로드_

    Returns:
        _media_type_: _이미지 스트림 혹은 다운로드_
    """
    
    # 추론 후 저장된 기존 모니터링 이미지 (conf img)
    if img_type == 'conf':
        folder_path = SAMPLE_DET_FOLDER / 'exp'
    # 저장된 기본 촬영 이미지 (cap img)
    elif img_type == 'cap':
        folder_path = SAMPLE_IMG_FOLDER

    # regex를 통해 유효하지 않은 값이 들어오면, FastAPI가 자동으로 422 Unprocessable Entity 에러 코드 반환

    if action == "stream":
        # 이미지 스트리밍 생성
        image_stream = create_image_stream_from_folder(folder_path)
        return StreamingResponse(
            image_stream,
            media_type="multipart/x-mixed-replace; boundary=frame"
        )

    elif action == "download":
        # 이미지 파일 압축
        zip_file = await create_zip_file_from_folder(folder_path)
        return StreamingResponse(
            zip_file,
            media_type="application/x-zip-compressed",
            headers={"Content-Disposition": "attachment;filename=images.zip"}
        )

# -----------------------------------------------------------
# labels
# -----------------------------------------------------------
# 객체 탐지 추론
# sample 이미지

# situation format...
# {
#     'name': str,
#     'player_id': Optional[int]
# }

@router.post('/labels')
async def process_inference(
    # det, clst 이외 유효성 검사
    mode: str = Query(default='det', regex='^(det|clst)$'),
    situation: Optional[dict] = None
):
    """_media 디렉토리의 sample 이미지로 yolo 객체 탐지 | 클러스터링_

    Args:
        mode (str, optional): _yolo 객체 탐지 | 클러스터링_
        situation (Optional[dict], optional): _['amb_pick', 'amb_done', 'ch_win']_

    Raises:
        HTTPException: _description_

    Returns:
        _message_: _객체 탐지 성공 여부_,
        현재 클러스터링 결과값을 임의로 전송받음.
    """

    # regex를 통해 유효하지 않은 값이 들어오면, FastAPI가 자동으로 422 Unprocessable Entity 에러 코드 반환

    match mode:
        case "det":
            # yolo 객체 탐지
            # media 디렉토리의 샘플 이미지 사용
            msg = await inference("sample_dir")
            return msg

        case "clst":
            detection_results = await convert_to_dict(await convert_txt_file())

            exceptLog = []  # 예외로그
            for _ in range(2):
                try:
                    # 클러스터링
                    clustered_result = tracePlayers(
                        detection_results, situation)

                    if clustered_result is not None:
                        main.app.game.playersCard = clustered_result[0]
                        main.app.game.deckCard = clustered_result[1]
                        clustered_result = {
                            "user_card": clustered_result[0],
                            "deck_card": clustered_result[1]
                        }

                        # 클러스터링 결과값 튜플 1회 저장
                        #TODO: 전역 변수 문제로 현재 임시로 deque(maxlen=1) 사용
                        SAMPLE_IMG_CLST.append(clustered_result)  # 1회 저장

                        if clustered_result:
                            return {"클러스터링 성공"}

                        else:
                            return {"클러스터링 실패"}

                except Exception as e:
                    exceptLog.append(e)

            raise HTTPException(status_code=404, detail={
                'cause': 'Over trial...', 'exceptLog': str(exceptLog)})

# -----------------------------------------------------------
# 객체 탐지 결과 요청
# sample 이미지
# yolo, 클러스터링 결과


@router.get('/labels')
async def get_labels(
    result_type: str = Query(
        default="det", regex="^(det|clst)$")  # det, clst 이외 유효성 검사
):
    """_media 디렉토리의 sample 이미지로, YOLOv9의 detect_dual.run() 객체 탐지 | 클러스터링 작동 확인, game_start 선행 필수_

    Args:
        result_type (str, optional): _yolo 객체 탐지 | 클러스터링_

    Returns:
        _type_: _객체 탐지 | 클러스터링 결과 반환_
    """
    # regex를 통해 유효하지 않은 값이 들어오면, FastAPI가 자동으로 422 Unprocessable Entity 에러 코드 반환

    match result_type:
        case "det":
            # 저장된 txt 파일 반환
            detection_results = await convert_to_dict(await convert_txt_file())
            return JSONResponse(content=detection_results)

        case "clst":
            if SAMPLE_IMG_CLST:
                clustered_result = await convert_to_dict(TEST_IMG_DET[-1])
                return JSONResponse(content=clustered_result)
            else:
                return {"클러스터링 결과가 없습니다."}

# -----------------------------------------------------------
