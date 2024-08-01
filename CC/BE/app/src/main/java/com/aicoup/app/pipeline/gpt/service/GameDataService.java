package com.aicoup.app.pipeline.gpt.service;

import com.aicoup.app.domain.entity.game.GameData;
import com.aicoup.app.domain.repository.GameDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

public interface GameDataService {
    public String getGameDataAsJson(String gameId);
    public String getFormattedGameDataAsJson(String gameId);
}
