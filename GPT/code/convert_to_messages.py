import json

# 주어진 데이터를 리스트로 정의합니다.
data = []
data_from_jsonl = []

# JSONL 파일에서 데이터를 읽어옴
with open('game/game10/action_10.jsonl', 'r', encoding='utf-8') as f:
    json_object = ''
    open_braces = 0
    for line in f:
        line = line.strip()
        json_object += line
        
        # 중괄호 개수 세기
        open_braces += line.count('{')
        open_braces -= line.count('}')
        
        # 중괄호가 모두 닫힌 경우, JSON 객체의 끝으로 판단
        if open_braces == 0:
            if json_object.endswith(','):
                json_object = json_object[:-1]  # 끝의 쉼표 제거
            if json_object:  # 빈 문자열이 아닌 경우만 처리
                try:
                    data.append(json.loads(json_object))
                    data_from_jsonl.append(json_object+'\n')
                except json.JSONDecodeError as e:
                    print(f"JSONDecodeError: {e} - 객체: {json_object}")
                json_object = ''  # 현재 JSON 객체가 끝났으므로 초기화


# 고정된 system content
system_content = ("""You are an API that receives information of every turn of the Coup board game and outputs what current player has to do. Take the current turn information in JSON format and output the result in JSON format.

cards_open indicates whether the card has lost its influence. if input is \"cards\": [\"duke,\" \"ambassador,\" \"cards_open\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before."

the goal of the game is to elimate the influence card of all other players and be the last survivor.
when a player lose all their influence card he lose the game.

Every turn, current_player perform one action they want and can afford.
- income: current_player get 1 coin.
- foreign_aid: current_player get 2 coins. other duke can perform counter_action.
- coup: cost 7 coins. choose one player and force to give up an influence card. if current_player start turn with 10 or more, current_player must coup.
- tax: current_player get 3 coins. can be challenged.
- steal: choose one player and take 2 coins. can be challeged. chosen player can perform counter_action with captain or ambassador.
- exchange: draw 2 influence card. place 2 influence card back. can be challeged.
- assassinate: cost 3 coins. choose one player and force to give up an influence card. can be challenged. chosen player can perform counter_action with contessa.

Every counter_action, current_player'action is canceled, or current_player can challenge to player performing counter_action. if challenge is success, counter_action is canceled.
Every challenge, the player who lose challenge is forced to give up an influence card.""")

# JSONL 데이터 생성을 위한 리스트 초기화
jsonl_data = []

# 입력 데이터를 파싱하여 JSONL 형식으로 변환
for i in range(0, len(data_from_jsonl), 2):
    user_content = json.dumps(data_from_jsonl[i], ensure_ascii=False)
    assistant_content = json.dumps(data_from_jsonl[i + 1], ensure_ascii=False)

    jsonl_entry = {
        "messages": [
            {"role": "system", "content": system_content},
            {"role": "user", "content": data_from_jsonl[i]},
            {"role": "assistant", "content": data_from_jsonl[i + 1]}
        ]
    }
    jsonl_data.append(jsonl_entry)

# JSONL 파일로 저장
with open('action_data_basic_10.jsonl', 'w', encoding='utf-8') as f:
    for entry in jsonl_data:
        f.write(json.dumps(entry, ensure_ascii=False) + '\n')

print("Jsonl 파일이 성공적으로 생성되었습니다.")
