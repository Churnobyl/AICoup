from fastapi import APIRouter
from fastapi.responses import JSONResponse

from service import *
from utils_main import *

TRIAL = 4
router = APIRouter(
    prefix='/api/board'
)

@router.get('game-start')
async def game_start():
    pass

# -----------------------------------------------------------
# 객체 클러스터링으로 데이터 정제 후, CC에 JSON 형태로 전송
# 작업 중


@router.get('/game-status')
async def get_game_status():
    results = await convert_to_dict(inference("dir"))
    result = tracePlayers(results)
    for tried in range(TRIAL):
        if result is not None:
            break
    return JSONResponse(result)

@router.get('/game-satatus/image')
async def get_board_image():
    return 

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
async def get_game_status():
    results = test
    return JSONResponse(content=results)
