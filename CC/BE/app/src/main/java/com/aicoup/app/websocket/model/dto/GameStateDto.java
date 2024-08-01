package com.aicoup.app.websocket.model.dto;

import com.aicoup.app.domain.entity.game.action.Action;
import com.aicoup.app.domain.entity.game.history.History;
import com.aicoup.app.domain.entity.game.member.GameMember;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class GameStateDto {
    private Integer turn;
    private List<GameMember> members = new ArrayList<>();
    private String message;
    private int whoseTurn;
    private History lastContext;
    private List<History> history;
    private int[] deck = new int[6];
}
