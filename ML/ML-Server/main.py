from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import os
import sys
import cv2
import time
from pathlib import Path

sys.path.append(os.path.join(os.path.dirname(os.path.abspath(os.path.dirname(__file__))), 'yolov9'))

# from yolov9.models import common
from utils.torch_utils import select_device
from detect_dual import run

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
DEVICE = select_device('')

# 사전 사진 촬영 체크 변수
IMG_CHECK = False

#---------------------------------------------------------------------

### 웹캠 사진 촬영 3장
def capture_photos():
    print("capture_photos() 시작")
    # 기본 웹캠에 연결
    cap = cv2.VideoCapture(0)

    if not cap.isOpened():
        print("웹캠 연결 끊김")
        return {"error": "Could not open webcam."}

    # 3장 촬영
    photo_count = 3
    for i in range(photo_count):
        ret, frame = cap.read()
        if not ret:
            print("촬영 실패")
            return {"error": "Failed to capture image."}

        # 촬영 사진을 지정된 경로 폴더에 저장
        print("촬영 성공")
        img_path = os.path.join(IMG_FOLDER, f'photo_{i+1}.jpg')
        cv2.imwrite(img_path, frame)
        time.sleep(1)  # Wait for a second before capturing the next image

    # Release the webcam
    cap.release()
    global IMG_CHECK 
    IMG_CHECK = True # 사진이 무사히 찍혔다면 1
    print("capture_photos() 종료")
    return {"message": "Photos captured successfully."}

#---------------------------------------------------------------------

### 사진 촬영 POST 요청
@app.post("/capture")
def capture():
    result = capture_photos()
    return JSONResponse(content=result)

#---------------------------------------------------------------------

### 객체 탐지 결과 GET 요청
@app.get("/detect")
async def detect_objects():
    try:
        global IMG_CHECK 
        if not IMG_CHECK:
            print("사전에 찍힌 이미지가 없습니다.")
            print("사진 촬영 메서드를 실행합니다.")
            capture_photos()
        
        detections = [] 
        # run 함수 직접 호출
        detections = run(
            weights=WEIGHTS,
            source=IMG_FOLDER,
            nosave=True # txt, img 저장 안 함 
        )
        print("main.py detect 실행 결과", detections)

        if not detections:
            raise HTTPException(status_code=404, detail="탐지 결과를 찾을 수 없습니다.")

        #JSON으로 전달할 객체 탐지 결과를 담을 리스트
        results = [] 

        # 이미지 3장의 객체 탐지 결과 순차적으로 탐색
        count = 0
        for img_det in detections:
            count += 1
            det_results = []
            for det in img_det:
                class_id, x, y, w, h, conf = map(float, det)
                det_results.append({
                    "class_id": int(class_id),
                    "x": x,
                    "y": y,
                    "width": w,
                    "height": h,
                    "confidence": conf
                })
            results.append({
                "image": count,
                "detections": det_results
            })

        IMG_CHECK = False # 사진 체크 변수 초기화
        return JSONResponse(content={"results": results})

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")

#---------------------------------------------------------------------

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)