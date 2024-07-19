package com.aicoup.app.pipeline.gpt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChatGPTSocketImplTest {

    @Autowired
    ChatGPTSocket chatGPTSocket;

    @Test
    void testChatGPTApi() {
        String dataFromGptApiForAction = chatGPTSocket.getDataFromGptApiForAction("{\"role\": \"system\", \"content\": \"보드 게임 coup의 현재 게임 정보를 받고 현재 플레이어의 action을 JSON으로 return하는 api입니다. 출력은 JSON 값만 출력합니다.\\ncoup의 규칙\\n- Every turn, current player choose action below\\n    - Income [coin +1]\\n    - Foreign Aid [coin +2]\\n        - Counteraction (all player available)\\n            - Challenge\\n    - Coup [coin -7, selected player’s card -1]\\n    - Tax[coin +3]\\n        - Challenge\\n    - Steal[coin +2, selected player’s coin-2]\\n        - Challenge\\n        - Counteraction (only selected player available)\\n            - Challenge\\n    - Exchange [card +2, selected card -2]\\n        - Challenge\\n    - Assainate [coin -3, selected player’s card -1]\\n        - Challenge\\n        - Counteraction (only selected player available)\\n            - Challenge\\n- Every counteraction\\n    - if success, [action canceled]\\n    - if fail, [counteraction player’s card -1]\\n- Every challenge,\\n    - if success, [current player’s card -1]\\n    - if fail, [challenge player’s card -1]\\n\\n입력 받는 데이터의 형태\\nplayerinfo의 card는 현재 플레이어가 가지고 있는 카드 종류. cards_open은 해당 카드의 공개 여부, coin은 현재 플레이어의 소지 동전수를 의미해. history는 해당 플레이어가 행동한 action의 종류가 저장되어 있어\\n{\\n  \\\"player_num\\\": 3,\\n  \\\"current_player\\\": 2,\\n  \\\"turn\\\": 5,\\n  \\\"playerinfo\\\": [\\n    {\\n      \\\"1\\\": {\\n        \\\"cards\\\": [\\\"Duke\\\", \\\"Captain\\\"],\\n        \\\"cards_open\\\": [false, false],\\n        \\\"coins\\\": 5\\n      },\\n      \\\"2\\\": {\\n        \\\"cards\\\": [\\\"Duke\\\", \\\"Ambassador\\\"],\\n        \\\"cards_open\\\": [true, false],\\n        \\\"coins\\\": 3\\n      },\\n      \\\"3\\\": {\\n        \\\"cards\\\": [\\\"Contessa\\\", \\\"Captain\\\"],\\n        \\\"cards_open\\\": [false, false],\\n        \\\"coins\\\": 4\\n      }\\n    }\\n  ],\\n  \\\"history\\\": {\\n    \\\"1\\\": [\\\"Duke\\\"],\\n    \\\"2\\\": [\\\"Duke\\\"],\\n    \\\"3\\\": [\\\"Captain\\\"]\\n  }\\n}\\n\\n해당 데이터의 출력값은\\n{\\n  \\\"action\\\": \\\"Exchange\\\",\\n  \\\"target\\\": \\\"none\\\"\\n}\\n\\n2번 샘플\\n{\\n  \\\"player_num\\\": 3,\\n  \\\"current_player\\\": 2,\\n  \\\"turn\\\": 2,\\n  \\\"playerinfo\\\": [\\n    {\\n      \\\"1\\\": {\\n        \\\"cards\\\": [\\\"Duke\\\", \\\"Captain\\\"],\\n        \\\"cards_open\\\": [false, false],\\n        \\\"coins\\\": 5\\n      },\\n      \\\"2\\\": {\\n        \\\"cards\\\": [\\\"Duke\\\", \\\"Ambassador\\\"],\\n        \\\"cards_open\\\": [false, false],\\n        \\\"coins\\\": 2\\n      },\\n      \\\"3\\\": {\\n        \\\"cards\\\": [\\\"Contessa\\\", \\\"Captain\\\"],\\n        \\\"cards_open\\\": [false, false],\\n        \\\"coins\\\": 2\\n      }\\n    }\\n  ],\\n  \\\"history\\\": {\\n    \\\"1\\\": [\\\"Duke\\\"],\\n    \\\"2\\\": [],\\n    \\\"3\\\": []\\n  }\\n}\\n2번 샘플에 대한 출력값\\n{\\n  \\\"action\\\": \\\"Tax\\\",\\n  \\\"target\\\": \\\"none\\\"\\n}\"},\n" +
                "      {\"role\": \"user\", \"content\": \"{\\\"player_num\\\": 4, \\\"current_player\\\": 1, \\\"turn\\\": 18, \\\"playerinfo\\\": [{\\\"1\\\": {\\\"cards\\\": [\\\"ambassador\\\", \\\"captain\\\"], \\\"cards_open\\\": [false, false], \\\"coins\\\": 5}, \\\"2\\\": {\\\"cards\\\": [\\\"captain\\\", \\\"captain\\\"], \\\"cards_open\\\": [false, true], \\\"coins\\\": 6}, \\\"3\\\": {\\\"cards\\\": [\\\"duke\\\", \\\"contessa\\\"], \\\"cards_open\\\": [false, false], \\\"coins\\\": 7}, \\\"4\\\": {\\\"cards\\\": [\\\"assassin\\\", \\\"contessa\\\"], \\\"cards_open\\\": [true, true], \\\"coins\\\": 3}}], \\\"history\\\": {\\\"1\\\": [\\\"duke\\\", \\\"ambassador\\\", \\\"captain\\\"], \\\"2\\\": [\\\"captain\\\", \\\"captain\\\", \\\"captain\\\"], \\\"3\\\": [\\\"duke\\\", \\\"ambassador\\\", \\\"duke\\\", \\\"contessa\\\", \\\"duke\\\"], \\\"4\\\": [\\\"assassin\\\"]}}\"}");

//        Parser
//
//        assertThat(data)
        System.out.println("dataFromGptApiForAction = " + dataFromGptApiForAction);
    }
}