package com.aicoup.app.pipeline.gpt.converter;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter @Setter
public class GPTPlayer {
    private List<String> cards;
    private List<Boolean> cardsOpen;
    private int coins;

    public GPTPlayer(List<String> cards, List<Boolean> cardsOpen, int coins) {
        this.cards = cards;
        this.cardsOpen = cardsOpen;
        this.coins = coins;
    }
}
