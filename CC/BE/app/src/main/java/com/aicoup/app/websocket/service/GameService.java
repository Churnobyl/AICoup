package com.aicoup.app.websocket.service;

import com.aicoup.app.websocket.model.dto.CardDto;
import com.aicoup.app.websocket.model.dto.GameState;
import com.aicoup.app.websocket.model.dto.PlayerDto;
import com.aicoup.app.websocket.model.dto.Action;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {
    private GameState gameState;

    public void setupGame(List<String> playerNames) {
        gameState = GameState.getInstance();
        gameState.setPlayers(new ArrayList<>());
        gameState.setPlayersAlive(new ArrayList<>());
        gameState.setGameIsRunning(true);
        gameState.setCurrentPlayerIndex(0);

        initializeDeck();

        for (String name : playerNames) {
            PlayerDto player = new PlayerDto();
            player.setName(name);
            player.setCards(gameState.drawCards(2));
            gameState.getPlayers().add(player);
            gameState.getPlayersAlive().add(player);
        }
    }

    private void initializeDeck() {
        List<CardDto> deck = new ArrayList<>();
        String[] cardTypes = {"Duke", "Assassin", "Ambassador", "Captain", "Contessa"};
        for (String cardType : cardTypes) {
            for (int i = 0; i < 3; i++) {
                deck.add(new CardDto(cardType, false, false));
            }
        }
        Collections.shuffle(deck);
        gameState.setDeck(deck);
    }

    public void takeTurn(String playerName, String actionName, String targetPlayerName) {
        int playerIndex = getPlayerIndexByName(playerName);
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
                if (player.getCoins() < 7) {
                    throw new IllegalStateException("At least 7 coins are required for Coup.");
                }
                validateTarget(player, targetPlayerName);
                break;
            case "Assassinate":
                if (player.getCoins() < 3) {
                    throw new IllegalStateException("At least 3 coins are required for Assassination.");
                }
                validateTarget(player, targetPlayerName);
                break;
            case "Steal":
                PlayerDto target = validateTarget(player, targetPlayerName);
                if (target.getCoins() == 0) {
                    throw new IllegalStateException("Cannot steal from a player with no coins.");
                }
                break;
            case "Tax":
            case "ForeignAid":
            case "Income":
                if (targetPlayerName != null && !targetPlayerName.isEmpty()) {
                    throw new IllegalStateException("This action does not require a target.");
                }
                break;
            case "Exchange":
                if (targetPlayerName != null && !targetPlayerName.isEmpty()) {
                    throw new IllegalStateException("Exchange does not require a target.");
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid action name: " + action.getName());
        }
    }

    private PlayerDto validateTarget(PlayerDto player, String targetPlayerName) {
        if (targetPlayerName == null || targetPlayerName.isEmpty()) {
            throw new IllegalArgumentException("Target player is required for this action.");
        }

        PlayerDto target = findPlayerByName(targetPlayerName)
                .orElseThrow(() -> new IllegalArgumentException("Target player not found: " + targetPlayerName));

        if (Objects.equals(target.getName(), player.getName())) {
            throw new IllegalStateException("Cannot target yourself.");
        }

        if (!target.isAlive()) {
            throw new IllegalStateException("Cannot target a player who is out of the game.");
        }

        return target;
    }

    private Action getActionByName(String actionName) {
        return switch (actionName) {
            case "Income" -> new Action("Income", 0, false);
            case "ForeignAid" -> new Action("ForeignAid", 0, false);
            case "Coup" -> new Action("Coup", 7, true);
            case "Tax" -> new Action("Tax", 0, false);
            case "Assassinate" -> new Action("Assassinate", 3, true);
            case "Steal" -> new Action("Steal", 0, true);
            case "Exchange" -> new Action("Exchange", 0, false);
            default -> throw new IllegalArgumentException("Invalid action name: " + actionName);
        };
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
                    loseInfluence(target);
                }
                break;
            case "Tax":
                player.setCoins(player.getCoins() + 3);
                break;
            case "Assassinate":
                player.setCoins(player.getCoins() - 3);
                if (target != null) {
                    loseInfluence(target);
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

    private void loseInfluence(PlayerDto target) {
        // 실제 게임에서는 플레이어가 제거할 카드를 선택해야 합니다.
        // 여기서는 간단히 첫 번째 비공개 카드를 제거하는 것으로 구현합니다.
        CardDto cardToLoseInfluence = target.getCards().stream()
                .filter(card -> !card.isInfluenceLost())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No cards left to lose influence"));

        cardToLoseInfluence.setRevealed(true);
        cardToLoseInfluence.setInfluenceLost(true);

        if (target.getCards().stream().allMatch(CardDto::isInfluenceLost)) {
            target.setAlive(false);
            gameState.getPlayersAlive().remove(target);
        }
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

    private int getPlayerIndexByName(String playerName) {
        List<PlayerDto> players = gameState.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(playerName)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Player not found: " + playerName);
    }

    private Optional<PlayerDto> findPlayerByName(String name) {
        return gameState.getPlayers().stream()
                .filter(player -> player.getName().equals(name))
                .findFirst();
    }
}
