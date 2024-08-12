package com.aicoup.app.domain.service;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.member.GameMember;

import java.util.List;

public interface GameService {

    Game createNewGame(String gamename, Integer participants);
}
