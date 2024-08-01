package com.aicoup.app;

import com.aicoup.app.websocket.model.dto.GameState;
import com.aicoup.app.websocket.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GameControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(GameControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameService gameService;

    @BeforeEach
    public void setup() {
        gameService.setupGame(Arrays.asList("Player1", "Player2"));
    }

    @Test
    public void testSetupGame() throws Exception {
        logger.info("Starting testSetupGame");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/game/setup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"Player1\", \"Player2\"]"))
                .andExpect(status().isOk());
        logger.info("Completed testSetupGame");
    }

    @Test
    public void testGetGameState() throws Exception {
        logger.info("Starting testGetGameState");
        mockMvc.perform(MockMvcRequestBuilders.get("/api/game/state")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players[0].name").value("Player1"))
                .andExpect(jsonPath("$.players[1].name").value("Player2"));
        logger.info("Completed testGetGameState");
    }

    @Test
    public void testTakeTurn() throws Exception {
        logger.info("Starting testTakeTurn");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/game/turn")
                        .param("playerIndex", "0")
                        .param("action", "Income")
                        .param("targetPlayerName", ""))
                .andExpect(status().isOk());

        GameState gameState = gameService.getGameState();
        assert gameState.getPlayers().get(0).getCoins() == 3;
        logger.info("Completed testTakeTurn: Player1 coins = {}", gameState.getPlayers().get(0).getCoins());
    }
}
