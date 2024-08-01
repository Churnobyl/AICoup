package com.aicoup.app.websocket.service;

import com.aicoup.app.websocket.model.dto.CardDto;
import com.aicoup.app.websocket.model.dto.GameState;
import com.aicoup.app.websocket.model.dto.PlayerDto;
import com.aicoup.app.websocket.model.dto.Action;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class GameService {
    private final GameState gameState = GameState.getInstance();

    public void setupGame(List<String> playerNames) {
        gameState = new GameState();
        gameState.setPlayers(new ArrayList<>());
        gameState.setPlayersAlive(new ArrayList<>());
        gameState.setGameIsRunning(true);
        gameState.setCurrentPlayerIndex(0);

        initializeDeck();

        for (String name : playerNames) {
            PlayerDto player = new PlayerDto();
            player.setName(name);
            player.setCards(gameState.drawCards(2));
            player.setCoins(2);
            player.setAlive(true);
            gameState.getPlayers().add(player);
            gameState.getPlayersAlive().add(player);
        }
    }

    private void initializeDeck() {
        List<CardDto> deck = new ArrayList<>();
        String[] cardTypes = {"Duke", "Assassin", "Ambassador", "Captain", "Contessa"};
        for (String cardType : cardTypes) {
            for (int i = 0; i < 3; i++) {
                deck.add(new CardDto(cardType, false));
            }
        }
        Collections.shuffle(deck);
        gameState.setDeck(deck);
    }

    public void takeTurn(int playerIndex, String actionName, String targetPlayerName) {
        PlayerDto player = gameState.getPlayers().get(playerIndex);
        Action action = getActionByName(actionName);

        // 액션 유효성 검증
        validateAction(player, action, targetPlayerName);

        PlayerDto target = null;
        if (action.isHasTarget()) {
            target = findPlayerByName(targetPlayerName)
                    .orElseThrow(() -> new IllegalArgumentException("Target player not found."));
        }

        // 액션 로직 실행
        performAction(player, action, target);

        // 턴 종료 후 상태 업데이트
        updateGameState();
    }

    private void validateAction(PlayerDto player, Action action, String targetPlayerName) {
        // 충분한 코인이 있는지 검증
        if (action.getCoinsNeeded() > player.getCoins()) {
            throw new IllegalStateException("Not enough coins to perform the action.");
        }

        // 타겟이 필요한 액션인 경우 타겟 플레이어 존재 여부 검증
        if (action.isHasTarget() && (targetPlayerName == null || targetPlayerName.isEmpty())) {
            throw new IllegalArgumentException("Target player is required for this action.");
        }

        // 특정 액션에 대한 추가 검증
        switch (action.getName()) {
            case "Coup":
                if (player.getCoins() >= 10 && !action.getName().equals("Coup")) {
                    throw new IllegalStateException("Player must perform Coup when having 10 or more coins.");
                }
                break;
            case "Assassinate":
                if (player.getCoins() < 3) {
                    throw new IllegalStateException("At least 3 coins are required for Assassination.");
                }
                break;
            case "Steal":
                PlayerDto target = findPlayerByName(targetPlayerName)
                        .orElseThrow(() -> new IllegalArgumentException("Target player not found."));
                if (target.getCoins() == 0) {
                    throw new IllegalStateException("Cannot steal from a player with no coins.");
                }
                break;
            case "Tax":
                // Tax 액션에 대한 특별한 검증이 필요하다면 여기에 추가
                break;
            case "Income":
            case "ForeignAid":
            case "Exchange":
                // 이 액션들에 대한 특별한 검증이 필요하다면 여기에 추가
                break;
            default:
                throw new IllegalArgumentException("Invalid action name: " + action.getName());
        }
    }

    private Action getActionByName(String actionName) {
        switch(actionName) {
            case "Income":
                return new Action("Income", 0, false);
            case "ForeignAid":
                return new Action("ForeignAid", 0, false);
            case "Coup":
                return new Action("Coup", 7, true);
            case "Tax":
                return new Action("Tax", 0, false);
            case "Assassinate":
                return new Action("Assassinate", 3, true);
            case "Steal":
                return new Action("Steal", 0, true);
            case "Exchange":
                return new Action("Exchange", 0, false);
            default:
                throw new IllegalArgumentException("Invalid action name: " + actionName);
        }
    }

    private void performAction(PlayerDto player, Action action, PlayerDto target) {
        switch(action.getName()) {
            case "Income":
                player.setCoins(player.getCoins() + 1);
                break;
            case "ForeignAid":
                player.setCoins(player.getCoins() + 2);
                break;
            case "Coup":
                player.setCoins(player.getCoins() - 7);
                if (target != null) {
                    String cardToRemove = chooseCardToRemove(target);
                    target.removeCard(cardToRemove);
                    if (target.getCards().isEmpty()) {
                        gameState.getPlayersAlive().remove(target);
                    }
                }
                break;
            case "Tax":
                player.setCoins(player.getCoins() + 3);
                break;
            case "Assassinate":
                player.setCoins(player.getCoins() - 3);
                if (target != null) {
                    String cardToRemove = chooseCardToRemove(target);
                    target.removeCard(cardToRemove);
                    if (target.getCards().isEmpty()) {
                        gameState.getPlayersAlive().remove(target);
                    }
                }
                break;
            case "Steal":
                if (target != null) {
                    int stolenCoins = Math.min(2, target.getCoins());
                    target.setCoins(target.getCoins() - stolenCoins);
                    player.setCoins(player.getCoins() + stolenCoins);
                }
                break;
            case "Exchange":
                exchangeCards(player);
                break;
            default:
                throw new IllegalArgumentException("Invalid action name: " + action.getName());
        }
    }

    private String chooseCardToRemove(PlayerDto target) {
        // 실제 게임에서는 플레이어가 제거할 카드를 선택해야 합니다.
        // 여기서는 간단히 첫 번째 카드를 제거하는 것으로 구현합니다.
        return target.getCards().get(0);
    }

    private void exchangeCards(PlayerDto player) {
        List<CardDto> drawnCards = gameState.drawCards(2);
        List<CardDto> allCards = new ArrayList<>(player.getCards());
        allCards.addAll(drawnCards);

        // 여기서 플레이어가 카드를 선택하는 로직이 필요합니다.
        // 임시로 처음 2장을 선택한다고 가정합니다.
        List<CardDto> selectedCards = new ArrayList<>(allCards.subList(0, 2));

        player.setCards(selectedCards);
        gameState.returnCardsToDeck(new ArrayList<>(allCards.subList(2, 4)));
    }

    private void updateGameState() {
        int currentPlayerIndex = gameState.getCurrentPlayerIndex();
        currentPlayerIndex = (currentPlayerIndex + 1) % gameState.getPlayers().size();
        gameState.setCurrentPlayerIndex(currentPlayerIndex);

        List<PlayerDto> playersAlive = gameState.getPlayersAlive();
        playersAlive.removeIf(player -> player.getCards().isEmpty());

        if (playersAlive.size() <= 1) {
            gameState.setGameIsRunning(false);
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    private Optional<PlayerDto> findPlayerByName(String name) {
        return gameState.getPlayers().stream()
                .filter(player -> player.getName().equals(name))
                .findFirst();
    }
}
