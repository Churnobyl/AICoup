-- 캐릭터 데이터
INSERT IGNORE INTO card_info
(card_info_id,
 name,
 english_name,
 image_url)
VALUES (1,
        '공작',
        'duke',
        'duke.jpg'),
       (2,
        '사령관',
        'captain',
        'captain.jpg'),
       (3,
        '암살자',
        'assassin',
        'assassin.jpg'),
       (4,
        '귀부인',
        'contessa',
        'contessa.jpg'),
       (5,
        '외교관',
        'ambassador',
        'ambassador.jpg');

-- 가능한 액션 데이터
INSERT IGNORE INTO possible_action
(possible_action_id,
 action_id,
 can_action_id)
VALUES
    (
        1,
        2,
        23
    ),
    (
        2,
        2,
        19
    ),
    (
        3,
        3,
        23
    ),
    (
        4,
        3,
        8
    ),
    (
        5,
        4,
        23
    ),
    (
        6,
        4,
        20
    ),
    (
        7,
        4,
        21
    ),
    (
        8,
        4,
        8
    ),
    (
        9,
        5,
        23
    ),
    (
        10,
        5,
        22
    ),
    (
        11,
        5,
        8
    ),
    (
        12,
        6,
        23
    ),
    (
        13,
        6,
        8
    ),
    (
        14,
        19,
        8
    ),
    (
        15,
        20,
        8
    ),
    (
        16,
        21,
        8
    ),
    (
        17,
        22,
        8
    );

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
        'income',
        '1원 획득. 방해할 수 없습니다.',
        0,
        0,
        0,
        false,
        false),
       (2,
        '해외원조',
        'foreign_aid',
        '2원 획득. 공작으로 방해할 수 있습니다.',
        0,
        1,
        0,
        true,
        false),
       (3,
        '징세',
        'tax',
        '3원 획득. 방해할 수 없습니다.',
        1,
        0,
        0,
        false,
        true),
       (4,
        '강탈',
        'steal',
        '다른 플레이어로부터 2원을 빼앗아옵니다. 사령관/외교관으로 방해할 수 있습니다.',
        2,
        2,
        5,
        true,
        true),
       (5,
        '암살',
        'assassinate',
        '3원 지불. 영향력을 잃게 할 플레이어를 고르세요. 귀부인으로 방해할 수 있습니다.',
        3,
        4,
        0,
        true,
        true),
       (6,
        '교환',
        'exchange',
        '덱의 카드 2장과 원하는 카드를 교환. 방해할 수 없습니다.',
        5,
        0,
        0,
        false,
        true),
       (7,
        '쿠',
        'coup',
        '7원 지불. 영향력을 잃게 할 플레이어를 고르세요. 방해할 수 없습니다.',
        0,
        0,
        0,
        false,
        false),
       (8,
        '을 갖고 있다는 주장에 도전합니다.',
        'challenge',
        '상대방이 어떤 카드를 가지고 있다는 것에 의심을 품고 도전합니다.',
        0,
        0,
        0,
        false,
        false),
       (9,
        '허용',
        'permit',
        '상대방의 행동을 허용합니다.',
        0,
        0,
        0,
        false,
        false),
       (10,
        '방해',
        'block_duke',
        '공작으로 방해합니다.',
        0,
        0,
        0,
        false,
        false),
       (11,
        '방해',
        'block_captain',
        '사령관으로 방해합니다.',
        0,
        0,
        0,
        false,
        false),
       (12,
        '방해',
        'block_ambassador',
        '대사로 방해합니다.',
        0,
        0,
        0,
        false,
        false),
       (13,
        '방해',
        'block_contessa',
        '귀부인으로 방해합니다.',
        0,
        0,
        0,
        false,
        false),

       (17,
        '게임 생성',
        'Game Init',
        '게임이 생성되었습니다.',
        0,
        0,
        0,
        false,
        false),
       (18,
        '플레이어 턴',
        'Player Turn',
        '플레이어의 턴입니다.',
        0,
        0,
        0,
        false,
        false);

INSERT IGNORE INTO aicoup.game_data (id, player_num, current_player, playerinfo, history, current_action, target,
                                     counter_actioner, counter_action)
VALUES (1, 4, 1, '[
               {
                   "1": {"cards": ["duke", "captain"], "cards_open": [false, false], "coins": 2},
                   "2": {"cards": ["assassin", "captain"], "cards_open": [false, false], "coins": 2},
                   "3": {"cards": ["ambassador", "ambassador"], "cards_open": [false, false], "coins": 2},
                   "4": {"cards": ["assassin", "contessa"], "cards_open": [false, false], "coins": 2}
               }
           ]', '{
               "1": [],
               "2": [],
               "3": [],
               "4": []
           }', null, null, null, null),
       (2, 4, 1, '[
               {
                   "1": {"cards": ["duke", "captain"], "cards_open": [false, false], "coins": 2},
                   "2": {"cards": ["assassin", "captain"], "cards_open": [false, false], "coins": 2},
                   "3": {"cards": ["ambassador", "ambassador"], "cards_open": [false, false], "coins": 2},
                   "4": {"cards": ["assassin", "contessa"], "cards_open": [false, false], "coins": 2}
               }
           ]', '{
               "1": [],
               "2": [],
               "3": [],
               "4": []
           }', 'tax', 'none', null, null),
       (3, 4, 2, '[
               {
                   "1": {"cards": ["duke", "captain"], "cards_open": [false, false], "coins": 5},
                   "2": {"cards": ["assassin", "captain"], "cards_open": [false, false], "coins": 2},
                   "3": {"cards": ["ambassador", "ambassador"], "cards_open": [false, false], "coins": 2},
                   "4": {"cards": ["assassin", "contessa"], "cards_open": [false, false], "coins": 2}
               }
           ]', '{
               "1": ["tax"],
               "2": [],
               "3": [],
               "4": []
           }', 'steal', '1', null, null),
       (4, 4, 2, '[
               {
                   "1": {"cards": ["duke", "captain"], "cards_open": [false, false], "coins": 5},
                   "2": {"cards": ["assassin", "captain"], "cards_open": [false, false], "coins": 2},
                   "3": {"cards": ["ambassador", "ambassador"], "cards_open": [false, false], "coins": 2},
                   "4": {"cards": ["assassin", "contessa"], "cards_open": [false, false], "coins": 2}
               }
           ]', '{
               "1": ["tax"],
               "2": [],
               "3": [],
               "4": []
           }', null, null, '1', 'captain');