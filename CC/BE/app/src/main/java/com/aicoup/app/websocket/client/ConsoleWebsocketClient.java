package com.aicoup.app.websocket.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ConsoleWebsocketClient {
    private static final String serverUri = "ws://localhost:8080/ws-coup";
    private static final String[] actions = {"Income", "ForeignAid", "Tax", "Steal", "Assassinate", "Exchange", "Coup"};
    private static final Scanner scanner = new Scanner(System.in);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static StompSession stompSession;
    private static String gameId;

    public static void main(String[] args) {
        try {
            WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
            stompClient.setMessageConverter(new MappingJackson2MessageConverter());

            StompSessionHandler sessionHandler = new CustomStompSessionHandler();
            stompSession = stompClient.connect(serverUri, sessionHandler).get();

            System.out.println("Connected to server");
            setupGame();

            // Keep the main thread running
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static List<Transport> createTransportClient() {
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        return transports;
    }

    private static void setupGame() {
        System.out.println("플레이어 수를 입력하세요 (2-6):");
        int playerCount = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 소비

        List<String> playerNames = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            System.out.println("플레이어 " + (i + 1) + "의 이름을 입력하세요:");
            playerNames.add(scanner.nextLine());
        }

        Map<String, Object> message = new HashMap<>();
        message.put("roomId", "1");
        message.put("writer", "client");
        message.put("state", "gameInit");
        message.put("mainMessage", playerNames);

        sendMessage("/app/game/action", message);
    }

    private static void playTurn(Map<String, Object> gameState) {
        Map<String, Object> currentPlayer = (Map<String, Object>) ((List<?>) gameState.get("members")).get((int) gameState.get("whoseTurn"));

        System.out.println("\n현재 플레이어: " + currentPlayer.get("name"));
        System.out.println("코인: " + currentPlayer.get("coin"));
        System.out.println("카드:");
        printCards(currentPlayer);

        System.out.println("가능한 액션:");
        for (int i = 0; i < actions.length; i++) {
            System.out.print((i + 1) + ". " + actions[i] + " ");
        }

        System.out.println("\n" + "액션 번호를 선택하세요:");
        int actionIndex = scanner.nextInt();
        scanner.nextLine(); // 개행 문자 소비

        if (actionIndex < 1 || actionIndex > actions.length) {
            System.out.println("잘못된 액션 번호입니다. 다시 선택해주세요.");
            playTurn(gameState);
            return;
        }

        String action = actions[actionIndex - 1];

        String targetPlayerName = "";
        if (action.equals("Coup") || action.equals("Assassinate") || action.equals("Steal")) {
            System.out.println("타겟 플레이어의 이름을 입력하세요:");
            targetPlayerName = scanner.nextLine();
        }

        Map<String, Object> message = new HashMap<>();
        message.put("roomId", "1");
        message.put("writer", currentPlayer.get("name"));
        message.put("state", "action");
        Map<String, String> mainMessage = new HashMap<>();
        mainMessage.put("action", action);
        mainMessage.put("targetPlayerName", targetPlayerName);
        mainMessage.put("cookie", gameId);
        message.put("mainMessage", mainMessage);

        sendMessage("/app/game/action", message);
    }

    private static void printGameState(Map<String, Object> gameState) {
        System.out.println("\n현재 게임 상태:");
        List<Map<String, Object>> players = (List<Map<String, Object>>) gameState.get("members");
        for (Map<String, Object> player : players) {
            System.out.println(player.get("name") + " - 코인: " + player.get("coin") +
                    ", 생존: " + (player.get("isPlayer").equals(true) ? "O" : "X"));
            System.out.println("  카드:");
            printCards(player);
        }
        System.out.println();
    }

    private static void printCards(Map<String, Object> player) {
        Map<String, Object> leftCard = (Map<String, Object>) player.get("leftCardInfo");
        Map<String, Object> rightCard = (Map<String, Object>) player.get("rightCardInfo");

        if (leftCard != null) {
            System.out.println("    - " + leftCard.get("name") + ((int)player.get("leftCard") < 0 ? " (공개)" : " (비공개)"));
        }
        if (rightCard != null) {
            System.out.println("    - " + rightCard.get("name") + ((int)player.get("rightCard") < 0 ? " (공개)" : " (비공개)"));
        }
    }

    private static void sendMessage(String destination, Object payload) {
        try {
            stompSession.send(destination, payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class CustomStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("STOMP Connection Established");
            session.subscribe("/topic/game/1", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return Map.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    try {
                        Map<String, Object> messageMap = (Map<String, Object>) payload;
                        String state = (String) messageMap.get("state");
                        Map<String, Object> mainMessage = (Map<String, Object>) messageMap.get("mainMessage");

                        switch (state) {
                            case "gameMade":
                                gameId = (String) mainMessage.get("message");
                                System.out.println("Game created with ID: " + gameId);
                                break;
                            case "gameState":
                                printGameState(mainMessage);
                                playTurn(mainMessage);
                                break;
                            case "actionProcessed":
                                System.out.println("Action processed successfully");
                                break;
                            case "error":
                                System.out.println("Error: " + mainMessage.get("error"));
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            System.out.println("Error: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}