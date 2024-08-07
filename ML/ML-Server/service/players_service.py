import matplotlib.pyplot as plt
from multiset import *

from service.clustering import kMeansClustering
from utils_main.vector_util import *
from utils_main.plot_util import plotInformations
import main

# TMP: variables, constances
IMAGE_NUM = 3
IS_AMB_ACTION = False
AMB_PLAYER_IDX = 0
CARD_REALLOC_SITUATION = ['amb_pick', 'amb_done', 'ch_win']

# TODO: ambassador action param
def tracePlayers(inferResult, situation):
    # data 변환

    # 임의로 리스트 하나만 남겨서 0의 인덱스로 접근 한 모습
    ## TODO: 이 부분 구현해야함
    
    ## 이전 코드
    # cardInfo = inferResult[0]['detections']
    # cardInfo = [obj for obj in cardInfo if obj['class_id'] < 6]

    image_idx = 0
    # 먼저 카드의 개수로 valid
    while image_idx < IMAGE_NUM:
        # TODO: api name을 무조건 박을지 결정 일단 코드는 남겨둠...
        # action = situation['name'] if situation.get('name') else None
        image_idx, cardInfo = preValidCard(inferResult, s_idx=image_idx, is_amb_action=situation['name']=='amb_pick')
        if cardInfo is None:
            print("Failed pre Valid...")
            image_idx += 1
            continue
        print(f'{image_idx=}')

        cardPoints = dict(enumerate([{
                                "class_id": i['class_id'],
                                "center": (i['x'], i['y']),
                                "vector": getVectorValueByCenter([[i['x'], i['y']]])[0],
                                "cluster": -1
                                }
                                for i in cardInfo]))
        # TODO: it can be depracted
        for card in cardPoints:
            # angle = getAngleByVector(cardPoints[card]['vector'])
            angle = math.acos(cardPoints[card]['vector'][0] / getVectorSize(cardPoints[card]['vector']))
            angle = angle if cardPoints[card]['vector'][1] > 0 else 2 * math.pi - angle
            cardPoints[card]['angle'] = angle

        # 클러스터링
        N = main.app.game.playerNum + 1   # tmp
        clusters = kMeansClustering(N, cardPoints)
        # 클러스터 검증 및 덱, 유저 인덱스 반환
        deckIdx, usersIdx = clusterValid(clusters)
        if deckIdx is None:
            print("Failed clustering Valid...")
            image_idx += 1
            continue

        # 벡터변환 및 플레이어 할당
        clusterPoints = [clusters[k]['center'] for k in clusters]

        clusterVects  = getVectorValueByCenter(clusterPoints)
        vectorSizes = getVectorSizes(clusterVects)
        products = [productVector(vec, [1, 0]) for vec in clusterVects]

        angles = [math.acos(p / s) if v[1] > 0 else 2 * math.pi - math.acos(p/s) for p, s, v in zip(products, vectorSizes, clusterVects)]

        degrees = anglesToDegrees(angles)
        degrees = list(enumerate(degrees))
        degrees.sort(key=lambda x : x[1])

        # TODO: cluster 분배 정책이 되는 함수 구현
        player_cluster_id = [cluster_id for (cluster_id, _) in degrees[::-1] if cluster_id != deckIdx]

        # 시각화
        plotInformations(cardInfo=cardInfo, clusters=clusters, block=True, show=False, save=True)

        # test_player = [{
        #         "left_card": cardPoints[min([card for card in clusters[id]['cards']], key=lambda x : cardPoints[card]['angle'])]['class_id']
        #     } for id in player_cluster_id]
        # print(test_player)


        # TODO: 이전 정보 가져와서 extra 분류해야함
        # 함수로 아래 빼기. - extra 분류, min/max가 에러 해결, cos 유사 판별
        players = [{
                "cards": [{"id": card, "class": cardPoints[card]['class_id']} for card in clusters[id]['cards']],
                # TODO: 함수로 빼기 + 각도 0 근처에서 값 반전 고려
                "left_card": pickLeftCardClass([card for card in clusters[id]['cards']], cardPoints),
                "right_card": pickRightCardClass([card for card in clusters[id]['cards']], cardPoints),
                "extra_card": [],
                "center_point": clusters[id]['center']
            } for id in player_cluster_id
        ]
        deck = {
            "cards": [{"id": card, "class": cardPoints[card]['class_id']} for card in clusters[deckIdx]['cards']],
            "center_point": clusters[deckIdx]['center']
        }

        # validation
        # TODO: validation 부분 구현
        if not postValidCard(players, situation):
            print('Failed validation in post valid')
            return None

        return (players, deck)
    
    return None

