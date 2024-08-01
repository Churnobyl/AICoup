package com.aicoup.app.pipeline.gpt.converter;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
public class GPTGameState {
    private Map<Integer, GPTPlayer> players;

    public GPTGameState(Map<Integer, GPTPlayer> players) {
        this.players = players;
    }
}
