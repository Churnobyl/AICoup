# 어플리케이션 시작
# 웹캠 싱글턴 생성
# 게임 생성

# 외부 모듈
from fastapi import FastAPI
from contextlib import asynccontextmanager
import os
import sys

# sys.path.append(os.path.join(os.path.dirname(os.path.abspath(os.path.dirname(__file__))), 'yolov9'))
# 위 코드 줄바꿈하면 ModuleNotFoundError: No module named 'detect_dual 에러 발생
current_file_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_file_dir)
target_dir = os.path.join(parent_dir, 'yolov9')
sys.path.append(target_dir)

# 내부 모듈 # 모듈 import 선언 순서 중요
from core import status
from core.capture_config import get_capture_manager
from router import *

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("app 실행")
    cap = get_capture_manager()  # 웹캠 연결 # Singleton
    app.game = status.GameStatus()  # game 생성

    yield

    print("app 종료")
    cap.release()  # 웹캠 연결 해제

app = FastAPI(lifespan=lifespan)

# 라우터 연결
app.include_router(debug.router)
app.include_router(board.router)
app.include_router(sample.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
