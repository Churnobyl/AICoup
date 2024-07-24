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

    @MessageMapping(value = "/chat/enter")
    public void enter(MessageDto message) {
        message.setMessage(message.getWriter() + "님이 채팅방에 참여하였습니다.");
        template.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }

    @MessageMapping(value = "/chat/message")
    public void message(MessageDto message) throws JsonProcessingException {
        MessageDto newMessage = new MessageDto();
        newMessage.setWriter("server");
        newMessage.setRoomId(message.getRoomId());


        // 서버와 현실 일치 검증
        if (!webSocketGameService.validate()) {
            newMessage.setState("validationFail");
            newMessage.setMessage("검증 실패");
            template.convertAndSend("/sub/chat/room/" + message.getRoomId(), newMessage);
            return;
        }

        String state = message.getState();

        String returnState = null;

        switch (state) {
            case "gameInit":
                String gameId = webSocketGameService.gameInit(message.getRoomId());
                gameInitCookieSend(gameId);
                returnState = "gameState";
                break;
            case "nextTurn":
//                returnState = webSocketGameService.nextTurn();
                break;
            case "myChoice":
//                returnState = webSocketGameService.myChoice();
                break;
            default:
                throw new IllegalArgumentException("웹소켓 메시지 잘못 접근함");
        }

        newMessage.setState(returnState);
        
        if (returnState.equals("gameState")) {
            ObjectMapper objectMapper = new ObjectMapper();
            GameStateDto gameStateDto = webSocketGameService.buildGameState("ㅎㅇ");
            // JSON 문자열이 아닌 객체를 직접 설정
            newMessage.setMessage(objectMapper.convertValue(gameStateDto, Map.class));
        }

        template.convertAndSend("/sub/chat/room/" + newMessage.getRoomId(), newMessage);
    }

    private void gameInitCookieSend(String gameId) {
        // cookie 설정
        MessageDto cookieSetMessage = new MessageDto();
        cookieSetMessage.setState("cookieSet");
        cookieSetMessage.setWriter("server");
        cookieSetMessage.setRoomId(gameId);
        template.convertAndSend("/sub/chat/room/" + cookieSetMessage.getRoomId(), cookieSetMessage);
    }
}
