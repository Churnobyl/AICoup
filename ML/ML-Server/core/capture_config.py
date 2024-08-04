import cv2


class CaptureManager:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            self = super(CaptureManager, cls).__new__(cls)
            self.cap = cv2.VideoCapture(0)
            print("웹캠 연결")

            # 프레임 사이즈 HD(1280x720)로 설정
            self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
            self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)
            print("웹캠 프레임 설정 완료")

            # 프레임 사이즈 출력
            width = self.cap.get(cv2.CAP_PROP_FRAME_WIDTH)
            height = self.cap.get(cv2.CAP_PROP_FRAME_HEIGHT)
            print(f'Frame size: {width}x{height}')

            if not self.cap.isOpened():
                raise Exception("Could not open webcam.")

        cls._instance = self
        print("웹캠 초기화 완료")
        return cls._instance

    def get_frame(self):
        ret, frame = self.cap.read()
        if not ret:
            raise Exception("촬영 실패.")
        return frame

    def release(self):
        self.cap.release()
        print("웹캠 연결 해제")

# -----------------------------------------------------------


# 전역 인스턴스 변수
capture_manager_instance = None

# Singleton


def get_capture_manager():
    global capture_manager_instance
    if capture_manager_instance is None:
        capture_manager_instance = CaptureManager()
    return capture_manager_instance
