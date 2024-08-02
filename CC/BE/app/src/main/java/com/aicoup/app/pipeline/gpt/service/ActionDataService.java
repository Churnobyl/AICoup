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
public class ActionDataService implements GameDataService {
    private final GameDataRepository gameDataRepository;
    private final ObjectMapper objectMapper;
    private final GPTConverter gptConverter;

    public String getGameDataAsJson(String gameId) {
        Optional<GameData> gameDataOptional = gameDataRepository.findById(gameId);

        if (gameDataOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid game ID: " + gameId);
        }

        GameData gameData = gameDataOptional.get();

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

    public String[] getData(String gameId) {
        String formattedGameDataAsJson = getFormattedGameDataAsJson(gameId);

        // JSONObject 생성
        JSONObject jsonObject = new JSONObject(formattedGameDataAsJson);

        // 키값과 밸류값 추출
        String action = jsonObject.getString("action");
        String target = jsonObject.getString("target");
        String[] actionArr = new String[2];
        actionArr[0] = action;
        actionArr[1] = target;
        System.out.println(formattedGameDataAsJson);
        return actionArr;
    }
}
