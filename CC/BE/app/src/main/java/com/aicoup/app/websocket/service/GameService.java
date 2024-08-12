package com.aicoup.app.websocket.service;

import com.aicoup.app.pipeline.gpt.ChatGPTSocket;
import com.aicoup.app.pipeline.gpt.service.*;
import com.aicoup.app.websocket.model.dto.CardDto;
import com.aicoup.app.websocket.model.dto.GameState;
import com.aicoup.app.websocket.model.dto.PlayerDto;
import com.aicoup.app.websocket.model.dto.Action;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameService {
    private GameState gameState;
    @Autowired
    ChatGPTSocket chatGPTSocket;
    @Autowired
    ActionDataService actionDataService;

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

    public void takeTotalTurn(String playerName, String actionName, String targetPlayerName) {
        List<PlayerDto> players = gameState.getPlayers();
        takeTurn(playerName, actionName, targetPlayerName);
        for(int i = 1; i < players.size(); i++) {
            String[] actionArr = new String[2];
            actionArr = askActionToGPT();
            String player = players.get(i).getName();
            String action = actionArr[0];
            String target = actionArr[1];
            takeTurn(player, action, target);
        }
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

    public String[] askActionToGPT() throws JSONException {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and outputs what current player has to do. Take the current turn information in JSON format and output the result in JSON format.cards_open indicates whether the card has lost its influence. if input is \\\"cards\\\": [\\\"duke\\\", \\\"ambassador\\\"], \\\"cards_open\\\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before.the goal of the game is to elimate the influence card of all other players and be the last survivor.when a player lose all their influence card he lose the game.Every turn, current_player perform one action they want and can afford.- income: current_player get 1 coin.- foreign_aid: current_player get 2 coins. other duke can perform counter_action.- coup: cost 7 coins. choose one player and force to give up an influence card. if current_player start turn with 10 or more, current_player must coup.- tax: current_player get 3 coins. can be challenged.- steal: choose one player and take 2 coins. can be challeged. chosen player can perform counter_action with captain or ambassador.- exchange: draw 2 influence card. place 2 influence card back. can be challeged.- assassinate: cost 3 coins. choose one player and force to give up an influence card. can be challenged. chosen player can perform counter_action with contessa.Every counter_action, current_player'action is canceled, or current_player can challenge to player performing counter_action. if challenge is success, counter_action is canceled.Every challenge, the player who lose challenge is forced to give up an influence card.";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = actionDataService.getFormattedGameDataAsJson("1");

        // API 호출
        String dataFromGptApiForAction = chatGPTSocket.getDataFromGptApiForAction(systemPrompt, userPrompt);

        // 결과 출력
        System.out.println("dataFromGptApiForAction = " + dataFromGptApiForAction);

        // JSONObject 생성
        JSONObject jsonObject = new JSONObject(dataFromGptApiForAction);

        // 키값과 밸류값 추출
        String action = jsonObject.getString("action");
        String target = jsonObject.getString("target");
        String[] actionArr = new String[2];
        actionArr[0] = action;
        actionArr[1] = target;
        System.out.println(dataFromGptApiForAction);
        return actionArr;
    }

    public String askChallengeToGPT() throws JSONException {
        String systemPrompt = "You are an API that receives information of every turn of the Coup board game and current_player's action and target. you should output which player should challenge as a challenger for current_player's action. if there is no proper challenger, you should ouput \\\"none\\\". Take information in JSON format and output the result in JSON format.cards_open indicates whether the card has lost its influence. if input is \\\"cards\\\": [\\\"duke\\\", \\\"ambassador\\\"], \\\"cards_open\\\": [true, false] means that the duke has lost its influence, and ambassador is influential. coins shows how much coins each player has. history shows what each player acts before. history show what action was taken by each player. if input is \\\"history\\\": {\\\"1\\\": [\\\"tax\\\", \\\"exchange\\\", \\\"steal\\\"],\\\"2\\\": [\\\"steal\\\", \\\"steal\\\", \\\"steal\\\"],\\\"3\\\": [\\\"tax\\\", \\\"tax\\\"],\\\"4\\\": [\\\"income\\\", \\\"assassinate\\\"]} and current_player is 1, current_player's last two action is exchange and steal.any other player can challenge to a current_player regardless of whether they are the involved in action.player may be telling the truth or bluffing.the goal of the game is to elimate the influence card of all other players and be the last survivor.when a player lose all their influence card he lose the game.whoever loses the challenge immediately loses an influence card.challenger is usually a player in order of player who is target, player who has most influence card.it is suspicious if current_action is not match with the recent actions.";

        // 데이터베이스에서 게임 데이터를 JSON 형식으로 가져오기
        String userPrompt = actionDataService.getFormattedGameDataAsJson("2");

        // API 호출
        String dataFromGptApiForChallenge = chatGPTSocket.getDataFromGptApiForAction(systemPrompt, userPrompt);

        // 결과 출력
        System.out.println("dataFromGptApiForChallenge = " + dataFromGptApiForChallenge);

        // JSONObject 생성
        JSONObject jsonObject = new JSONObject(dataFromGptApiForChallenge);

        // 키값과 밸류값 추출
        String challenger = jsonObject.getString("challenger");
        System.out.println(dataFromGptApiForChallenge);
        return challenger;
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
            case "coup":
                if (player.getCoins() < 7) {
                    throw new IllegalStateException("At least 7 coins are required for Coup.");
                }
                validateTarget(player, targetPlayerName);
                break;
            case "assassinate":
                if (player.getCoins() < 3) {
                    throw new IllegalStateException("At least 3 coins are required for Assassination.");
                }
                validateTarget(player, targetPlayerName);
                break;
            case "steal":
                PlayerDto target = validateTarget(player, targetPlayerName);
                if (target.getCoins() == 0) {
                    throw new IllegalStateException("Cannot steal from a player with no coins.");
                }
                break;
            case "tax":
            case "foreign_aid":
            case "income":
                if(Objects.equals(targetPlayerName, "")) break;
                else if (!Objects.equals(targetPlayerName, "none")) {
                    throw new IllegalStateException("This action does not require a target.");
                }
                break;
            case "exchange":
                if(Objects.equals(targetPlayerName, "")) break;
                else if (!Objects.equals(targetPlayerName, "none")) {
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
            case "income" -> new Action("income", 0, false);
            case "foreign_aid" -> new Action("foreign_aid", 0, false);
            case "coup" -> new Action("coup", 7, true);
            case "tax" -> new Action("tax", 0, false);
            case "assassinate" -> new Action("assassinate", 3, true);
            case "steal" -> new Action("steal", 0, true);
            case "exchange" -> new Action("exchange", 0, false);
            default -> throw new IllegalArgumentException("Invalid action name: " + actionName);
        };
    }

    private void performAction(PlayerDto player, Action action, PlayerDto target) {
        switch(action.getName()) {
            case "income":
                player.setCoins(player.getCoins() + 1);
                break;
            case "foreign_aid":
                player.setCoins(player.getCoins() + 2);
                break;
            case "coup":
                player.setCoins(player.getCoins() - 7);
                if (target != null) {
                    loseInfluence(target);
                }
                break;
            case "tax":
                player.setCoins(player.getCoins() + 3);
                break;
            case "assassinate":
                player.setCoins(player.getCoins() - 3);
                if (target != null) {
                    loseInfluence(target);
                }
                break;
            case "steal":
                if (target != null) {
                    int stolenCoins = Math.min(2, target.getCoins());
                    target.setCoins(target.getCoins() - stolenCoins);
                    player.setCoins(player.getCoins() + stolenCoins);
                }
                break;
            case "exchange":
                exchangeCards(player);
                break;
            default:
                throw new IllegalArgumentException("Invalid action name: " + action.getName());
        }
    }

    private void performChallenge(PlayerDto player, Action action, PlayerDto target) {
        switch(action.getName()) {
            case "income":
            case "foreign_aid":
            case "coup":
                break;
            case "tax":
                player.setCoins(player.getCoins() + 3);
                break;
            case "assassinate":
                player.setCoins(player.getCoins() - 3);
                if (target != null) {
                    loseInfluence(target);
                }
                break;
            case "steal":
                if (target != null) {
                    int stolenCoins = Math.min(2, target.getCoins());
                    target.setCoins(target.getCoins() - stolenCoins);
                    player.setCoins(player.getCoins() + stolenCoins);
                }
                break;
            case "exchange":
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
