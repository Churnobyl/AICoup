package com.aicoup.app.domain.service;

import com.aicoup.app.domain.game.GameGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoTypeGameService implements GameService {
    private final GameGenerator gameGenerator;

    @Transactional
    @Override
    public Integer createNewGame(String gamename, Integer participants) {
        gameGenerator.init(gamename, participants);
        return 0;
    }
}
