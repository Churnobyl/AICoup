from fastapi import APIRouter, Query
from fastapi.responses import JSONResponse, StreamingResponse
from typing import Optional

from service import *
from utils_main import *
from core import *

router = APIRouter(
    prefix='/api/debug'
)

# -----------------------------------------------------------
# images
# -----------------------------------------------------------
# 이미지 촬영


@router.post('/images')
async def capture_images():
    msg = await media_service.capture_images()
    return msg

# -----------------------------------------------------------
# 이미지 요청
# 이미지 스트리밍
# 이미지 다운로드
'''
쿼리 파라미터
cap이미지와 conf이미지는 동일한 리소스,
stream과 download는 리소스에 대한 행위
'''


@router.get('/images')
async def get_images(
    # conf, cap 이외 유효성 검사
    img_type: str = Query(default='cap', regex='^(cap|conf)$'),
    # stream, download 이외 유효성 검사
    action: str = Query(default="stream", regex="^(stream|download)$")
):
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
# 객체 탐지 재추론


@router.post('/labels')
async def reprocess_inference():
    # 메모리 버퍼의 이미지 사용
    results = await convert_to_dict(inference("buffers"))
    return JSONResponse(content=results)

# -----------------------------------------------------------
# 객체 탐지 결과 요청
# 객체 탐지 yolo 결과
# 객체 탐지 클러스터링 결과


@router.get('/labels')
async def get_labels(
    result_type: str = Query(
        default="det", regex="^(det|clst)$"),  # det, clst 이외 유효성 검사
    step: Optional[int] = Query(default=None),  # step 파라미터는 선택적
    plot: Optional[int] = Query(default=None)  # plot 파라미터는 선택적
):
    if result_type == "det":
        # 저장된 객체 탐지 결과 내역 반환
        detection_results = await convert_queue()
        return JSONResponse(content=detection_results)

    elif result_type == "clst":
        # 클러스터링 과정 반환
        match step:
            # 클러스터링 단계별 결과
            case 1:
                print('클러스터링 1단계')
            case 2:
                print('클러스터링 2단계')
            case 3:
                print('클러스터링 3단계')
            case _:
                print('Value is something else')
                return await JSONResponse(CLST_DATA)

        match plot:
            # 클러스터링 단계별 플롯 이미지
            case 1:
                print('클러스터링 1단계 이미지')
            case 2:
                print('클러스터링 2단계 이미지')
            case 3:
                print('클러스터링 3단계 이미지')
            case _:
                print('Value is something else')
        pass

    # regex를 통해 유효하지 않은 값이 들어오면, FastAPI가 자동으로 422 Unprocessable Entity 에러 코드 반환

# -----------------------------------------------------------