def pickLeftCardClass(card_list, card_points):
    [a, b] = card_list
    a_v = card_points[a]['vector']
    b_v = card_points[b]['vector']
    mean_v = [(a_v[0] + b_v[0])/2, (a_v[1] + b_v[1])/2]
    mean_v_n = [-1 * mean_v[1], mean_v[0]]
    left_point = b if productVector(a_v, mean_v_n) > 0 else a
    return card_points[left_point]['class_id']


def pickRightCardClass(card_list, card_points):
    [a, b] = card_list
    a_v = card_points[a]['vector']
    b_v = card_points[b]['vector']
    mean_v = [(a_v[0] + b_v[0])/2, (a_v[1] + b_v[1])/2]
    mean_v_n = [-1 * mean_v[1], mean_v[0]]
    right_point = a if productVector(a_v, mean_v_n) > 0 else b
    return card_points[right_point]['class_id']


def allocate_players_cluster_id(cluster_degree, deck_idx):
    # firstInfer를 보고 벡터 유사도로 분배하기
    pass


# TODO: 해야함.
def make_players(clusters, cardPoints, player_cluster_id, deck_idx, action):
    # firstInfer를 보고 precard 뽑아내기
    pre_cards = main.app.game.playersCard
    pre_cards = [[pre_cards[id]['left_card'], pre_cards[id]['right_card']] for id in player_cluster_id]

    # action에 amb_pick보고 extra 만들기
    extras = [
              list(Multiset(cardPoints[card]['class_id']) - Multiset(pre))
              for id in player_cluster_id
              for card in clusters[id]['cards']
              for pre in pre_cards
              ]

    players = [{
                "cards": [{"id": card, "class": cardPoints[card]['class_id']} for card in clusters[id]['cards']],
                # TODO: 함수로 빼기 + 각도 0 근처에서 값 반전 고려
                "left_card": pickLeftCardClass([card for card in clusters[id]['cards']], cardPoints),
                "right_card": pickRightCardClass([card for card in clusters[id]['cards']], cardPoints),
                "extra_card": [],
                "center_point": clusters[id]['center']
            } for id in player_cluster_id
        ]
    deck = {
            "cards": [{"id": card, "class": cardPoints[card]['class_id']} for card in clusters[deck_idx]['cards']],
            "center_point": clusters[deck_idx]['center']
        }
    return players, deck


def preValidCard(infers, s_idx=0, is_amb_action=False):
    cardInfo = []
    for idx in range(s_idx, IMAGE_NUM):
        cardInfo = infers[idx]['detections']
        cardInfo = [obj for obj in cardInfo if obj['class_id'] < 6]
        if calcCardsNum(len(cardInfo), is_amb_action):
            print(f'{len(cardInfo)=}')
            return idx, cardInfo

    return s_idx, None


def clusterValid(clusters):
    deck = None
    user = []
    for idx in clusters:
        if len(clusters[idx]['cards']) == 1:
            if deck is not None:
                print("clustering error - double deck")
                return None, None
            deck = idx
        elif len(clusters[idx]['cards']) == 2 or len(clusters[idx]['cards']) == 4:
            user.append(idx)
        else:
            print("clustering error - invalid card num")
            return None, None

    return deck, user


def postValidCard(players, situation):
    if main.app.game.playersCard is not None:
        for idx, (pre, cur) in enumerate(zip(main.app.game.playersCard, players)):
            if situation['name'] in CARD_REALLOC_SITUATION and idx == situation['player_id']:
                continue
            if pre['left_card'] != cur['left_card']:
                return False
            if pre['right_card'] != cur['right_card']:
                return False
    return True


def calcCardsNum(cardNum, is_amb_action):
    validNum = main.app.game.playerNum * 2 + 1
    if is_amb_action:
        validNum += 2
    return cardNum == validNum