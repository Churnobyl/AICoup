from pathlib import Path
from collections import deque

# ML-Server root directory
FILE = Path(__file__).resolve()
ROOT = FILE.parents[1]  

# 모델 가중치 파일 경로
WEIGHTS = ROOT / 'core/coup-0730.pt'  
DEVICE = ''

# 최신 촬영 이미지 3장 저장
CAP_IMG_BUFFERS = deque(maxlen=3)

# 최신 탐지 결과 이미지 3장 저장
CONF_IMG_BUFFERS = deque(maxlen=3)

# 객체 탐지 결과값 최대 3턴 저장
DET_LOGS = deque(maxlen=3)

# 클러스터링 중간 결과값
test = {0: {'center': [0.5473437547683716, 0.15111111104488373], 'points': []},
            1: {'center': [0.5078125, 0.8891203701496124], 'points': []},
            2: {'center': [0.9234375059604645, 0.577777773141861], 'points': []},
            3: {'center': [0.11575520783662796, 0.4349537193775177], 'points': []},
            4: {'center': [0.46041667461395264, 0.5439814925193787], 'points': []}}

CLST_DATA = test

