from fastapi import FastAPI
import os
import sys
import cv2
import time
from pathlib import Path

sys.path.append(os.path.join(os.path.dirname(os.path.abspath(os.path.dirname(__file__))), 'yolov9'))

from router import board

app = FastAPI()

#---------------------------------------------------------------------

# YOLO 폴더 경로
FILE = Path(__file__).resolve()
ROOT = FILE.parents[0]  # YOLO root directory

# 촬영 사진 저장 폴더 경로 및 생성
IMG_FOLDER = ROOT / 'capture_img'
os.makedirs(IMG_FOLDER, exist_ok=True)

# 모델 가중치 파일 경로
WEIGHTS = ROOT / 'yolov9-s.pt'  
# DEVICE = select_device('')

# 사전 사진 촬영 체크 변수
IMG_CHECK = False

### 사진 촬영 POST 요청
# @app.post("/capture")
# def capture():
#     result = capture_photos()
#     return JSONResponse(content=result)

#---------------------------------------------------------------------

# ### 객체 탐지 결과 GET 요청
# @app.get("/detect")
# async def detect_objects():
#     try:
#         global IMG_CHECK 
#         if not IMG_CHECK:
#             print("사전에 찍힌 이미지가 없습니다.")
#             print("사진 촬영 메서드를 실행합니다.")
#             capture_photos()
        
#         detections = [] 
#         # run 함수 직접 호출
#         detections = run(
#             weights=WEIGHTS,
#             source=IMG_FOLDER,
#             nosave=True # txt, img 저장 안 함 
#         )
#         print("main.py detect 실행 결과", detections)

#         if not detections:
#             raise HTTPException(status_code=404, detail="탐지 결과를 찾을 수 없습니다.")

#         #JSON으로 전달할 객체 탐지 결과를 담을 리스트
#         results = makeJsonResult(detections)

#         IMG_CHECK = False # 사진 체크 변수 초기화
#         return JSONResponse(content={"results": results})

#     except Exception as e:
#         raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")

# #---------------------------------------------------------------------

app.include_router(board.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)