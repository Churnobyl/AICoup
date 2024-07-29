from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse,StreamingResponse

from service import *
from utils_main import *
from core import *

router = APIRouter(
    prefix='/api/debug'
)

# -----------------------------------------------------------
### 이미지 촬영
@router.post('/images/capture')
async def capture_images():
    msg = media_service.capture_images() # 중복된 메서드명 변경해야 하는데 마땅한 게 생각이 안 난다요...
    return JSONResponse(
        content=msg
    )

# -----------------------------------------------------------
### 이미지 스트리밍

# 쿼리 파라미터로 cap, conf 구분
@router.get('/images/stream/{img_type}')
async def stream_images(img_type: str = 'conf'): # 디폴트 conf

    # 추론 후 저장된 기존 모니터링 이미지 conf img
    if img_type == 'conf':
        folder_path = DET_FOLDER/'exp'

    # 저장된 기본 촬영 이미지 cap img
    elif img_type == 'cap':
        folder_path = IMG_FOLDER

    # 이외 쿼리문은 예외 처리
    else:
        raise HTTPException(status_code=400, detail="Invalid type parameter")
    
    # 이미지 스트리밍 생성
    image_stream = create_image_stream(folder_path)

    return StreamingResponse(
        image_stream,
        media_type="multipart/x-mixed-replace; boundary=frame"
    )

# -----------------------------------------------------------
### 이미지 다운로드

# 쿼리 파라미터로 cap, conf 구분
@router.get('/images/download/{img_type}')
async def download_images(img_type: str = 'conf'): # 디폴트 conf

    # 추론 후 저장된 기존 모니터링 이미지 conf img
    if img_type == 'conf':
        folder_path = DET_FOLDER/'exp'

    # 저장된 기본 촬영 이미지 cap img
    elif img_type == 'cap':
        folder_path = IMG_FOLDER

    # 이외 쿼리문은 예외 처리
    else:
        raise HTTPException(status_code=400, detail="Invalid type parameter")
    
    # 이미지 파일 압축
    zip_file = create_zip_file(folder_path)

    return StreamingResponse(
        zip_file, 
        media_type="application/x-zip-compressed", 
        headers={"Content-Disposition": "attachment;filename=images.zip"}
    )

# -----------------------------------------------------------
### 객체 탐지 재추론

# 객체 재탐지 (결과 json 전송)
@router.post('/labels/reprocess')
async def reprocess_inference():
    results = convert_list_to_json(inference())
    return JSONResponse(content=results)

# -----------------------------------------------------------
### 객체 탐지 텍스트 결과 JSON 전송

# 객체 탐지 기존 텍스트 결과 JSON 전송
@router.get('/labels')
async def get_labels():
    results = convert_list_to_json(convert_txt_files_to_list())
    return JSONResponse(content=results)
