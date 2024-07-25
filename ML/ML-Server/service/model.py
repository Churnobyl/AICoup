import sys
import os

from fastapi import HTTPException

from config import *

sys.path.append(os.path.join(os.path.abspath(os.path.dirname(os.path.abspath(os.path.dirname(__file__)))), 'yolov9'))

# from yolov9.models import common
from utils.torch_utils import select_device
from detect_dual import run

def inference():
    detections = [] 
    try:
        # run 함수 직접 호출
        detections = run(
            weights=WEIGHTS,
            source=IMG_FOLDER,
            nosave=True # txt, img 저장 안 함 
        )

        if not detections:
            raise HTTPException(status_code=404, detail="탐지 결과를 찾을 수 없습니다.")

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")
    
    return detections