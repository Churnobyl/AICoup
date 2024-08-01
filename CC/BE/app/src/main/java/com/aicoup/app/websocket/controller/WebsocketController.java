package com.aicoup.app.websocket.controller;

import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.websocket.model.dto.GameStateDto;
import com.aicoup.app.websocket.model.dto.MessageDto;
import com.aicoup.app.websocket.service.WebSocketGameServiceImpl;
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
        Map<String, String> validateResult = webSocketGameService.validate(message);

        if (!validateResult.get("result").equals("ok")) {
            newMessage.setState("validationFail");
            newMessage.setMainMessage(validateResult);
            template.convertAndSend("/sub/chat/room/" + message.getRoomId(), newMessage);
            return;
        }

        // state에 따라 분기 로직
        String state = message.getState();
        GameStateDto gameStateDto = null;
        String returnState = null;

        switch (state) {
            case "gameCheck":
                boolean result = webSocketGameService.gameCheck(message);
                returnState = result ? "exist" : "noExist";
                break;
            case "gameInit":
                String gameId = webSocketGameService.gameInit(message);
                gameInitCookieSend(gameId);
                returnState = "gameMade";
                break;
            case "gameState":
                returnState = "gameState";
                gameStateDto = gameStateGetter(message);
                break;
            case "nextTurn":
                returnState = webSocketGameService.nextTurn(message);
                gameStateDto = gameStateGetter(message);
                break;
            case "myChoice":
                returnState = webSocketGameService.myChoice(message);
                break;
            default:
                throw new IllegalArgumentException("웹소켓 메시지 잘못 접근함");
        }

        // 로직 이후 보낼 메시지에 state 설정
        newMessage.setState(returnState);

        // JSON 문자열이 아닌 객체를 직접 설정
        ObjectMapper objectMapper = new ObjectMapper();
        newMessage.setMainMessage(objectMapper.convertValue(gameStateDto, Map.class));

        template.convertAndSend("/sub/chat/room/" + newMessage.getRoomId(), newMessage);
    }

    private GameStateDto gameStateGetter(MessageDto message) {
        GameStateDto gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));

        // 다른 사람 카드 가리기
        for (GameMember member : gameStateDto.getMembers()) {
            if (!member.isPlayer()) {
                if (member.getLeftCard() > 0) {
                    member.setLeftCard(0);
                }
                if (member.getRightCard() > 0) {
                    member.setRightCard(0);
                }
            }
        }

        return gameStateDto;
    }

    private void gameInitCookieSend(String gameId) {
        // cookie 설정
        MessageDto cookieSetMessage = new MessageDto("1", "server");
        ObjectMapper objectMapper = new ObjectMapper();
        GameStateDto gameStateDto = new GameStateDto();
        gameStateDto.setMessage(gameId);
        cookieSetMessage.setMainMessage(objectMapper.convertValue(gameStateDto, Map.class));
        cookieSetMessage.setState("cookieSet");
        template.convertAndSend("/sub/chat/room/" + 1, cookieSetMessage);
    }
}
