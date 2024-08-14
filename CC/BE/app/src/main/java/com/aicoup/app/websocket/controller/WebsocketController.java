package com.aicoup.app.websocket.controller;

import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.pipeline.aiot.AIoTSocket;
import com.aicoup.app.websocket.model.dto.GameStateDto;
import com.aicoup.app.websocket.model.dto.MessageDto;
import com.aicoup.app.websocket.service.WebSocketGameServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final AIoTSocket aIoTSocket;

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

        System.out.println("state: "+state);

        switch (state) {
            case "gameCheck":
                boolean result = webSocketGameService.gameCheck(message);
                returnState = result ? "exist" : "noExist";
                break;
            case "gameInit":
//                aIoTSocket.gameStart();
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
                returnState = webSocketGameService.performPlayerAction(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                break;
            case "anyChallenge":
                returnState = webSocketGameService.handleGPTChallenge(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                break;
            case "performGame":
                returnState = webSocketGameService.deadCardOpen(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                break;
            case "deadCardOpen":
                returnState = webSocketGameService.performAction(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                break;
            case "anyCounterAction":
                returnState = webSocketGameService.handleGPTCounterAction(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                if(returnState.equals("gptCounterActionNone")) {
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "endGame";
                }
                break;
            case "counterActionChallenge":
                returnState = webSocketGameService.handlePlayerChallenge(message); // 수정 필요!!
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                if(returnState.equals("challengeSuccess")) {
                    returnState = "counterActionChallengeSuccess";
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "gameState";
                } else {
                    returnState = "counterActionChallengeFail";
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "endGame";
                }
                break;
            case "counterActionPermit":
                returnState = webSocketGameService.handleGPTCounterActionChallenge(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                break;
            case "challenge":
                returnState = webSocketGameService.handlePlayerChallenge(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                break;
            case "performChallenge":
                returnState = webSocketGameService.handlePlayerPerformChallenge(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                if(returnState.equals("challengeSuccess")) {
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "gameState"; // 플레이어 도전이 성공하면 액션 처리 없이 gameState
                } else {
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "endGame"; // 플레이어 도전이 실패하면 액션 처리 위해 endGame
                }
                break;
            case "cardOpen":
                returnState = webSocketGameService.handleGPTPerformChallenge(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                if(returnState.equals("challengeSuccess")) {
                    returnState = "gptChallengeSuccess";
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "gameState"; // gpt 도전이 성공하면 액션 처리 없이 gameState
                } else {
                    returnState = "gptChallengeFail";
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "endGame"; // gpt 도전이 실패하면 액션 처리 위해 endGame
                }
                break;
            case "counterActionCardOpen":
                returnState = webSocketGameService.handleGPTPerformCounterActionChallenge(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                if(returnState.equals("challengeSuccess")) {
                    returnState = "gptCounterActionChallengeSuccess";
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "gameState"; // 성공했으면 액션 처리 없이 gameState
                } else {
                    returnState = "gptCounterActionChallengeFail";
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "endGame"; // 실패했으면 액션 처리위해 endGame
                }
                break;
            case "counterAction":
                returnState = "counterActionProcessed";
                gameStateDto = webSocketGameService.handlePlayerCounterAction(message);
                break;
            case "permit":
                returnState = webSocketGameService.handleGPTChallenge(message);
                gameStateDto = webSocketGameService.buildGameState(((Map<String, String>)message.getMainMessage()).get("cookie"));
                if(returnState.equals("gptChallenge")) {
                    wrapMessage(newMessage, gameStateDto, roomId, returnState);
                    returnState = "cardOpen";
                }
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