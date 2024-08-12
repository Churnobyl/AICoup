package com.aicoup.app.pipeline.gpt.service;

import com.aicoup.app.domain.entity.game.GameData;
import com.aicoup.app.domain.repository.GameDataRepository;
import com.aicoup.app.pipeline.gpt.converter.GPTConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CounterActionDataService implements GameDataService {
    private final GameDataRepository gameDataRepository;
    private final ObjectMapper objectMapper;
    private final GPTConverter gptConverter;

    public String getGameDataAsJson(String gameId) {
        GameData gameData = gptConverter.run(gameId);

        try {
            return objectMapper.writeValueAsString(gameData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }

    public String getFormattedGameDataAsJson(String gameId) {
        GameData gameData = gptConverter.run(gameId);

        try {
            // JSON 형식으로 변환
            String playerInfoJson = objectMapper.writeValueAsString(gameData.getPlayerinfo());
            String historyJson = objectMapper.writeValueAsString(gameData.getHistory());

            // JSON 문자열 생성
            return String.format(
                    "{\"player_num\": %d, \"current_player\": %d, \"playerinfo\": %s, \"history\": %s}",
                    gameData.getPlayerNum(),
                    gameData.getCurrentPlayer(),
                    playerInfoJson,
                    historyJson
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }

    @Override
    public String[] getData(String gameId) {
        String formattedGameDataAsJson = getFormattedGameDataAsJson(gameId);

        // JSONObject 생성
        JSONObject jsonObject = new JSONObject(formattedGameDataAsJson);

        // 키값과 밸류값 추출
        String counterActioner = jsonObject.getString("counter_actioner");
        String counterAction = jsonObject.getString("counter_action");
        String[] actionArr = new String[2];
        actionArr[0] = counterActioner;
        actionArr[1] = counterAction;
        System.out.println(formattedGameDataAsJson);
        return actionArr;
    }
}
