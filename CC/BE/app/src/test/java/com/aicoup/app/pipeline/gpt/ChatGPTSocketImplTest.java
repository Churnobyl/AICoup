package com.aicoup.app.pipeline.gpt;

import com.aicoup.app.pipeline.gpt.service.ActionDataService;
import com.aicoup.app.pipeline.gpt.service.ChallengeDataService;
import com.aicoup.app.pipeline.gpt.service.CounterActionChallengeDataService;
import com.aicoup.app.pipeline.gpt.service.CounterActionDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatGPTSocketImplTest {

    @Autowired
    ChatGPTSocket chatGPTSocket;

    @Autowired
    ActionDataService actionDataService;
    @Autowired
    ChallengeDataService challengeDataService;
    @Autowired
    CounterActionDataService counterActionDataService;
    @Autowired
    CounterActionChallengeDataService counterActionChallengeDataService;

    @Test
    void testActionApi() {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and outputs what current player has to do. Take the current turn information in JSON format and output the result in JSON format.cards_open indicates whether the card has lost its influence. if input is \\\"cards\\\": [\\\"duke\\\", \\\"ambassador\\\"], \\\"cards_open\\\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before.the goal of the game is to elimate the influence card of all other players and be the last survivor.when a player lose all their influence card he lose the game.Every turn, current_player perform one action they want and can afford.- income: current_player get 1 coin.- foreign_aid: current_player get 2 coins. other duke can perform counter_action.- coup: cost 7 coins. choose one player and force to give up an influence card. if current_player start turn with 10 or more, current_player must coup.- tax: current_player get 3 coins. can be challenged.- steal: choose one player and take 2 coins. can be challeged. chosen player can perform counter_action with captain or ambassador.- exchange: draw 2 influence card. place 2 influence card back. can be challeged.- assassinate: cost 3 coins. choose one player and force to give up an influence card. can be challenged. chosen player can perform counter_action with contessa.Every counter_action, current_player'action is canceled, or current_player can challenge to player performing counter_action. if challenge is success, counter_action is canceled.Every challenge, the player who lose challenge is forced to give up an influence card.";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = actionDataService.getFormattedGameDataAsJson(1L);

        // API 호출
        String dataFromGptApiForAction = chatGPTSocket.getDataFromGptApiForAction(systemPrompt, userPrompt);

        // 결과 출력
        System.out.println("dataFromGptApiForAction = " + dataFromGptApiForAction);
    }

    @Test
    void testChallengeApi() {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and current_player's action and target. you should output which player should challenge as a challenger for current_player's action. if there is no proper challenger, you should ouput \\\"none\\\". Take information in JSON format and output the result in JSON format.cards_open indicates whether the card has lost its influence. if input is \\\"cards\\\": [\\\"duke\\\", \\\"ambassador\\\"], \\\"cards_open\\\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before. history show what action was taken by each player. if input is \\\"history\\\": {\\\"1\\\": [\\\"tax\\\", \\\"exchange\\\", \\\"steal\\\"],\\\"2\\\": [\\\"steal\\\", \\\"steal\\\", \\\"steal\\\"],\\\"3\\\": [\\\"tax\\\", \\\"tax\\\"],\\\"4\\\": [\\\"income\\\", \\\"assassinate\\\"]} and current_player is 1, current_player's last two action is exchange and steal.any other player can challenge to a current_player regardless of whether they are the involved in action.player may be telling the truth or bluffing.the goal of the game is to elimate the influence card of all other players and be the last survivor.when a player lose all their influence card he lose the game.whoever loses the challenge immediately loses an influence card.challenger is usually a player in order of player who is target, player who has most influence card.it is suspicious if current_action is not match with the recent actions.";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = challengeDataService.getFormattedGameDataAsJson(2L);

        // API 호출
        String dataFromGptApiForChallenge = chatGPTSocket.getDataFromGptApiForChallengeAgainstAction(systemPrompt, userPrompt);

        // 결과 출력
        System.out.println("dataFromGptApiForChallenge = " + dataFromGptApiForChallenge);
    }

    @Test
    void testCounterActionApi() {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and current_player's action and target. you return counter_actioner and counter_action. counter_actioner is a player who perform counter_action and counter_action is a charater who can block current_action. if there is no proper counter_actioner, you should ouput \\\"none\\\" for counter_actioner and counter_action. Take information in JSON format and output the result in JSON format.cards_open indicates whether the card has lost its influence. if input is \\\"cards\\\": [\\\"duke\\\", \\\"ambassador\\\"], \\\"cards_open\\\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before. history show what action was taken by each player. if input is \\\"history\\\": {\\\"1\\\": [\\\"tax\\\", \\\"exchange\\\", \\\"steal\\\"],\\\"2\\\": [\\\"steal\\\", \\\"steal\\\", \\\"steal\\\"],\\\"3\\\": [\\\"tax\\\", \\\"tax\\\"],\\\"4\\\": [\\\"income\\\", \\\"assassinate\\\"]} and current_player is 1, current_player's last two action is exchange and steal.for below current_action only can be blocked by other player who is target and follwing counter_action.- assassinate: contessa.- steal: ambassador, captain.  for below current_action only can be blocked by following counter_action.- foreign_aid: duke.player may be telling the truth or bluffing.if current_action is successfully is counter_actioned, current_action fails.player who perform counter_action can be challenged.the goal of the game is to elimate the influence card of all other players and be the last survivor.when a player lose all their influence card he lose the game.whoever loses the challenge immediately loses an influence card.counter_actioner is usually a player who is target.player whose recent action is tax usally perform counter_action with duke.if target player's contessa is influential and current_action is assassinate, he must perform counteraction with contessa.if target player has an only influence card and current_action is assassinate, he must perform counter_action with contessa.";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = challengeDataService.getFormattedGameDataAsJson(3L);

        // API 호출
        String dataFromGptApiForCounterAction = chatGPTSocket.getDataFromGptApiForCounteractionAgainstAction(systemPrompt, userPrompt);

        // 결과 출력
        System.out.println("dataFromGptApiForCounterAction = " + dataFromGptApiForCounterAction);
    }

    @Test
    void testCounterActionChallengeApi() {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and counter_actioner and his _counter_action. you should output which player should challenge as a challenger for counter_actioner's counter_action. if there is no proper challenger, you should ouput \\\"none\\\". Take information in JSON format and output the result in JSON format.cards_open indicates whether the card has lost its influence. if input is \\\"cards\\\": [\\\"duke\\\", \\\"ambassador\\\"], \\\"cards_open\\\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before. history show what action was taken by each player. if input is \\\"history\\\": {\\\"1\\\": [\\\"tax\\\", \\\"exchange\\\", \\\"steal\\\"],\\\"2\\\": [\\\"steal\\\", \\\"steal\\\", \\\"steal\\\"],\\\"3\\\": [\\\"tax\\\", \\\"tax\\\"],\\\"4\\\": [\\\"income\\\", \\\"assassinate\\\"]} and current_player is 1, current_player's last two action is exchange and steal.any other player can challenge to a current_player regardless of whether they are the involved in action.player may be telling the truth or bluffing.the goal of the game is to elimate the influence card of all other players and be the last survivor.when a player lose all their influence card he lose the game.whoever loses the challenge immediately loses an influence card.challenger is usually a player in order of player who is current_player, player who has most influence card.it is suspicious if counter_action is not match with the recent actions.";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = counterActionChallengeDataService.getFormattedGameDataAsJson(4L);

        // API 호출
        String dataFromGptApiForCounterActionChallenge = chatGPTSocket.getDataFromGptApiForChallengeAgainstCounteraction(systemPrompt, userPrompt);

        // 결과 출력
        System.out.println("dataFromGptApiForCounterActionChallenge = " + dataFromGptApiForCounterActionChallenge);
    }
}
