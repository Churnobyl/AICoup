import os
from pathlib import Path

# YOLO 폴더 경로
FILE = Path(__file__).resolve()
ROOT = FILE.parents[0]  # YOLO root directory

# 촬영 사진 저장 폴더 경로 및 생성
IMG_FOLDER = ROOT / 'capture_img'
os.makedirs(IMG_FOLDER, exist_ok=True)

# 모델 가중치 파일 경로
WEIGHTS = ROOT / 'yolov9-s.pt'  
DEVICE = ''

# 사전 사진 촬영 체크 변수
IMG_CHECK = False