package com.aicoup.app.websocket.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PlayerDto {
    private String name;
    private int coins;
    private boolean alive;
    private List<CardDto> cards;
    private List<String> influence;

    public PlayerDto() {
        this.coins = 2;
        this.alive = true;
        this.influence = new ArrayList<>();
    }

    public void removeCard(String card) {
        this.cards.remove(card);
        if (this.cards.isEmpty()) {
            this.alive = false;
        }
    }

    public boolean hasCard(String card) {
        return this.cards.contains(card);
    }
}