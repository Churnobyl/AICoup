package com.aicoup.app.pipeline.gpt.service;

import com.aicoup.app.pipeline.gpt.ChatGPTSocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GPTResponseGetter {

    private final ChatGPTSocket chatGPTSocket;
    private final ActionDataService actionDataService;
    private final ChallengeDataService challengeDataService;
    private final CounterActionDataService counterActionDataService;
    private final CounterActionChallengeDataService counterActionChallengeDataService;
    private final ObjectMapper objectMapper;

    public String[] actionApi(String gameId) {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and outputs what current player has to do. Take the current turn information in JSON format and output the result in JSON format.cards_open indicates whether the card has lost its influence. if input is \"cards\": [\"duke\", \"ambassador\"], \"cards_open\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before.the goal of the game is to elimate the influence card of all other players and be the last survivor.when a player lose all their influence card he lose the game.Every turn, current_player perform one action they want and can afford.- income: current_player get 1 coin.- foreign_aid: current_player get 2 coins. other duke can perform counter_action.- coup: cost 7 coins. choose one player and force to give up an influence card. if current_player start turn with 10 or more, current_player must coup.- tax: current_player get 3 coins. can be challenged.- steal: choose one player and take 2 coins. can be challeged. chosen player can perform counter_action with captain or ambassador.- exchange: draw 2 influence card. place 2 influence card back. can be challeged.- assassinate: cost 3 coins. choose one player and force to give up an influence card. can be challenged. chosen player can perform counter_action with contessa.Every counter_action, current_player'action is canceled, or current_player can challenge to player performing counter_action. if challenge is success, counter_action is canceled.Every challenge, the player who lose challenge is forced to give up an influence card.";

        String userPrompt = actionDataService.getFormattedGameDataAsJson(gameId);

        String dataFromGptApiForAction = "";
        JSONObject jsonObject = null;
        int maxAttempts = 3;
        int attempts = 0;

        while (attempts < maxAttempts) {
            dataFromGptApiForAction = chatGPTSocket.getDataFromGptApiForAction(systemPrompt, userPrompt);

            try {
                jsonObject = new JSONObject(dataFromGptApiForAction);
                if (jsonObject.has("action") && jsonObject.has("target")) {
                    break;  // "action && target key 존재 확인"
                }
            } catch (JSONException e) {
                // JSON 파싱이 실패하면 api 재호출
            }

            attempts++;
            if (attempts < maxAttempts) {
                // 재호출 전에 기다리는 로직
                try {
                    Thread.sleep(1000);  // 1초 기다림
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (jsonObject == null || !jsonObject.has("action") || !jsonObject.has("target")) {
            // 3번 시도해도 형식에 맞지 않으면 그냥 {"action' : "income", "target" : "none"} 으로 고정
            return new String[]{"income", "none"};
        }

        String action = jsonObject.getString("action");

        String target;
        // 정수형 -> 문자열 처리
        Object targetObj = jsonObject.get("target");
        if (targetObj instanceof Integer) {
            target = String.valueOf(targetObj);
        } else {
            target = targetObj.toString();
        }
        String[] actionArr = new String[2];
        actionArr[0] = action;
        actionArr[1] = target;
        System.out.println(dataFromGptApiForAction);
        return actionArr;
    }

    public String[] challengeApi(String gameId) {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and current_player's action and target. you should output which player should challenge as a challenger for current_player's action. if there is no proper challenger, you should ouput \"none\". Take information in JSON format and output the result in JSON format.cards_open indicates whether the card has lost its influence. if input is \"cards\": [\"duke\", \"ambassador\"], \"cards_open\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before. history show what action was taken by each player. if input is \"history\": {\"1\": [\"tax\", \"exchange\", \"steal\"],\"2\": [\"steal\", \"steal\", \"steal\"],\"3\": [\"tax\", \"tax\"],\"4\": [\"income\", \"assassinate\"]} and current_player is 1, current_player's last two action is exchange and steal.any other player can challenge to a current_player regardless of whether they are the involved in action.player may be telling the truth or bluffing.the goal of the game is to elimate the influence card of all other players and be the last survivor.when a player lose all their influence card he lose the game.whoever loses the challenge immediately loses an influence card.challenger is usually a player in order of player who is target, player who has most influence card.it is suspicious if current_action is not match with the recent actions.";

        String userPrompt = challengeDataService.getFormattedGameDataAsJson(gameId);

        String dataFromGptApiForChallenge = "";
        JSONObject jsonObject = null;
        int maxAttempts = 3;
        int attempts = 0;

        while (attempts < maxAttempts) {
            dataFromGptApiForChallenge = chatGPTSocket.getDataFromGptApiForChallengeAgainstAction(systemPrompt, userPrompt);

            try {
                jsonObject = new JSONObject(dataFromGptApiForChallenge);
                if (jsonObject.has("challenger")) {
                    break;  // "challenger" key 존재 확인
                }
            } catch (JSONException e) {
                // JSON 파싱이 실패하면 api 재호출
            }

            attempts++;
            if (attempts < maxAttempts) {
                // 재호출 전에 기다리는 로직
                try {
                    Thread.sleep(1000);  // 1초 기다림
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (jsonObject == null || !jsonObject.has("challenger")) {
            // 3번 시도해도 형식에 맞지 않으면 그냥 {"challenger" : "none"} 으로 고정
            return new String[]{"none"};
        }

        String challenger;
        // 정수형 -> 문자열 처리
        Object challengerObj = jsonObject.get("challenger");
        if (challengerObj instanceof Integer) {
            challenger = String.valueOf(challengerObj);
        } else {
            challenger = challengerObj.toString();
        }
        String[] challengerArr = new String[1];
        challengerArr[0] = challenger;
        System.out.println(dataFromGptApiForChallenge);
        return challengerArr;
    }

    public String[] counterActionApi(String gameId) {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and current_player's action and target. you return counter_actioner and counter_action. counter_actioner is a player who perform counter_action and counter_action is a charater who can block current_action. if there is no proper counter_actioner, you should ouput \\\"none\\\" for counter_actioner and counter_action. Take information in JSON format and output the result in JSON format.cards_open indicates whether the card has lost its influence. if input is \\\"cards\\\": [\\\"duke\\\", \\\"ambassador\\\"], \\\"cards_open\\\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before. history show what action was taken by each player. if input is \\\"history\\\": {\\\"1\\\": [\\\"tax\\\", \\\"exchange\\\", \\\"steal\\\"],\\\"2\\\": [\\\"steal\\\", \\\"steal\\\", \\\"steal\\\"],\\\"3\\\": [\\\"tax\\\", \\\"tax\\\"],\\\"4\\\": [\\\"income\\\", \\\"assassinate\\\"]} and current_player is 1, current_player's last two action is exchange and steal.for below current_action only can be blocked by other player who is target and follwing counter_action.- assassinate: contessa.- steal: ambassador, captain.  for below current_action only can be blocked by following counter_action.- foreign_aid: duke.player may be telling the truth or bluffing.if current_action is successfully is counter_actioned, current_action fails.player who perform counter_action can be challenged.the goal of the game is to elimate the influence card of all other players and be the last survivor.when a player lose all their influence card he lose the game.whoever loses the challenge immediately loses an influence card.counter_actioner is usually a player who is target.player whose recent action is tax usally perform counter_action with duke.if target player's contessa is influential and current_action is assassinate, he must perform counteraction with contessa.if target player has an only influence card and current_action is assassinate, he must perform counter_action with contessa.";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = counterActionDataService.getFormattedGameDataAsJson(gameId);

        String dataFromGptApiForCounterAction = "";
        JSONObject jsonObject = null;
        int maxAttempts = 3;
        int attempts = 0;

        while (attempts < maxAttempts) {
            dataFromGptApiForCounterAction = chatGPTSocket.getDataFromGptApiForCounteractionAgainstAction(systemPrompt, userPrompt);

            try {
                jsonObject = new JSONObject(dataFromGptApiForCounterAction);
                if (jsonObject.has("counter_actioner") && jsonObject.has("counter_action")) {
                    break;  // "counter_actioner && counter_action key 존재 확인"
                }
            } catch (JSONException e) {
                // JSON 파싱이 실패하면 api 재호출
            }

            attempts++;
            if (attempts < maxAttempts) {
                // 재호출 전에 기다리는 로직
                try {
                    Thread.sleep(1000);  // 1초 기다림
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (jsonObject == null || !jsonObject.has("counter_actioner") || !jsonObject.has("counter_action")) {
            // 3번 시도해도 형식에 맞지 않으면 그냥 {"counter_actioner' : "none", "counter_action" : "none"} 으로 고정
            return new String[]{"none", "none"};
        }

        String counterAction;
        // 정수형 -> 문자열 처리
        Object counterActionObj = jsonObject.get("counter_actioner");
        if (counterActionObj instanceof Integer) {
            counterAction = String.valueOf(counterActionObj);
        } else {
            counterAction = counterActionObj.toString();
        }
        String target = jsonObject.getString("counter_action");
        String[] actionArr = new String[2];
        actionArr[0] = counterAction;
        actionArr[1] = target;
        System.out.println(dataFromGptApiForCounterAction);
        return actionArr;
    }

    public String[] counterActionChallengeApi(String gameId) {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and counter_actioner and his _counter_action. you should output which player should challenge as a challenger for counter_actioner's counter_action. if there is no proper challenger, you should ouput \\\"none\\\". Take information in JSON format and output the result in JSON format.cards_open indicates whether the card has lost its influence. if input is \\\"cards\\\": [\\\"duke\\\", \\\"ambassador\\\"], \\\"cards_open\\\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before. history show what action was taken by each player. if input is \\\"history\\\": {\\\"1\\\": [\\\"tax\\\", \\\"exchange\\\", \\\"steal\\\"],\\\"2\\\": [\\\"steal\\\", \\\"steal\\\", \\\"steal\\\"],\\\"3\\\": [\\\"tax\\\", \\\"tax\\\"],\\\"4\\\": [\\\"income\\\", \\\"assassinate\\\"]} and current_player is 1, current_player's last two action is exchange and steal.any other player can challenge to a current_player regardless of whether they are the involved in action.player may be telling the truth or bluffing.the goal of the game is to elimate the influence card of all other players and be the last survivor.when a player lose all their influence card he lose the game.whoever loses the challenge immediately loses an influence card.challenger is usually a player in order of player who is current_player, player who has most influence card.it is suspicious if counter_action is not match with the recent actions.";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = counterActionChallengeDataService.getFormattedGameDataAsJson(gameId);

        String dataFromGptApiForChallenge = "";
        JSONObject jsonObject = null;
        int maxAttempts = 3;
        int attempts = 0;

        while (attempts < maxAttempts) {
            dataFromGptApiForChallenge = chatGPTSocket.getDataFromGptApiForChallengeAgainstAction(systemPrompt, userPrompt);

            try {
                jsonObject = new JSONObject(dataFromGptApiForChallenge);
                if (jsonObject.has("challenger")) {
                    break;  // "challenger" key 존재 확인
                }
            } catch (JSONException e) {
                // JSON 파싱이 실패하면 api 재호출
            }

            attempts++;
            if (attempts < maxAttempts) {
                // 재호출 전에 기다리는 로직
                try {
                    Thread.sleep(1000);  // 1초 기다림
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (jsonObject == null || !jsonObject.has("challenger")) {
            // 3번 시도해도 형식에 맞지 않으면 그냥 challenger : none으로 고정
            return new String[]{"none"};
        }

        String challenger;
        // 정수형 -> 문자열 처리
        Object challengerObj = jsonObject.get("challenger");
        if (challengerObj instanceof Integer) {
            challenger = String.valueOf(challengerObj);
        } else {
            challenger = challengerObj.toString();
        }
        String[] challengerArr = new String[1];
        challengerArr[0] = challenger;
        System.out.println(dataFromGptApiForChallenge);
        return challengerArr;
    }

    public String actionDialogApi(String action, String target, String currentPlayer, String personality) {
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("action", action);
        jsonData.put("target", target);
        jsonData.put("current_player", currentPlayer);
        jsonData.put("personality", personality);

        String systemPrompt = "You are an API that returns Korean dialog for board game Coup player. The dialog must shows your personality, action and target of action from current_player's point of view in colloquial word. dialog would be better as if current_player is really doing his action. You receive action, target, current_player who is you and personality which of yours.  You must return the result in Korean.";
        String userPrompt = "";
        try {
            userPrompt = objectMapper.writeValueAsString(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 또는 적절한 에러 처리
        }
        return chatGPTSocket.getDataFromGptApiForDialog(systemPrompt, userPrompt);
    }

    public String challengeDialogApi(String challenger, String currentPlayer, String currentAction, String personality) {
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("challenger", challenger);
        jsonData.put("current_player", currentPlayer);
        jsonData.put("current_action", currentAction);
        jsonData.put("personality", personality);

        String systemPrompt = "You are an API that returns Korean dialog for board game Coup player. You receive challenger, current_player, current_action and personality as input. challenger is you. current_player is who you challenge as you think current_player is bluffing. current_action is the action taken by current_player. personality is the personality of challenger. The dialog must start with your introduction which shows the number of challenger and end with your conclusion. The dialog must shows your personality, current_player and his or her action from challenger's point of view in colloquial word. You must return the result in Korean.";
        String userPrompt = "";
        try {
            userPrompt = objectMapper.writeValueAsString(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 또는 적절한 에러 처리
        }
        return chatGPTSocket.getDataFromGptApiForDialog(systemPrompt, userPrompt);
    }
}
