package com.aicoup.app.websocket.client;

import com.aicoup.app.websocket.model.dto.GameState;
import com.aicoup.app.websocket.model.dto.PlayerDto;
import com.aicoup.app.websocket.model.dto.CardDto;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleClient {
    private static final Scanner scanner = new Scanner(System.in);
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String baseUrl = "http://localhost:8080/api/game";
    private static final String[] actions = {"Income", "ForeignAid", "Tax", "Steal", "Assassinate", "Exchange", "Coup"};

    public static void main(String[] args) {
        setupGame();
        while (true) {
            GameState gameState = getGameState();
            if (!gameState.isGameIsRunning()) {
                System.out.println("게임이 종료되었습니다. 승자는 " + gameState.getPlayersAlive().get(0).getName() + "입니다.");
                break;
            }
            playTurn(gameState);
        }
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

        restTemplate.postForObject(baseUrl + "/setup", playerNames, Void.class);
    }

    private static GameState getGameState() {
        return restTemplate.getForObject(baseUrl + "/state", GameState.class);
    }

    private static void playTurn(GameState gameState) {
        PlayerDto currentPlayer = gameState.getPlayers().get(gameState.getCurrentPlayerIndex());

        System.out.println("\n현재 플레이어: " + currentPlayer.getName());
        System.out.println("코인: " + currentPlayer.getCoins());
        System.out.println("카드:");
        for (CardDto card : currentPlayer.getCards()) {
            System.out.println("  - " + card.getName() + (card.isRevealed() ? " (공개)" : " (비공개)"));
        }
        System.out.println("가능한 액션:");
        for (int i = 0; i < actions.length; i++) {
            System.out.println((i + 1) + ". " + actions[i]);
        }

        System.out.println("액션 번호를 선택하세요:");
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

        try {
            restTemplate.postForObject(
                    baseUrl + "/turn?playerName=" + currentPlayer.getName() +
                            "&action=" + action + "&targetPlayerName=" + targetPlayerName,
                    null,
                    Void.class
            );
        } catch (Exception e) {
            System.out.println("오류 발생: " + e.getMessage());
        }

        // 액션 수행 후 현재 게임 상태 출력
        printGameState(getGameState());
    }

    private static void printGameState(GameState gameState) {
        System.out.println("\n현재 게임 상태:");
        for (PlayerDto player : gameState.getPlayers()) {
            System.out.println(player.getName() + " - 코인: " + player.getCoins() +
                    ", 생존: " + (player.isAlive() ? "O" : "X"));
            System.out.println("  카드:");
            for (CardDto card : player.getCards()) {
                if (card.isRevealed()) {
                    System.out.println("    - " + card.getName() + " (공개)");
                } else {
                    System.out.println("    - 비공개 카드");
                }
            }
        }
        System.out.println();
    }
}