from fastapi import APIRouter
from fastapi.responses import JSONResponse,StreamingResponse

from service import *
from utils_main import *
from core import *

router = APIRouter(
    prefix='/api/debug'
)

# -----------------------------------------------------------
### 이미지 스트리밍

# 새 촬영 후 저장된 기본 이미지 스트리밍 전송
@router.get("/capture-images")
async def stream_capture_images():
    # 사진 촬영
    capture_images()
    return StreamingResponse(
        image_stream_generator(IMG_FOLDER),
        media_type="multipart/x-mixed-replace; boundary=frame"
    )

# 추론 후 저장된 모니터링 이미지 스트리밍 전송
@router.get("/conf-images")
async def stream_conf_images():
    return StreamingResponse(
        image_stream_generator(DET_FOLDER/'exp'),
        media_type="multipart/x-mixed-replace; boundary=frame"
    )

# -----------------------------------------------------------
### 이미지 다운로드

# 이미지 다운로드 파일 전송
# image_service.py에 모듈화하기
# 함수명 변경
@router.get('/images')
async def getConfImages():
    zip_filename = "images.zip"
    
    s = io.BytesIO()
    zf = zipfile.ZipFile(s, "w")

    for img in IMG_FILES:
        img_path = DET_FOLDER/'exp' / img
        zf.write(img_path, img)
    
    zf.close()

    s.seek(0)
    
    return StreamingResponse(
        iter([s.getvalue()]), 
        media_type="application/x-zip-compressed", 
        headers={"Content-Disposition": f"attachment;filename={zip_filename}"}
    )

# -----------------------------------------------------------
### 이미지 재추론

# 마지막으로 촬영된 이미지들을 토대로 재추론 (xywh값)
# 함수명 변경
@router.get('/labels')
async def getLabels():
    results = makeDetectionsJsonResult(inference())
    return JSONResponse(content={"results": results})
