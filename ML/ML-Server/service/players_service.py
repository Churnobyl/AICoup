import matplotlib.pyplot as plt

from service.clustering import kMeansClustering
from utils_main.vector_util import *
from utils_main.plot_util import plotInformations

def tracePlayers(inferResult):
    # data 변환

    # 임의로 리스트 하나만 남겨서 0의 인덱스로 접근 한 모습
    ## TODO: 이 부분 구현해야함
    ## TODO: BBOX 말도 안되는 포인트 제거 하는게 좋아보임
    cardInfo = inferResult[0]['detections']
    cardInfo = [obj for obj in cardInfo if obj['class_id'] < 6]

    image_idx, cardInfo = preValidCard(inferResult)
    if cardInfo is None:
        return None
    # 먼저 카드의 개수로 valid
    

    cardPoints = dict(enumerate([{
                            "class_id": i['class_id'],
                            "center": (i['x'], i['y']),
                            "vector": getVectorValueByCenter([[i['x'], i['y']]])[0],
                            "cluster": -1
                            }
                            for i in cardInfo]))
    for card in cardPoints:
        # angle = getAngleByVector(cardPoints[card]['vector'])
        angle = math.acos(cardPoints[card]['vector'][0] / getVectorSize(cardPoints[card]['vector']))
        angle = angle if cardPoints[card]['vector'][1] > 0 else 2 * math.pi - angle
        cardPoints[card]['angle'] = angle

    # 클러스터링
    N = 5   # tmp
    clusters = kMeansClustering(N, cardPoints)

    # TODO: 덱 카드와 플레이어 카드 분리

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
    player_cluster_id = [cluster_id for (cluster_id, _) in degrees]

    # 시각화
    plotInformations(cardInfo=cardInfo, clusters=clusters, block=True, save=True)

    # test_player = [{
    #         "left_card": cardPoints[min([card for card in clusters[id]['cards']], key=lambda x : cardPoints[card]['angle'])]['class_id']
    #     } for id in player_cluster_id]
    # print(test_player)


    # TODO: 이전 정보 가져와서 extra 분류해야함
    players = [{
            "cards": [{"id": card, "class": cardPoints[card]['class_id']} for card in clusters[id]['cards']],
            # TODO: 함수로 빼기 + 각도 0 근처에서 값 반전 고려
            "left_card": cardPoints[min([card for card in clusters[id]['cards']], key=lambda x : cardPoints[x]['angle'])]['class_id'],
            "right_card": cardPoints[max([card for card in clusters[id]['cards']], key=lambda x : cardPoints[x]['angle'])]['class_id'],
            "extra_card": [],
            "vector_value": clusterVects[id]
        } for id in player_cluster_id[::-1]
    ]

    # validation
    # TODO: validation 부분 구현
    postValidCard()
    
    print(players)
    return players


def preValidCard(infers, s_idx=0):
    cardInfo = []
    for idx in enumerate(s_idx, infers):
        cardInfo = infers[idx]
        cardInfo = [obj for obj in cardInfo if obj['class_id'] < 6]
        if len(cardInfo):
            return idx, cardInfo

    return idx, None


def postValidCard():
    pass


def calcCardsNum():
    pass
