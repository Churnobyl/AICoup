package com.aicoup.app.websocket.service;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.action.Action;
import com.aicoup.app.domain.entity.game.card.CardInfo;
import com.aicoup.app.domain.entity.game.history.History;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.redisRepository.GameMemberRepository;
import com.aicoup.app.domain.redisRepository.GameRepository;
import com.aicoup.app.domain.repository.ActionRepository;
import com.aicoup.app.domain.repository.CardInfoRepository;
import com.aicoup.app.domain.repository.PossibleActionRepository;
import com.aicoup.app.pipeline.aiot.AIoTSocket;
import com.aicoup.app.pipeline.aiot.dto.MMResponse;
import com.aicoup.app.pipeline.gpt.service.GPTResponseGetter;
import com.aicoup.app.websocket.model.dto.GameStateDto;
import com.aicoup.app.websocket.model.dto.MessageDto;
import com.aicoup.app.domain.game.GameGenerator;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.server.WebSocketService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebSocketGameServiceImpl implements WebSocketGameService {

    private final GameGenerator gameGenerator;
    private final GameRepository gameRepository;
    private final GameMemberRepository gameMemberRepository;
    private final CardInfoRepository cardInfoRepository;
    private final ActionRepository actionRepository;
    private final PossibleActionRepository possibleActionRepository;
    private final AIoTSocket aIoTSocket;
    private final GPTResponseGetter gptResponseGetter;
    private final SimpMessagingTemplate messagingTemplate;

    public enum ActionType {
        INCOME(1, "income"),
        FOREIGN_AID(2, "foreign_aid"),
        TAX(3, "tax"),
        STEAL(4, "steal"),
        ASSASSINATE(5, "assassinate"),
        EXCHANGE(6, "exchange"),
        COUP(7, "coup"),
        CHALLENGE(8, "challenge"),
        PERMIT(9, "permit"),
        BLOCK_DUKE(10, "block_duke"),
        BLOCK_CAPTAIN(11, "block_captain"),
        BLOCK_AMBASSADOR(12, "block_ambassador"),
        BLOCK_CONTESSA(13, "block_contessa");

        private final int value;
        private final String name;

        ActionType(int value, String name) {
            this.value = value;
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public static String findActionName(int actionValue) {
            for (ActionType action : values()) {
                if (action.getValue() == actionValue) {
                    return action.getName();
                }
            }
            throw new IllegalArgumentException("Invalid action value: " + actionValue);
        }

        public static ActionType fromActionValue(int actionValue) {
            for (ActionType action : values()) {
                if (action.getValue() == actionValue) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Invalid action value: " + actionValue);
        }

        public static int findActionValue(String actionName) {
            for (ActionType action : values()) {
                if (action.getName().equalsIgnoreCase(actionName)) {
                    return action.getValue();
                }
            }
            throw new IllegalArgumentException("Invalid action name: " + actionName);
        }
    }

    // 플레이어가 GPT인지 확인하는 메서드
    private boolean isGPTPlayer(GameMember player) {
        return player.getName().startsWith("GPT");
    }

    public GameStateDto performPlayerAction(MessageDto message) {
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        String gameId = mainMessage.get("cookie");
        String playerName = message.getWriter();
        int actionValue = Integer.parseInt(mainMessage.get("action"));
        String targetPlayerName = mainMessage.get("targetPlayerName");

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        String actionName = ActionType.findActionName(actionValue);
        validateAction(gameId, playerName, actionName);

        return processAction(game, playerName, actionValue, targetPlayerName);
    }

    public GameStateDto performGPTAction(String gameId, GameMember currentPlayer) {
        String[] actionResult = gptResponseGetter.actionApi(gameId);
        String action = actionResult[0];
        String target = actionResult[1];

        int actionValue = ActionType.findActionValue(action);

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        return processAction(game, currentPlayer.getName(), actionValue, target);
    }

    private GameStateDto processAction(Game game, String playerName, int actionValue, String targetPlayerName) {
        GameMember player = findPlayerByName(game, playerName);

        // 1단계: 액션 유효성 검사 및 초기 처리
        game.setCurrentActionState("ACTION_PENDING");
        game.setCurrentAction(actionValue);
        game.setCurrentPlayerName(playerName);
        game.setCurrentTargetName(targetPlayerName);
        gameRepository.save(game);

        // 2단계: 챌린지 기회 제공
        if (shouldPerformChallenge(actionValue)) {
            offerPlayerChallenge(game, actionValue);
            return buildGameState(game.getId());
        }

        // 3단계: 액션 완료 (챌린지가 필요 없는 경우)
        return completePlayerAction(game, player.getName(), actionValue, targetPlayerName);
    }

    @Override
    public Map<String, String> validate(MessageDto message) {
        Map<String, String> returnMessage = new HashMap<>();
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();

        if (mainMessage.get("cookie") == null) { // 쿠키 없으면 새 게임
          returnMessage.put("result", "ok");
          returnMessage.put("message", "새 게임");
          return returnMessage;
        } else if (!gameRepository.existsById(mainMessage.get("cookie"))) { // 쿠키는 있는데 게임 없으면
            returnMessage.put("result", "fail");
            returnMessage.put("message", "유효하지 않은 게임입니다.");
            return returnMessage;
        }

        // AIoT 검증 로직
        Game game = gameRepository.findById(mainMessage.get("cookie")).get();
        List<GameMember> members = game.getMemberIds().stream()
                .map(gameMemberRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<MMResponse> dataFromAIoTServer = aIoTSocket.getDataFromAIoTServer();
        for (int i = 0; i < members.size(); i++) {
            if (!validateMemberCards(members.get(i), dataFromAIoTServer.get(i))) {
                returnMessage.put("result", "fail");
                returnMessage.put("message", members.get(i).getName() + "님의 카드 상태가 서버와 다릅니다.");
                return returnMessage;
            }
        }

        returnMessage.put("result", "ok");
        returnMessage.put("message", "");
        return returnMessage;
    }

    private boolean validateMemberCards(GameMember member, MMResponse aiotData) {
        return (member.getLeftCard() > 0 && Objects.equals(member.getLeftCard(), aiotData.getLeft_card())) ||
                (member.getLeftCard() < 0 && aiotData.getLeft_card() == 0) &&
                        (member.getRightCard() > 0 && Objects.equals(member.getRightCard(), aiotData.getRight_card())) ||
                (member.getRightCard() < 0 && aiotData.getRight_card() == 0);
    }

    private GameStateDto completePlayerAction(Game game, String playerName, int actionValue, String targetPlayerName) {
        GameMember currentPlayer = findPlayerByName(game, playerName);

        // 액션 수행
        performAction(game, currentPlayer, actionValue, targetPlayerName);

        // 턴 종료 및 다음 플레이어로 이동
        game.setWhoseTurn((game.getWhoseTurn() + 1) % game.getMemberIds().size());
        game.setTurn(game.getTurn() + 1);

        gameRepository.save(game);

        return buildGameState(game.getId());
    }

    // 챌린지와 카운터 액션을 처리하는 메서드
    private void handleChallengeAndCounterAction(Game game, int actionValue) {
        // 챌린지 처리
        if (shouldPerformChallenge(actionValue)) {
            boolean playerChallenged = offerPlayerChallenge(game, actionValue);
            if (!playerChallenged) {
                handleGPTChallenge(game, actionValue);
            }
        }

        // 카운터 액션 처리
        if (shouldPerformCounterAction(actionValue)) {
            boolean playerCountered = offerPlayerCounterAction(game, actionValue);
            if (!playerCountered) {
                handleGPTCounterAction(game, actionValue);
            }
        }
    }

    private boolean offerPlayerChallenge(Game game, int actionValue) {
        game.setAwaitingChallenge(true);
        game.setAwaitingChallengeActionValue(actionValue);
        gameRepository.save(game);
        checkAndNotifyChallenge(game, "1");
        return false;
    }

    private boolean offerPlayerCounterAction(Game game, int actionValue) {
        game.setAwaitingCounterAction(true);
        game.setAwaitingCounterActionValue(actionValue);
        gameRepository.save(game);
        checkAndNotifyChallenge(game, "1");
        return false;
    }

    public void checkAndNotifyChallenge(Game game, String roomId) {
        if (game.isAwaitingChallenge()) {
            MessageDto challengeNotification = new MessageDto(roomId, "server");
            challengeNotification.setState("awaitingChallenge");
            challengeNotification.setMainMessage(Map.of(
                    "actionValue", game.getAwaitingChallengeActionValue(),
                    "message", "Challenge opportunity available"
            ));
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, challengeNotification);
        }
    }

    public void checkAndNotifyCounterAction(Game game, String roomId) {
        if (game.isAwaitingCounterAction()) {
            MessageDto counterActionNotification = new MessageDto(roomId, "server");
            counterActionNotification.setState("awaitingCounterAction");
            counterActionNotification.setMainMessage(Map.of(
                    "actionValue", game.getAwaitingCounterActionValue(),
                    "message", "Counter action opportunity available"
            ));
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, counterActionNotification);
        }
    }
    private void handleGPTChallenge(Game game, int actionValue) {
        String[] challengeResult = gptResponseGetter.challengeApi(game.getId());
        String challenger = challengeResult[0];
        if (!"none".equals(challenger)) {
            challenge(game.getId(), challenger, actionValue);
        }
    }

    private void handleGPTCounterAction(Game game, int actionValue) {
        String[] counterActionResult = gptResponseGetter.counterActionApi(game.getId());
        String counterActioner = counterActionResult[0];
        String counterAction = counterActionResult[1];
        if (!"none".equals(counterActioner)) {
            int counterActionValue = convertActionToValue(counterAction);
            counterAction(game.getId(), counterActioner, counterActionValue);

            boolean playerChallengedCounter = offerPlayerChallenge(game, counterActionValue);
            if (!playerChallengedCounter) {
                handleGPTChallengeAgainstCounter(game, counterActionValue);
            }
        }
    }

    private void handleGPTChallengeAgainstCounter(Game game, int counterActionValue) {
        String[] counterActionChallengeResult = gptResponseGetter.counterActionChallengeApi(game.getId());
        String counterActionChallenger = counterActionChallengeResult[0];
        if (!"none".equals(counterActionChallenger)) {
            challenge(game.getId(), counterActionChallenger, counterActionValue);
        }
    }

    public GameStateDto handlePlayerChallenge(MessageDto message) {
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        String gameId = mainMessage.get("cookie");
        String challengerName = message.getWriter();
        boolean isChallenge = Boolean.parseBoolean(mainMessage.get("isChallenge"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (!game.isAwaitingChallenge()) {
            throw new IllegalStateException("Challenge is not allowed at this time");
        }

        GameStateDto gameState;
        if (isChallenge) {
            int actionValue = game.getAwaitingChallengeActionValue();
            gameState = challenge(gameId, challengerName, actionValue);

            boolean challengeSuccessful = gameState.isChallengeSuccessful();

            if (challengeSuccessful) {
                game.setCurrentActionState("CHALLENGE_SUCCESSFUL");
            } else {
                GameMember player = findPlayerByName(game, game.getCurrentPlayerName());
                gameState = completePlayerAction(game, player.getName(), game.getCurrentAction(), game.getCurrentTargetName());
            }
        } else {
            GameMember player = findPlayerByName(game, game.getCurrentPlayerName());
            gameState = completePlayerAction(game, player.getName(), game.getCurrentAction(), game.getCurrentTargetName());
        }

        game.setAwaitingChallenge(false);
        game.setAwaitingChallengeActionValue(null);
        gameRepository.save(game);

        return gameState;
    }

    public GameStateDto handlePlayerCounterAction(MessageDto message) {
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        String gameId = mainMessage.get("cookie");
        String counterActorName = message.getWriter();
        boolean isCounterAction = Boolean.parseBoolean(mainMessage.get("isCounterAction"));

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (!game.isAwaitingCounterAction()) {
            throw new IllegalStateException("Counter action is not allowed at this time");
        }

        GameStateDto gameState;
        if (isCounterAction) {
            int actionValue = Integer.parseInt(mainMessage.get("action"));
            gameState = counterAction(gameId, counterActorName, actionValue);

            // 카운터 액션에 대한 챌린지 기회 제공
            offerPlayerChallenge(game, actionValue);
        } else {
            gameState = buildGameState(gameId);
        }

        game.setAwaitingCounterAction(false);
        game.setAwaitingCounterActionValue(null);
        gameRepository.save(game);

        return gameState;
    }

    // 액션 문자열을 숫자로 변환하는 메서드
    private int convertActionToValue(String action) {
        switch (action.toLowerCase()) {
            case "income": return 1;
            case "foreign_aid": return 2;
            case "tax": return 3;
            case "steal": return 4;
            case "assassinate": return 5;
            case "exchange": return 6;
            case "coup": return 7;
            default: throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    // 챌린지를 수행해야 하는지 확인하는 메서드
    private boolean shouldPerformChallenge(int actionValue) {
        return actionValue == 3 || actionValue == 4 || actionValue == 5 || actionValue == 6;
    }

    // 카운터 액션을 수행해야 하는지 확인하는 메서드
    private boolean shouldPerformCounterAction(int actionValue) {
        return actionValue == 2 || actionValue == 4 || actionValue == 5;
    }

    public void validateAction(String gameId, String playerName, String actionName) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        GameMember player = findPlayerByName(game, playerName);
        Action action = actionRepository.findByEnglishName(actionName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid action: " + actionName));

        int requiredCoins = getRequiredCoinsForAction(actionName);
        if (player.getCoin() < requiredCoins) {
            throw new IllegalArgumentException("Not enough coins to perform the action.");
        }

        if (player.getCoin() >= 10 && !actionName.equals("Coup")) {
            throw new IllegalArgumentException("Player must perform Coup when having 10 or more coins.");
        }
    }

    private int getRequiredCoinsForAction(String actionName) {
        switch (actionName) {
            case "Coup":
                return 7;
            case "Assassinate":
                return 3;
            default:
                return 0;
        }
    }

    private void performAction(Game game, GameMember player, int actionValue, String targetPlayerName) {
        ActionType actionType = ActionType.fromActionValue(actionValue);
        Action action = actionRepository.findByEnglishName(actionType.name())
                .orElseThrow(() -> new IllegalArgumentException("Invalid action: " + actionType.name()));

        GameMember target = null;
        if (targetPlayerName != null && !targetPlayerName.isEmpty()) {
            target = findPlayerByName(game, targetPlayerName);
        }

        switch (actionType) {
            case INCOME:
                player.setCoin(player.getCoin() + 1);
                break;
            case FOREIGN_AID:
                player.setCoin(player.getCoin() + 2);
                break;
            case COUP:
                player.setCoin(player.getCoin() - 7);
                if (target != null) {
                    loseInfluence(target);
                }
                break;
            case TAX:
                player.setCoin(player.getCoin() + 3);
                break;
            case ASSASSINATE:
                player.setCoin(player.getCoin() - 3);
                if (target != null) {
                    loseInfluence(target);
                }
                break;
            case STEAL:
                if (target != null) {
                    int stolenCoins = Math.min(2, target.getCoin());
                    target.setCoin(target.getCoin() - stolenCoins);
                    player.setCoin(player.getCoin() + stolenCoins);
                }
                break;
            case EXCHANGE:
                // Exchange 로직 구현
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + actionType.name());
        }

        gameMemberRepository.save(player);
        if (target != null) {
            gameMemberRepository.save(target);
        }
        recordHistory(game.getId(), action.getId(), player.getName(), targetPlayerName);
    }

    private void loseInfluence(GameMember target) {
        if (target.getLeftCard() != null && target.getLeftCard() > 0) {
            target.setLeftCard(-target.getLeftCard());
        } else if (target.getRightCard() != null && target.getRightCard() > 0) {
            target.setRightCard(-target.getRightCard());
        }

        if ((target.getLeftCard() == null || target.getLeftCard() < 0) &&
                (target.getRightCard() == null || target.getRightCard() < 0)) {
            target.setPlayer(false);
        }
    }

    private GameMember findPlayerByName(Game game, String playerId) {
        System.out.println("playerId: " + playerId);
        return game.getMemberIds().stream()
                .map(id -> gameMemberRepository.findById(id).orElseThrow())
                .filter(member -> member.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerId));
    }

    @Override
    public GameStateDto getGameState(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));

        List<GameMember> members = game.getMemberIds().stream()
                .map(id -> gameMemberRepository.findById(id).orElseThrow())
                .collect(Collectors.toList());

        Map<Integer, CardInfo> cardInfoMap = cardInfoRepository.findAll().stream()
                .collect(Collectors.toMap(CardInfo::getId, Function.identity()));

        members.forEach(member -> {
            if (member.getLeftCard() != null) {
                CardInfo leftCardInfo = cardInfoMap.get(Math.abs(member.getLeftCard()));
                member.setLeftCardInfo(leftCardInfo);
            }
            if (member.getRightCard() != null) {
                CardInfo rightCardInfo = cardInfoMap.get(Math.abs(member.getRightCard()));
                member.setRightCardInfo(rightCardInfo);
            }
        });

        Map<String, Integer> possibleActions = game.getActionContext().isEmpty()
                ? getDefaultActions()
                : possibleActionRepository.findCanActionNamesAndIdsByActionId(game.getActionContext().getLast().getActionId());

        return getGameStateDto(game, members, possibleActions);
    }

    private Map<String, Integer> getDefaultActions() {
        Map<String, Integer> defaultActions = new HashMap<>();
        defaultActions.put("income", 1);
        defaultActions.put("foreign_aid", 2);
        defaultActions.put("tax", 3);
        defaultActions.put("steal", 4);
        defaultActions.put("assassinate", 5);
        defaultActions.put("exchange", 6);
        defaultActions.put("coup", 7);
        defaultActions.put("challenge", 8);
        defaultActions.put("permit", 9);
        defaultActions.put("block_duke", 10);
        defaultActions.put("block_captain", 11);
        defaultActions.put("block_ambassador", 12);
        defaultActions.put("block_contessa", 13);
        return defaultActions;
    }

    private GameStateDto getGameStateDto(Game game, List<GameMember> members, Map<String, Integer> possibleActions) {
        GameStateDto gameStateDto = new GameStateDto();
        gameStateDto.setMessage(game.getId());
        gameStateDto.setTurn(game.getTurn());
        gameStateDto.setMembers(members);
        gameStateDto.setHistory(game.getHistory());
        gameStateDto.setWhoseTurn(game.getWhoseTurn());
        gameStateDto.setCanAction(possibleActions);
        gameStateDto.setLastContext(game.getActionContext().isEmpty() ? null : game.getActionContext().getLast());
        gameStateDto.setDeck(game.getDeck());
        return gameStateDto;
    }

    @Override
    public void recordHistory(String gameId, Integer actionNumber, String playerTrying, String playerTried) {
        History history = new History(UUID.randomUUID().toString(), actionNumber, playerTrying, playerTried);

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));

        history.setTurn(game.getTurn());
        game.addHistory(history);
        gameRepository.save(game);
    }

    public GameStateDto buildGameState(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));

        GameStateDto gameStateDto = getGameState(gameId);
        gameStateDto.setMessage(gameId);
        gameStateDto.setAwaitingChallenge(game.isAwaitingChallenge());
        gameStateDto.setAwaitingCounterAction(game.isAwaitingCounterAction());
        gameStateDto.setAwaitingChallengeActionValue(game.getAwaitingChallengeActionValue());
        gameStateDto.setAwaitingCounterActionValue(game.getAwaitingCounterActionValue());

        return gameStateDto;
    }


    @Override
    public String nextTurn(MessageDto messageDto) {
        Map<String, String> mainMessage = (Map<String, String>) messageDto.getMainMessage();

        if (mainMessage.get("cookie") != null) {
            Optional<Game> existGame = gameRepository.findById(mainMessage.get("cookie"));
            if (existGame.isPresent()) {
                Game game = existGame.get();
                System.out.println("game: "+game);
                GameMember currentPlayer = findPlayerByName(game, game.getMemberIds().get(game.getWhoseTurn()));
                game.setWhoseTurn((game.getWhoseTurn() + 1) % 4);
                if (isGPTPlayer(currentPlayer)) {
                    performGPTAction(game.getId(), currentPlayer);
                }
                return "gameState";
            }
        }
        return "action";
    }

    @Override
    public String myChoice(MessageDto messageDto) {
        return "";
    }

    public boolean gameCheck(MessageDto messageDto) {
        Map<String, String> mainMessage = (Map<String, String>) messageDto.getMainMessage();

        if (mainMessage.get("cookie") != null) {
            return gameRepository.existsById(mainMessage.get("cookie"));
        }

        return false;
    }

    public String gameInit(MessageDto messageDto) {
        Map<String, String> mainMessage = (Map<String, String>) messageDto.getMainMessage();

        if (mainMessage.get("cookie") != null) {
            Optional<Game> existGame = gameRepository.findById(mainMessage.get("cookie"));
            // 기존 게임이 이미 존재한다면 해당 게임 아이디 리턴
            if (existGame.isPresent()) {
                return existGame.get().getId();
            }
        }

        // 새로운 게임 생성
        String gameId = gameGenerator.init(messageDto.getRoomId());
        // 게임 시작 히스토리 작성
        recordHistory(gameId, 17, "0", "0");
        return gameId;
    }

    public GameStateDto challenge(String gameId, String challengerName, Integer actionValue) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        GameMember challenger = findPlayerByName(game, challengerName);

        // 마지막 액션 가져오기
        History lastAction = game.getActionContext().getLast();
        ActionType lastActionType = ActionType.fromActionValue(lastAction.getActionId());
        GameMember target = findPlayerByName(game, lastAction.getPlayerTrying());

        // 도전 성공 여부 확인
        boolean challengeSuccess = !target.hasCard(lastActionType.getValue());

        if (challengeSuccess) {
            // 도전 성공: 타겟 플레이어가 영향력 상실
            loseInfluence(target);
        } else {
            // 도전 실패: 도전자가 영향력 상실
            loseInfluence(challenger);
            // 타겟 플레이어는 새 카드를 받음
            giveNewCard(target);
        }

        // 게임 상태 업데이트
        game.addHistory(new History(UUID.randomUUID().toString(), ActionType.CHALLENGE.getValue(), challengerName, target.getName()));
        gameRepository.save(game);

        return buildGameState(gameId);
    }

    public GameStateDto counterAction(String gameId, String counterActorName, Integer actionValue) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        GameMember counterActor = findPlayerByName(game, counterActorName);

        // 마지막 액션 가져오기
        History lastAction = game.getActionContext().getLast();
        ActionType originalActionType = ActionType.fromActionValue(lastAction.getActionId());
        ActionType counterActionType = ActionType.fromActionValue(actionValue);

        // 대응 가능한 액션인지 확인
        if (!isCounterActionValid(originalActionType, counterActionType)) {
            throw new IllegalArgumentException("Invalid counter action");
        }

        // 대응 액션 추가
        game.addHistory(new History(UUID.randomUUID().toString(), actionValue, counterActorName, lastAction.getPlayerTrying()));
        gameRepository.save(game);

        return buildGameState(gameId);
    }

    private boolean isCounterActionValid(ActionType originalAction, ActionType counterAction) {
        Map<ActionType, List<ActionType>> validCounterActions = Map.of(
                ActionType.FOREIGN_AID, List.of(ActionType.BLOCK_DUKE),
                ActionType.ASSASSINATE, List.of(ActionType.BLOCK_CONTESSA),
                ActionType.STEAL, List.of(ActionType.BLOCK_CAPTAIN, ActionType.BLOCK_DUKE)
        );

        return validCounterActions.getOrDefault(originalAction, List.of()).contains(counterAction);
    }

    private void giveNewCard(GameMember player) {

    }
}