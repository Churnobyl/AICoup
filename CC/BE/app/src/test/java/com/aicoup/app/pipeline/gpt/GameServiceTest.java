package com.aicoup.app.pipeline.gpt;

import com.aicoup.app.websocket.service.GameService;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GameServiceTest {

    @Autowired
    GameService gameService;

    @Test
    void actionTest() throws JSONException {
        gameService.askActionToGPT();
    }
}
