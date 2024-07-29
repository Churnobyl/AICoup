import sys
import os

from fastapi import HTTPException

from core import *

sys.path.append(os.path.join(os.path.abspath(os.path.dirname(os.path.abspath(os.path.dirname(__file__)))), 'yolov9'))

# from yolov9.models import common
from detect_dual import run

def inference():
    detections = [] 
    try:
        # run 함수 직접 호출
        detections = run(
            weights=WEIGHTS,
            source=IMG_FOLDER,
            # nosave=True # txt, img 저장 안 함 
            project = DET_FOLDER, #객체 탐지 결과 파일 저장 경로
            save_txt=True,  # 객체 탐지 결과 txt 파일 저장
            save_conf=True  # 객체 탐지 결과 img 파일 저장
        )

        if not detections:
            raise HTTPException(status_code=404, detail="탐지 결과를 찾을 수 없습니다.")

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"An error occurred: {str(e)}")
    
    return detections