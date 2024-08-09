import matplotlib.pyplot as plt
from multiset import *

from service.clustering import kMeansClustering
from utils_main.vector_util import *
from utils_main.plot_util import plotInformations
import main

# TMP: variables, constances
IMAGE_NUM = 3
## deprecated
# IS_AMB_ACTION = False
# AMB_PLAYER_IDX = 0
CARD_REALLOC_SITUATION = ['amb_pick', 'amb_done', 'ch_win']
PLAYER_INIT_VECTOR = [[0, -1], [-1, 0], [0, 1], [1, 0]]

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
                                "vector": getVectorValuesByCenters([[i['x'], i['y']]])[0],
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
        for k in clusters:
            clusters[k]['vector'] = getVectorValueByCenter(clusters[k]['center'])
        clusterPoints = [clusters[k]['center'] for k in clusters]

        clusterVects  = getVectorValuesByCenters(clusterPoints)
        vectorSizes = getVectorSizes(clusterVects)
        products = [productVector(vec, [1, 0]) for vec in clusterVects]

        angles = [math.acos(p / s) if v[1] > 0 else 2 * math.pi - math.acos(p/s) for p, s, v in zip(products, vectorSizes, clusterVects)]

        degrees = anglesToDegrees(angles)
        degrees = list(enumerate(degrees))
        degrees.sort(key=lambda x : x[1])

        # TODO: cluster 분배 정책이 되는 함수 구현
        player_cluster_id = allocate_players_cluster_id(clusters, deckIdx)
        # player_cluster_id = [cluster_id for (cluster_id, _) in degrees[::-1] if cluster_id != deckIdx]
        print(f'{player_cluster_id=}')

        # 시각화
        plotInformations(cardInfo=cardInfo, clusters=clusters, block=True, show=False, save=True)

        # test_player = [{
        #         "left_card": cardPoints[min([card for card in clusters[id]['cards']], key=lambda x : cardPoints[card]['angle'])]['class_id']
        #     } for id in player_cluster_id]
        # print(test_player)


        # TODO: 이전 정보 가져와서 extra 분류해야함
        # 함수로 아래 빼기. - extra 분류, min/max가 에러 해결, cos 유사 판별
        players, deck = make_players(clusters, cardPoints, player_cluster_id, deckIdx, situation)

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


def sortByLeftToRight(card_list, card_points, cluster_vector):
    cluster_v_n = [-1 * cluster_vector[1], cluster_vector[0]]
    return sorted([card_points[card] for card in card_list], key=lambda x: productVector(cluster_v_n, x['vector']))


def pickRightCardClass(card_list, card_points):
    [a, b] = card_list
    a_v = card_points[a]['vector']
    b_v = card_points[b]['vector']
    mean_v = [(a_v[0] + b_v[0])/2, (a_v[1] + b_v[1])/2]
    mean_v_n = [-1 * mean_v[1], mean_v[0]]
    right_point = a if productVector(a_v, mean_v_n) > 0 else b
    return card_points[right_point]['class_id']


def allocate_players_cluster_id(clusters, deck_idx):
    player_num = main.app.game.playerNum
    preCard = main.app.game.playersCard
    preClusters = [{'center_vector': card['center_vector'], 'similarlity': {}} for card in preCard] if preCard is not None else [{'center_vector': vector, 'similarlity': {}} for vector in PLAYER_INIT_VECTOR]

    for pre_cluster in preClusters:
        for id in clusters:
            if id == deck_idx:
                continue
            # print(pre_cluster['center_vector'])
            # print(clusters[id]['vector'])
            print(pre_cluster)
            pre_cluster['similarlity'][id] = calcCosSimilarlity(pre_cluster['center_vector'], clusters[id]['vector'])


    matchingList = [(p_id, c_id, pre['similarlity'][c_id]) for p_id, pre in enumerate(preClusters) for c_id in pre['similarlity']]
    matchingList.sort(key=lambda x: -x[2])

    player_table = dict()
    selected = []
    for matching in matchingList:
        if not matching[0] in player_table.keys():
            if matching[1] in selected:
                continue
            player_table[matching[0]] = matching[1:]
            selected.append(matching[1])
            print(player_table)
        if len(player_table) == player_num:
            break
    
    ret = []
    for idx in range(player_num):
        ret.append(player_table[idx][0])
    
    return ret


# TODO: 해야함.
def make_players(clusters, cardPoints, player_cluster_id, deck_idx, action):
    pre_cards_class = main.app.game.playersCard
    if main.app.game.playersCard is not None:
        pre_cards_class = [[pre['left_card'], pre['right_card']] for pre in pre_cards_class]

    players = []
    for index, id in enumerate(player_cluster_id):
        print('player_id: ', id)
        cards = sortByLeftToRight([card for card in clusters[id]['cards']], cardPoints, clusters[id]['vector']) # TODO: 'center' 고치자
        left_card = cards[0]['class_id'] # TODO: 위 함수 enumerate 필요한가? indic 바꾸기
        right_card = cards[1]['class_id']
        extra = []
        # action에 amb_pick보고 extra 만들기
        # card 객체로 다시 뽑아야함... 생각보다 긴 코드가 될 듯
        if index == action['player_id'] and action['name'] == 'amb_pick' and pre_cards_class is not None:    # TODO: 뒤 condition error raise로 아래로 빼주기
            amb_player = action['player_id']
            print(f'{clusters=}')
            amb_cards = clusters[player_cluster_id[amb_player]]['cards']   # TODO: player_id => clusters index로 바꿔서 넣어야함
            amb_hand_cards = []
            print(f'{cards=}')
            print(f'{amb_cards=}')
            print(f'{[cardPoints[ix]["class_id"] for ix in amb_cards]=}')
            # amb pre point
            for card in cards:      # 꼴보기 싫음. 코드 바꿔야함 fuxxin TODO ㅗㅗ
                print('pick: --', card)
                if card['class_id'] in [cardPoints[ix]['class_id'] for ix in amb_cards]:
                    amb_hand_cards.append(card)
                if len(amb_hand_cards) == 2:
                    break
            # amb_pre = [card for card in amb_cards if card['class_id'] in pre_cards_class] # 이거 안됌... TODO: 바꾸기 - 1 두개면 상황에 따라 logic error
            extra = list(Multiset([card['class_id'] for card in cards])
                        - Multiset([pre for pre in pre_cards_class[amb_player]]))
            print(f'{amb_hand_cards=}')
            left_card = amb_hand_cards[0]['class_id']
            right_card = amb_hand_cards[1]['class_id']
        players.append({
            "cards": cards,
            "left_card": left_card,
            "right_card": right_card,
            "extra_card": extra,
            "center_vector": getVectorValueByCenter(clusters[id]['center'])
        })

    # players = [{
    #             "cards": [{"id": card, "class": cardPoints[card]['class_id']} for card in clusters[id]['cards']],
    #             # TODO: 함수로 빼기 + 각도 0 근처에서 값 반전 고려
    #             "left_card": pickLeftCardClass([card for card in clusters[id]['cards']], cardPoints),
    #             "right_card": pickRightCardClass([card for card in clusters[id]['cards']], cardPoints),
    #             "extra_card": [],
    #             "center_point": clusters[id]['center']
    #         } for id in player_cluster_id]
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
            # print(f'{pre=}')
            # print(f'{cur=}')
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