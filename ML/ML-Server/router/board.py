from fastapi import APIRouter
from fastapi.responses import JSONResponse
from typing import Optional

from service import *
from utils_main import *
import main

TRIAL = 4
router = APIRouter(
    prefix='/api/board'
)

@router.post('game-start')
async def game_start(player_num: int):
    main.app.game.game_start(player_num)

# -----------------------------------------------------------
# 객체 클러스터링으로 데이터 정제 후, CC에 JSON 형태로 전송
# 작업 중


@router.get('/raw-status')
async def get_status_raw():
    return JSONResponse(main.app.game.__dict__)


@router.post('/game-status')
async def get_game_status(situate: Optional[dict]):
    # TODO: return error message
    if not main.app.game.running:
        return

    for tried in range(TRIAL):
        print("inference try... ", tried)
        results = await convert_to_dict(inference("dir"))
        result = tracePlayers(results)
        if result is not None:
            main.app.game.playersCard = result[0]
            main.app.game.deckCard = result[1]
            result = {
                "user_card": result[0],
                "deck_card": result[1]
            }
            return JSONResponse(result)
    return

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
