import os
from pathlib import Path

# ML-Server root directory
FILE = Path(__file__).resolve()
ROOT = FILE.parents[1]  

# 촬영 사진 저장 폴더 경로 및 생성
IMG_FOLDER = ROOT / 'media/capture_img'
os.makedirs(IMG_FOLDER, exist_ok=True)

# 객체 탐지 결과 저장 폴더 경로
DET_FOLDER = ROOT / 'media/detect'

# 모델 가중치 파일 경로
WEIGHTS = ROOT / 'core/coup-0730.pt'  
DEVICE = ''

# 이미지 파일 목록
IMG_FILES = ["photo_1.jpg", "photo_2.jpg", "photo_3.jpg"]

# 텍스트 파일 목록
TXT_FILES = ["photo_1.txt", "photo_2.txt", "photo_3.txt"]

# 클러스터링 중간 결과값
test = {0: {'center': [0.5473437547683716, 0.15111111104488373], 'points': []},
            1: {'center': [0.5078125, 0.8891203701496124], 'points': []},
            2: {'center': [0.9234375059604645, 0.577777773141861], 'points': []},
            3: {'center': [0.11575520783662796, 0.4349537193775177], 'points': []},
            4: {'center': [0.46041667461395264, 0.5439814925193787], 'points': []}}

CLST_DATA = test