# 저장 경로 및 파일 경로 설정

from pathlib import Path
from collections import deque
import os


# -----------------------------------------------------------
# ML-Server root directory
FILE = Path(__file__).resolve()
ROOT = FILE.parents[1]

# 모델 가중치 파일 경로
WEIGHTS = ROOT / 'core/coup-0812.pt'
DEVICE = ''

# -----------------------------------------------------------
# buffers
# 버퍼 임시 저장 경로
# 실시간 촬영 이미지


# 최신 촬영 이미지 3장 버퍼 저장
CAP_IMG_BUFFERS = deque(maxlen=3)

# 최신 탐지 결과 이미지 3장 버퍼 저장
CONF_IMG_BUFFERS = deque(maxlen=3)

# 웹캠 테스트 촬영 이미지 3장 버퍼 저장
TEST_CAP_IMG_BUFFERS = deque(maxlen=3)

# 웹캠 테스트 객체 탐지 결과 이미지 3장 버퍼 저장
TEST_CONF_IMG_BUFFERS = deque(maxlen=3)

# -----------------------------------------------------------
# logs
# 결과값 저장

# 객체 탐지 결과값 최대 3턴 버퍼 저장
DET_LOGS = deque(maxlen=3)

# 클러스터링 결과값 최대 3턴 버퍼 저장
CLST_LOGS = deque(maxlen=3)

# 웹캠 테스트 촬영 이미지 객체 탐지 결과값 1회 저장
TEST_IMG_DET = deque(maxlen=1)

# 샘플 이미지 클러스터링 결과값 1회 저장
# SAMPLE_IMG_CLST = None #TODO: 현재 값 공유가 되지 않고 있음
SAMPLE_IMG_CLST = deque(maxlen=1)
# -----------------------------------------------------------
# dir
# 디렉토리 파일 저장 경로
# 기본 저장된 sample 이미지

# 샘플 이미지 저장 폴더 경로 및 생성
SAMPLE_IMG_FOLDER = ROOT / 'media/sample_img'
os.makedirs(SAMPLE_IMG_FOLDER, exist_ok=True)

# 객체 탐지 결과 저장 폴더 경로
SAMPLE_DET_FOLDER = ROOT / 'media/detect'

# 이미지 파일 목록
SAMPLE_IMG_FILES = ["photo_1.jpg", "photo_2.jpg", "photo_3.jpg"]

# 텍스트 파일 목록
SAMPLE_TXT_FILES = ["photo_1.txt", "photo_2.txt", "photo_3.txt"]
