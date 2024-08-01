package com.aicoup.app.pipeline.gpt.service;

import com.aicoup.app.domain.entity.game.GameData;
import com.aicoup.app.domain.repository.GameDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CounterActionChallengeDataService implements GameDataService {
    private final GameDataRepository gameDataRepository;
    private final ObjectMapper objectMapper;

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
        Optional<GameData> gameDataOptional = gameDataRepository.findById(gameId);

        if (gameDataOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid game ID: " + gameId);
        }

        GameData gameData = gameDataOptional.get();

        try {
            // JSON 형식으로 변환
            String playerInfoJson = objectMapper.writeValueAsString(gameData.getPlayerinfo());
            String historyJson = objectMapper.writeValueAsString(gameData.getHistory());

            // JSON 문자열 생성
            return String.format(
                    "{\"player_num\": %d, \"current_player\": %d, \"counter_actioner\": %s, \"counter_action\": %s, \"playerinfo\": %s, \"history\": %s}",
                    gameData.getPlayerNum(),
                    gameData.getCurrentPlayer(),
                    gameData.getCounterActioner(),
                    gameData.getCounterAction(),
                    playerInfoJson,
                    historyJson
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }
}
