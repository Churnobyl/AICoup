from fastapi import APIRouter
from fastapi.responses import JSONResponse

from service import *
from utils_main import *

router = APIRouter(
    prefix='/api/board'
)

# -----------------------------------------------------------
# 객체 클러스터링으로 데이터 정제 후, CC에 JSON 형태로 전송
# 작업 중
@router.get('/game-status')
async def getGameStatus():
    pass

# -----------------------------------------------------------
# 더미 데이터 전송
test = [
    {
        "left_card": 4,
        "right_card": 3,
        "extra_card": []
    },
    {
        "left_card": 1,
        "right_card": 2,
        "extra_card": []
    },
    {
        'left_card': 5,
        'right_card': 4,
        'extra_card': []
    },
    {
        'left_card': 1,
        'right_card': 2,
        'extra_card': []
    }
]

@router.get('/game-status/test')
async def getGameStatus():
    results = test
    return JSONResponse(content=results)