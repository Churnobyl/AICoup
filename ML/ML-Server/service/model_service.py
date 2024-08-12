from fastapi import HTTPException
import sys
import os
from core import *
from detect_dual import run

current_file_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_file_dir)
target_dir = os.path.join(parent_dir, 'yolov9')
sys.path.append(target_dir)

# from yolov9.models import common


async def inference(path):
    detections = []
    try:
        match path:
            # 메모리 버퍼 이미지 불러오기 및 저장
            case "buffers":
                # run 함수 직접 호출
                detections = run(
                    weights=WEIGHTS,
                    # nosave=False,# 디폴트 # B박스를 생성한 img 파일 저장
                    save_conf=True  # 객체 탐지 정확도 저장
                )
                
                if not detections:
                    raise HTTPException(status_code=404, detail="탐지 결과를 찾을 수 없습니다.")
                
                # 객체 탐지 결과 메모리 버퍼에 저장
                DET_LOGS.append(detections)
                print(f"전역 변수 DET_QUEUE에 탐지 결과값 저장. {len(DET_LOGS)}/ 3(maxlen)")
            
            case "dir":
                # 디렉토리 이미지 불러오기 및 저장
                detections = run(
                    weights=WEIGHTS,
                    # nosave=False,# 디폴트 # B박스를 생성한 img 파일 저장
                    save_conf=True,  # 객체 탐지 정확도 저장
                    # -----------------------------------------------------------
                    # media 디렉토리 활용
                    source=IMG_FOLDER,
                    project = DET_FOLDER, #객체 탐지 결과 파일 저장 경로
                    save_txt=True,  # 객체 탐지 결과 txt 파일 저장
                )
                if not detections:
                    raise HTTPException(status_code=404, detail="탐지 결과를 찾을 수 없습니다.")
                
                print("media 디렉토리에 탐지 결과 txt 파일로 저장")

    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"An error occurred: {str(e)}")

    return detections
