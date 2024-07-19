package com.aicoup.app.domain.service;

import com.aicoup.app.domain.entity.game.Game;

public interface GameService {

    Integer createNewGame(String gamename, Integer participants);
}
