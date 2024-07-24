from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import os
import sys
import cv2
import time
from pathlib import Path

sys.path.append(os.path.join(os.path.dirname(os.path.abspath(os.path.dirname(__file__))), 'yolov9'))

# from yolov9.models import common
from models.common import DetectMultiBackend
from utils.torch_utils import select_device
from detect_dual import run

app = FastAPI()

# YOLO 폴더 경로
FILE = Path(__file__).resolve()
ROOT = FILE.parents[0]  # YOLO root directory

# 사진 저장 폴더 경로
capture_img_path = ROOT / 'capture_img'

# 사진 저장 폴더 생성
os.makedirs(capture_img_path, exist_ok=True)


# 전역 변수로 모델 초기화
print(ROOT)
weights=ROOT / 'yolov9-s.pt'  # 모델 가중치 파일 경로
device = select_device('')
# model = DetectMultiBackend(weights, device=device, dnn=False, data='../yolov9/data/coco.yaml', fp16=False)

# 디버그: device와 weights 출력
print(f"Device: {device}, Weights: {weights}")




### 웹캠 사진 촬영 3장
def capture_photos():
    # 기본 웹캠에 연결
    cap = cv2.VideoCapture(0)

    if not cap.isOpened():
        return {"error": "Could not open webcam."}

    # 3장 촬영
    photo_count = 3
    for i in range(photo_count):
        ret, frame = cap.read()
        if not ret:
            return {"error": "Failed to capture image."}

        # 사진을 지정된 경로 폴더에 저장
        img_path = os.path.join(capture_img_path, f'photo_{i+1}.jpg')
        cv2.imwrite(img_path, frame)
        time.sleep(1)  # Wait for a second before capturing the next image

    # Release the webcam
    cap.release()
    return {"message": "Photos captured successfully."}

#---------------------------------------------------------------------

### 사진 촬영 요청
@app.post("/capture")
def capture():
    result = capture_photos()
    return JSONResponse(content=result)

#---------------------------------------------------------------------

### 분석 결과 전송
@app.get("/detect")
async def detect_objects():
    try:
        # run 함수 직접 호출
        run(
            # model=model,  # 전역 model 인스턴스 전달
            ### source를, FastAPI의 Post요청으로 생성된 사진 폴더로 연결
            weights=weights,
            source=capture_img_path,  # source folder
            save_txt=True,
            save_conf=True,
        )

        # 결과 폴더 찾기 (가장 최근에 생성된 exp 폴더)
        detect_dir = Path(ROOT / '../yolov9/runs/detect')
        latest_exp = max(detect_dir.glob("exp*"), key=os.path.getctime)
        
        if latest_exp is None:
            raise HTTPException(status_code=404, detail="탐지 결과를 찾을 수 없습니다.")

        # labels 폴더에서 결과 읽기
        results = []
        labels_dir = latest_exp / "labels"
        for label_file in labels_dir.glob("*.txt"):
            with open(label_file, "r") as f:
                lines = f.readlines()
                file_results = []
                for line in lines:
                    class_id, x, y, w, h, conf = map(float, line.strip().split())
                    file_results.append({
                        "class_id": int(class_id),
                        "x": x,
                        "y": y,
                        "width": w,
                        "height": h,
                        "confidence": conf
                    })
                results.append({
                    "image": label_file.stem,
                    "detections": file_results
                })

        return JSONResponse(content={"results": results})

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")

#---------------------------------------------------------------------

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)