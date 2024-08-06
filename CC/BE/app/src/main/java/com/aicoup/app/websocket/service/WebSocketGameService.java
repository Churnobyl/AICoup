package com.aicoup.app.websocket.service;

import com.aicoup.app.websocket.model.dto.GameStateDto;
import com.aicoup.app.websocket.model.dto.MessageDto;

import java.util.Map;

public interface WebSocketGameService {
    Map<String, String> validate(MessageDto message);
//    GameStateDto processAction(MessageDto message);
    GameStateDto getGameState(String gameId);
    void recordHistory(String gameId, Integer actionNumber, String playerTrying, String playerTried);
    String nextTurn(MessageDto messageDto);
    String myChoice(MessageDto messageDto);
}