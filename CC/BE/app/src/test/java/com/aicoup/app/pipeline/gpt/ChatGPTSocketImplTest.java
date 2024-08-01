package com.aicoup.app.pipeline.gpt;

import com.aicoup.app.pipeline.gpt.service.GameDataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ChatGPTSocketImplTest {

    @Autowired
    ChatGPTSocket chatGPTSocket;

    @Autowired
    GameDataService gameDataService;

    @Test
    void testActionApi() {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and outputs what current player has to do...";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = gameDataService.getFormattedGameDataAsJson("1");
        System.out.println("userPrompt = " + userPrompt);

        // API 호출
        String dataFromGptApiForAction = chatGPTSocket.getDataFromGptApiForAction(systemPrompt, userPrompt);

        // 결과 출력
        System.out.println("dataFromGptApiForAction = " + dataFromGptApiForAction);
    }

    @Test
    void testChallengeApi() {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and current_player's action and target...";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = gameDataService.getFormattedGameDataAsJson("2");

        // API 호출
        String dataFromGptApiForChallenge = chatGPTSocket.getDataFromGptApiForChallengeAgainstAction(systemPrompt, userPrompt);

        // 결과 출력
        System.out.println("dataFromGptApiForChallenge = " + dataFromGptApiForChallenge);
    }

    @Test
    void testCounterActionApi() {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and current_player's action and target...";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = gameDataService.getFormattedGameDataAsJson("3");

        // API 호출
        String dataFromGptApiForCounterAction = chatGPTSocket.getDataFromGptApiForCounteractionAgainstAction(systemPrompt, userPrompt);

        // 결과 출력
        System.out.println("dataFromGptApiForCounterAction = " + dataFromGptApiForCounterAction);
    }

    @Test
    void testCounterActionChallengeApi() {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and counter_actioner and his _counter_action...";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = gameDataService.getFormattedGameDataAsJson("4");

        // API 호출
        String dataFromGptApiForCounterActionChallenge = chatGPTSocket.getDataFromGptApiForChallengeAgainstCounteraction(systemPrompt, userPrompt);

        // 결과 출력
        System.out.println("dataFromGptApiForCounterActionChallenge = " + dataFromGptApiForCounterActionChallenge);
    }
}
