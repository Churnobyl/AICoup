# 서버 통신용 api
# buffer의 이미지 사용
# game 시작
# 객체 탐지, 클러스터링

from fastapi import APIRouter, HTTPException
from fastapi.responses import JSONResponse
from typing import Optional

from service import *
from utils_main import *
import main

router = APIRouter(
    prefix='/api/board'
)

# 객체 탐지 + 클러스터링 시도 횟수 (임의 지정)
TRIAL = 2

# -----------------------------------------------------------
# 게임 시작


@router.post('game-start')
async def game_start(player_num: int):  # 플레이어 인원 쿼리 입력
    """_main.app.game 시작_

    Args:
        player_num (int): _플레이어 총 참여인원_

    Returns:
        _message_: _game 시작 성공 여부_
    """
    main.app.game.game_start(player_num)
    return JSONResponse({"success": "good"})

# -----------------------------------------------------------
# 보드게임 객체 조회

# situation format...
# {
#     'name': str,
#     'player_id': Optional[int]
# }


@router.post('/game-status')
async def get_game_status(situation: Optional[dict]):
    """_보드게임 카드 이미지 촬영 및 객체 인식, 결과값 누적 3회 저장_

    Args:
        situation (Optional[dict]): _['amb_pick', 'amb_done', 'ch_win']_

    Raises:
        HTTPException: _게임 시작이 선행되지 않았을 시_,
        HTTPException: _클러스터링 예외 발생 시_

    Returns:
        _tuple_: _객체 탐지 클러스터링 결과값, extra 카드 포함_
    """

    # 실행된 게임이 없다면,
    if not main.app.game.running:
        raise HTTPException(status_code=403, detail='Game is not running')

    print("get_game_status() 실행")

    # 이미지 촬영 (버퍼 임시 저장)
    await media_service.capture_images()

    exceptLog = []  # 예외로그
    # 이미지 객체 탐지 + 클러스터링 반복
    for tried in range(TRIAL):
        print("inference try... ", tried)
        try:
            # 이미지 yolo 객체 탐지 결과 # 버퍼 이미지 디폴트
            detection_results = await convert_to_dict(await inference("buffers"))

            # 이미지 클러스터링 결과
            clustered_result = tracePlayers(detection_results, situation)

            if clustered_result is not None:
                main.app.game.playersCard = clustered_result[0]
                main.app.game.deckCard = clustered_result[1]
                clustered_result = {
                    "user_card": clustered_result[0],
                    "deck_card": clustered_result[1]
                }

                # 전역 변수 덱큐에 결과값 누적 최대 3회 저장
                if tried == (TRIAL-1):
                    DET_LOGS.append(detection_results)
                    CLST_LOGS.append(clustered_result)

                print("get_game_status() 종료")
                return JSONResponse(clustered_result)

        except Exception as e:
            exceptLog.append(e)

    raise HTTPException(status_code=404, detail={
                        'cause': 'Over trial...', 'exceptLog': str(exceptLog)})
