package com.aicoup.app.websocket.service;

public interface WebSocketGameService {

    public String gameInit(String roomId);

    boolean validate();

    String nextTurn();

    String myChoice();

    void recordHistory(String gameId, Integer actionNumber, Integer playerTrying, Integer playerTried);
}
