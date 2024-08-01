package com.aicoup.app.websocket.service;

import com.aicoup.app.websocket.model.dto.MessageDto;

import java.util.Map;

public interface WebSocketGameService {

    boolean gameCheck(MessageDto messageDto);

    String gameInit(MessageDto messageDto);

    Map<String, String> validate(MessageDto messageDto);

    String nextTurn(MessageDto messageDto);

    String myChoice(MessageDto messageDto);

    void recordHistory(String gameId, Integer actionNumber, String playerTrying, String playerTried);
}
