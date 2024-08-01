package com.aicoup.app.domain.game;

import com.aicoup.app.domain.entity.game.Game;

public interface GameProcessor {

    // 프로세서 진입점
    String run(Game game);

    // 새로운 행동을 시작할 경우
    String runInitContext();

    // 누군가의 행동에 대해서 반응을 할 경우
    String runReactionContext();
}
