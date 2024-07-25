import os
import time
import cv2

from config import *

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
        time.sleep(0.5)  # Wait for a second before capturing the next image

    # Release the webcam
    cap.release()
    global IMG_CHECK 
    IMG_CHECK = True # 사진이 무사히 찍혔다면 1
    print("capture_photos() 종료")
    return {"message": "Photos captured successfully."}