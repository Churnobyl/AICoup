package com.aicoup.app.websocket.controller;

import com.aicoup.app.websocket.model.dto.GameState;
import com.aicoup.app.websocket.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/game")
public class ConsoleGameController {
    @Autowired
    private GameService gameService;

    @PostMapping("/setup")
    public void setupGame(@RequestBody List<String> playerNames) {
        gameService.setupGame(playerNames);
    }

    @PostMapping("/turn")
    public void takeTurn(
            @RequestParam("playerName") String playerName,
            @RequestParam("action") String action,
            @RequestParam("targetPlayerName") String targetPlayerName) {
        gameService.takeTotalTurn(playerName, action, targetPlayerName);
    }

    @GetMapping("/state")
    public GameState getGameState() {
        return gameService.getGameState();
    }
}
