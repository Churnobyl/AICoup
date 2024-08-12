package com.aicoup.app.websocket.model.dto;

import com.aicoup.app.domain.entity.game.history.History;
import com.aicoup.app.domain.entity.game.member.GameMember;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class GameStateDto {
    private String message; // gameId
    private Integer turn;
    private List<GameMember> members = new ArrayList<>();
    private int whoseTurn;
    private History lastContext;
    private int action;
    private String target;
    private int cardOpen;
    private Map<String, Integer> canAction = new HashMap<>();
    private List<History> history;
    private int[] deck = new int[6];

    private boolean awaitingChallenge;
    private boolean awaitingCounterAction;
    private Integer awaitingChallengeActionValue;
    private Integer awaitingCounterActionValue;
    private boolean challengeSuccessful;
}