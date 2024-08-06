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

from router import *
from core.capture_config import get_capture_manager
from core import status

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Singleton
    # 애플리케이션 시작 시 CaptureManager 인스턴스 생성
    cap = get_capture_manager()
    app.game = status.GameStatus()
    yield
    # 애플리케이션 종료 시 저장된 인스턴스의 release 메서드 호출
    cap.release()

app = FastAPI(lifespan=lifespan)

#---------------------------------------------------------------------
app.include_router(debug.router)
app.include_router(board.router)
app.include_router(sample_test.router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)