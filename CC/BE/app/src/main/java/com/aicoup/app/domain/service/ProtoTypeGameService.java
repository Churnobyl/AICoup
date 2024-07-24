package com.aicoup.app.domain.service;

import com.aicoup.app.domain.game.RandomGameGenerator;
import com.aicoup.app.domain.redisRepository.GameRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProtoTypeGameService implements GameService {

    private final GameRepository gameRepository;
    private final RandomGameGenerator gameGenerator;

    @Transactional
    @Override
    public String createNewGame(String roomId, Integer participants) {
        return gameGenerator.init(roomId, participants);
    }

}
