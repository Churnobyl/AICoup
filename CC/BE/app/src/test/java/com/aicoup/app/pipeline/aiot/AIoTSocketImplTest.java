package com.aicoup.app.pipeline.aiot;

import com.aicoup.app.pipeline.aiot.dto.MMResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AIoTSocketImplTest {

    AIoTSocket socket;

    @Test
    public void checkResponse() {
        System.out.println(socket.getDataFromAIoTServer());
    }

    @Test
    void gameStart() {
        socket.gameStart();
    }

    @Test
    void getDataFromAIoTServer() {
        List<MMResponse> dataFromAIoTServer = socket.getDataFromAIoTServer();
        System.out.println("dataFromAIoTServer = " + dataFromAIoTServer);
    }
}