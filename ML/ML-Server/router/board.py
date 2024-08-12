from fastapi import APIRouter, HTTPException
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
    return JSONResponse({"success": "good"})

# -----------------------------------------------------------
# 객체 클러스터링으로 데이터 정제 후, CC에 JSON 형태로 전송
# 작업 중


@router.get('/raw-status')
async def get_status_raw():
    return JSONResponse(main.app.game.__dict__)

# situation format...
# {
#     'name': str,
#     'player_id': Optional[int]
# }
@router.post('/game-status')
async def get_game_status(situation: Optional[dict]):
    # TODO: return error message
    if not main.app.game.running:
        raise HTTPException(status_code=403, detail='Game is not running')

    exceptLog = []

    for tried in range(TRIAL):
        print("inference try... ", tried)
        try:
            results = await convert_to_dict(await inference("dir"))
            result = tracePlayers(results, situation)
            if result is not None:
                main.app.game.playersCard = result[0]
                main.app.game.deckCard = result[1]
                result = {
                    "user_card": result[0],
                    "deck_card": result[1]
                }
                return JSONResponse(result)
        except Exception as e:
            exceptLog.append(e)
    raise HTTPException(status_code=404, detail={'cause': 'Over trial...', 'exceptLog': str(exceptLog)})

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
