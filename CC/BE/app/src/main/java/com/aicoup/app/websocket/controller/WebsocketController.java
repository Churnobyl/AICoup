package com.aicoup.app.websocket.controller;

import com.aicoup.app.websocket.model.dto.GameStateDto;
import com.aicoup.app.websocket.model.dto.MessageDto;
import com.aicoup.app.websocket.service.WebSocketGameServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebsocketController {
    private final SimpMessagingTemplate template;
    private final WebSocketGameServiceImpl webSocketGameService;

    @MessageMapping(value = "/chat/message")
    public void message(MessageDto message) {
        MessageDto newMessage = new MessageDto(message.getRoomId(), "server");

        // 서버와 현실 일치 검증
        if (!webSocketGameService.validate()) {
            newMessage.setState("validationFail");
            newMessage.setMainMessage("검증 실패");
            template.convertAndSend("/sub/chat/room/" + message.getRoomId(), newMessage);
            return;
        }

        // state에 따라 분기 로직
        String state = message.getState();
        String returnState = null;

        switch (state) {
            case "gameInit":
                String gameId = webSocketGameService.gameInit(message.getRoomId());
                gameInitCookieSend(gameId);
                webSocketGameService.recordHistory(gameId, 17, 0, 0);
                returnState = "gameState";
                break;
            case "nextTurn":
                returnState = webSocketGameService.nextTurn();
                break;
            case "myChoice":
                returnState = webSocketGameService.myChoice();
                break;
            default:
                throw new IllegalArgumentException("웹소켓 메시지 잘못 접근함");
        }

        // 로직 이후 보낼 메시지에 state 설정
        newMessage.setState(returnState);

        // 만약 gameState라면
        if (returnState.equals("gameState")) {
            ObjectMapper objectMapper = new ObjectMapper();
            GameStateDto gameStateDto = webSocketGameService.buildGameState(null);
            // JSON 문자열이 아닌 객체를 직접 설정
            newMessage.setMainMessage(objectMapper.convertValue(gameStateDto, Map.class));
        }

        template.convertAndSend("/sub/chat/room/" + newMessage.getRoomId(), newMessage);
    }

    private void gameInitCookieSend(String gameId) {
        // cookie 설정
        MessageDto cookieSetMessage = new MessageDto(gameId, "server");
        cookieSetMessage.setState("cookieSet");
        template.convertAndSend("/sub/chat/room/" + cookieSetMessage.getRoomId(), cookieSetMessage);
    }
}
