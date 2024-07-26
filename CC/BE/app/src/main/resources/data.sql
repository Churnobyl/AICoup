-- 캐릭터 데이터
INSERT IGNORE INTO card_info
(card_info_id,
 name,
 english_name,
 image_url)
VALUES (1,
        '공작',
        'Duke',
        'duke.jpg'),
       (2,
        '사령관',
        'Captain',
        'captain.jpg'),
       (3,
        '암살자',
        'Assassin',
        'assassin.jpg'),
       (4,
        '귀부인',
        'Contessa',
        'contessa.jpg'),
       (5,
        '외교관',
        'Ambassador',
        'ambassador.jpg');

-- 행동 데이터
INSERT IGNORE INTO action
(action_id,
 name,
 english_name,
 description,
 specific_character,
 counteraction_character1,
 counteraction_character2,
 can_be_blocked,
 can_be_challenged)
VALUES (1,
        '수입',
        'Income',
        '1원 획득. 방해할 수 없습니다.',
        0,
        0,
        0,
        false,
        false),
       (2,
        '해외원조',
        'Foreign Aid',
        '2원 획득. 공작으로 방해할 수 있습니다.',
        0,
        1,
        0,
        true,
        true),
       (3,
        '징세',
        'Tax',
        '3원 획득. 방해할 수 없습니다.',
        1,
        0,
        0,
        false,
        true),
       (4,
        '강탈',
        'Steal',
        '다른 플레이어로부터 2원을 빼앗아옵니다. 사령관/외교관으로 방해할 수 있습니다.',
        2,
        2,
        5,
        true,
        true),
       (5,
        '암살',
        'Assassinate',
        '3원 지불. 영향력을 잃게 할 플레이어를 고르세요. 귀부인으로 방해할 수 있습니다.',
        3,
        4,
        0,
        true,
        true),
       (6,
        '교환',
        'Exchange',
        '7원 지불. 영향력을 잃게 할 플레이어를 고르세요. 방해할 수 없습니다.',
        5,
        0,
        0,
        false,
        true),
       (7,
        '쿠',
        'Coup',
        '7원 지불. 영향력을 잃게 할 플레이어를 고르세요. 방해할 수 없습니다.',
        0,
        0,
        0,
        false,
        false),
       (8,
        '도전',
        'Challenge',
        '상대방의 카드에 의심을 품고 공개를 요구합니다.',
        0,
        0,
        0,
        false,
        false),
       (9,
        '뽑기',
        'Draw',
        '카드를 버리고 덱을 섞은 뒤 다시 카드를 뽑습니다.',
        0,
        0,
        0,
        false,
        false),
       (10,
        '승리',
        'Win',
        '게임에서 승리합니다.',
        0,
        0,
        0,
        false,
        false),
       (11,
        '탈락',
        'Lose',
        '게임에서 탈락합니다.',
        0,
        0,
        0,
        false,
        false),
       (12,
        '공개 성공',
        'Successful Open',
        '도전에 성공적으로 카드를 공개합니다',
        0,
        0,
        0,
        false,
        false),
       (13,
        '공개 실패',
        'Fail Open',
        '도전에 카드 공개를 실패합니다.',
        0,
        0,
        0,
        false,
        false),
       (14,
        '행동 성공',
        'Success',
        '어떤 도전에 대해 성공합니다.',
        0,
        0,
        0,
        false,
        false),
       (15,
        '행동 실패',
        'Fail',
        '어떤 도전에 대해 실패합니다.',
        0,
        0,
        0,
        false,
        false),
       (16,
        '두장 뽑기',
        'Draw Double',
        '두장을 뽑습니다.',
        0,
        0,
        0,
        false,
        false),
       (17,
        '게임 시작',
        'Start Game',
        '게임을 시작합니다.',
        0,
        0,
        0,
        false,
        false)