package com.aicoup.app.domain.game;

import com.aicoup.app.domain.entity.game.Game;

public interface GameGenerator {
    Game init(String roomId);
}
