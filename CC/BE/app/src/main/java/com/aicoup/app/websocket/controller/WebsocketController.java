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
    private final WebSocketGameServiceImpl webSocketGameService;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/message")
    public void handleMessage(MessageDto message) {
        String roomId = message.getRoomId();
        MessageDto newMessage = new MessageDto(roomId, "server");

        // 서버와 현실 일치 검증
        Map<String, String> validateResult = webSocketGameService.validate(message);

        if (validateResult.get("result").equals("noGame")) {
            newMessage.setState("noGame");
            newMessage.setMainMessage(validateResult);
            sendMessage(roomId, newMessage);
            return;
        }

        // state에 따라 분기 로직
        String state = message.getState();
        GameStateDto gameStateDto = null;
        String returnState = null;

        System.out.println(state);

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
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                hideOtherPlayersCards(gameStateDto);
                returnState = "gameState";
                break;
            case "nextTurn":
                returnState = webSocketGameService.nextTurn(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                hideOtherPlayersCards(gameStateDto);
                break;
            case "action":
                webSocketGameService.performPlayerAction(message);
                returnState = "actionPending";
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                break;
            case "anyChallenge":
                returnState = webSocketGameService.handleGPTChallenge(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                if(!returnState.equals("gptChallengeNone")) {
                    wrapMessage(newMessage, gameStateDto, roomId, "gptChallenge");
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "endGame";
                } else {
                }
                break;
            case "performGame":
                webSocketGameService.performAction(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                returnState = "gameState";
                break;
            case "anyCounterAction":
                returnState = webSocketGameService.handleGPTCounterAction(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                if(!returnState.equals("gptChallengeNone")) {
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                }
                break;
            case "actionProcessed":
                webSocketGameService.processAction(message);
                returnState = "actionProcessed";
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                break;
            case "mychoice":
                webSocketGameService.myChoice(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                returnState = "challengeProcessed";
                break;
            case "challenge":
                gameStateDto = webSocketGameService.handlePlayerChallenge(message);
                returnState = "challengeProcessed";
                break;
            case "counterAction":
                gameStateDto = webSocketGameService.handlePlayerCounterAction(message);
                returnState = "counterActionProcessed";
                break;
            case "permit":
                break;
            default:
                throw new IllegalArgumentException("웹소켓 메시지 잘못 접근함");
        }
        wrapMessage(newMessage, gameStateDto, roomId, returnState);
    }

    private void wrapMessage(MessageDto newMessage, GameStateDto gameStateDto, String roomId, String returnState) {
        // 로직 이후 보낼 메시지에 state 설정
        newMessage.setState(returnState);

        // gameStateDto가 null이 아닌 경우에만 mainMessage에 설정
        if (gameStateDto != null) {
            newMessage.setMainMessage(objectMapper.convertValue(gameStateDto, Map.class));
        }
        System.out.println("gameStateDto = " + gameStateDto);

        sendMessage(roomId, newMessage);
    }

    private void sendMessage(String roomId, MessageDto message) {
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, message);
    }

    private void hideOtherPlayersCards(GameStateDto gameStateDto) {
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
    }

    private void gameInitCookieSend(String gameId) {
        MessageDto cookieSetMessage = new MessageDto("1", "server");
        GameStateDto gameStateDto = new GameStateDto();
        gameStateDto.setMessage(gameId);
        cookieSetMessage.setMainMessage(objectMapper.convertValue(gameStateDto, Map.class));
        cookieSetMessage.setState("cookieSet");
        sendMessage("1", cookieSetMessage);
    }
}