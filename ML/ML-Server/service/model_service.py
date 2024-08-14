from fastapi import HTTPException
import sys
import os
from core import *
from detect_dual import run

current_file_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_file_dir)
target_dir = os.path.join(parent_dir, 'yolov9')
sys.path.append(target_dir)


async def inference(path, buffers=None):
    print("inference() 실행")
    detections = []
    try:
        match path:
            case "buffers":
                # 버퍼 이미지 불러오기 및 저장
                if buffers is None:
                    buffers = CAP_IMG_BUFFERS  # 디폴트 버퍼

                detections = run(
                    weights=WEIGHTS,
                    save_conf=True,  # 객체 탐지 정확도 저장
                    buffer_path=buffers  # TEST_IMG_BUFFERS | CONF_IMG_BUFFERS
                )

                if not detections:
                    raise HTTPException(
                        status_code=404, detail="탐지 결과를 찾을 수 없습니다.")

                if buffers == TEST_CAP_IMG_BUFFERS:
                    TEST_IMG_DET.append(detections)  # 1회 저장
                    print("inference() 종료")
                    # yolo 테스트 시, 성공 msg만 간단히 반환
                    return {"객체 탐지 성공"}

            case "sample_dir":
                # 디렉토리 이미지 불러오기 및 저장
                detections = run(
                    weights=WEIGHTS,
                    save_conf=True,  # 객체 탐지 정확도 저장
                    # -----------------------------------------------------------
                    source=SAMPLE_IMG_FOLDER,  # media 디렉토리 활용
                    project=SAMPLE_DET_FOLDER,  # 객체 탐지 결과 파일 저장 경로
                    save_txt=True,  # 객체 탐지 결과 txt 파일 저장
                )
                if not detections:
                    raise HTTPException(
                        status_code=404, detail="탐지 결과를 찾을 수 없습니다.")

                print("inference() 종료")
                # yolo 샘플 테스트 시, 성공 msg만 간단히 반환
                return {"sample 이미지 객체 탐지 성공"}

    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"An error occurred: {str(e)}")

    print("inference() 종료")
    # board.get_game_status() 실행 시, 탐지 결과값 detections 반환
    return detections
