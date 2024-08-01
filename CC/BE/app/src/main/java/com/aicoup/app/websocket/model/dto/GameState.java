package com.aicoup.app.websocket.model.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

@Getter
    @Setter
public class GameState {
    private List<PlayerDto> players;
    private List<PlayerDto> playersAlive;
    private int currentPlayerIndex;
    private boolean gameIsRunning;
    private List<CardDto> deck;

    // Singleton 패턴으로 전역적으로 상태를 관리합니다.
    private static GameState instance;

    private GameState() {
        reset();
    }

    public static synchronized GameState getInstance() {
        if (instance == null) {
            instance = new GameState();
        }
        return instance;
    }

    public void reset() {
        players = new ArrayList<>();
        playersAlive = new ArrayList<>();
        currentPlayerIndex = 0;
        gameIsRunning = true;
    }

    public void initializeDeck() {
        deck = new ArrayList<>();
        // 각 카드 타입별로 3장씩 추가
        for (int i = 0; i < 3; i++) {
            deck.add("Duke");
            deck.add("Assassin");
            deck.add("Ambassador");
            deck.add("Captain");
            deck.add("Contessa");
        }
        Collections.shuffle(deck);
    }

    public List<CardDto> drawCards(int count) {
        List<CardDto> drawnCards = new ArrayList<>();
        for (int i = 0; i < count && !deck.isEmpty(); i++) {
            drawnCards.add(deck.remove(0));
        }
        return drawnCards;
    }

    public void returnCardsToDeck(List<CardDto> cards) {
        deck.addAll(cards);
        Collections.shuffle(deck);
    }
}
