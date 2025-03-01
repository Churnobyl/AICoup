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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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
        return player.getName().contains("GPT");
    }

    public String performPlayerAction(MessageDto message) {
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        int actionValue = Integer.parseInt(mainMessage.get("action"));
        String targetId = mainMessage.get("targetPlayerId");
        Game game = returnGame(message);
        String playerId = findPlayerByName(game, game.getMemberIds().get(game.getWhoseTurn())).getId();
        String actionName = ActionType.findActionName(actionValue);
        if (validateAction(game, playerId, targetId, actionName)) {
            recordHistory(game, actionValue, null, playerId, targetId, null);
            if(actionValue==5) {
                GameMember player = findPlayerByName(game, playerId);
                player.setCoin(player.getCoin()-3);
                gameMemberRepository.save(player);
            }
            return "actionPending";
        } else {
            return "action";
        }
    }

    public void performGPTAction(Game game, GameMember currentPlayer) {
        String[] actionResult;
        String action, target, targetId = "none";

        if(currentPlayer.getCoin()>=10) {
            action = "coup";
            GameMember gameMember;
            do {
                Random random = new Random();
                int targetNum = random.nextInt(4);
                target = "" + targetNum;
                targetId = game.getMemberIds().get(targetNum);
                gameMember = findPlayerByName(game, targetId);
            } while(gameMember.getLeftCard()<0 && gameMember.getRightCard()<0 && currentPlayer.getId()!=gameMember.getId());
        } else {
            do {
                actionResult = gptResponseGetter.actionApi(game.getId());
                action = actionResult[0];
                target = actionResult[1];
                if(!target.equals("none")) {
                    targetId = game.getMemberIds().get(Integer.parseInt(target)-1);
                } else {
                    targetId = "none";
                }
            } while(!validateAction(game, currentPlayer.getId(), targetId, action));
        }
        // 대사 api 호출
        String actionDialog = gptResponseGetter.actionDialogApi(action, target, currentPlayer.getName(), currentPlayer.getPersonality());

        int actionValue = ActionType.findActionValue(action);
        if(actionValue==5) {
            currentPlayer.setCoin(currentPlayer.getCoin()-3);
        }
        recordHistory(game, actionValue, null, currentPlayer.getId(), targetId, actionDialog);
        gameMemberRepository.save(currentPlayer);
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
            returnMessage.put("result", "noGame");
            returnMessage.put("message", "게임이 없습니다.");
            return returnMessage;
        }

        // AIoT 검증 로직
//        Game game = gameRepository.findById(mainMessage.get("cookie")).get();
//        List<GameMember> members = game.getMemberIds().stream()
//                .map(gameMemberRepository::findById)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .toList();
//
//        List<MMResponse> dataFromAIoTServer = aIoTSocket.getDataFromAIoTServer();
//        for (int i = 0; i < members.size(); i++) {
//            if (!validateMemberCards(members.get(i), dataFromAIoTServer.get(i))) {
//                returnMessage.put("result", "fail");
//                returnMessage.put("message", members.get(i).getName() + "님의 카드 상태가 서버와 다릅니다.");
//                return returnMessage;
//            }
//        }

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

    private boolean offerPlayerChallenge(Game game, int actionValue) {
        game.setAwaitingChallenge(true);
        game.setAwaitingChallengeActionValue(actionValue);
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
            recordHistory(game, 1, null, "0", "0", null);
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, challengeNotification);
        }
    }

    // gpt의 액션을 플레이어가 허용했을 때 다른 gpt가 도전하는 경우 처리 로직 필요
    public String handleGPTChallenge(MessageDto message) {
        Game game = returnGame(message);
        String returnState;
        if(!shouldPerformChallenge(game.getHistory().get(game.getHistory().size()-1).getActionId())) { // 도전 가능한 액션이 아니라면
            returnState = "gptChallengeNone"; // gptChallengeNone 메세지 전송
            return returnState;
        }
        String[] challengeResult = gptResponseGetter.challengeApi(game.getId()); // gpt의 도전 응답 추출
        String challengerNumber = challengeResult[0];

        // 해당 턴의 액션 히스토리 추출
        int index = game.getHistory().size()-1;
        History history = game.getHistory().get(index);
        while(history.getActionId()>7) {
            index--;
            history = game.getHistory().get(index);
        }
        int actionValue = history.getActionId(); // 의심한 액션 추출
        actionValue = switch (actionValue) {
            case 3 -> 1;
            case 4 -> 2;
            case 5 -> 3;
            case 6 -> 5;
            default -> actionValue;
        };
        String actionerId = history.getPlayerTrying(); // 해당 행동 수행한 사람의 아이디 추출
        GameMember actioner = findPlayerByName(game, actionerId); // 해당 행동 수행한 플레이어 추출
        GameMember challenger = new GameMember();
        while(true) {
            challengeResult = gptResponseGetter.challengeApi(game.getId());
            challengerNumber = challengeResult[0];
            if(challengerNumber.equals("2")||challengerNumber.equals("3")||challengerNumber.equals("4")||challengerNumber.equals("none")) {
                if(challengerNumber.equals("none")) {
                    break;
                } else {
                    String challengerId = game.getMemberIds().get(Integer.parseInt(challengerNumber)-1);
                    challenger = findPlayerByName(game, challengerId);
                    System.out.println("actionerId : " + actionerId);
                    System.out.println("challengerId : " + challengerId);
                    if((challenger.getLeftCard()>0||challenger.getRightCard()>0)&&!actionerId.equals(challengerId)) {
                        break;
                    }
                }
            }
        }
        if (!"none".equals(challengerNumber)) {
            returnState = "gptChallenge";
            String challengerId = game.getMemberIds().get(Integer.parseInt(challengerNumber)-1);
            // 도전 대사 추출
            String challengeDialog = gptResponseGetter.challengeDialogApi(challenger.getName(), actioner.getName(), ActionType.findActionName(actionValue), challenger.getPersonality());
            // 게임 상태 업데이트(우선 챌린지 한 내역을 히스토리에 기록)
            recordHistory(game, 8, null, challengerId, actionerId, challengeDialog);
            if(!isGPTPlayer(actioner)) { // 해당 턴에 액션을 행한 주체가 플레이어라면
                return returnState;
            } else { // 해당 턴에 액션을 행한 주체가 gpt 라면
                GameStateDto gameState;
                gameState = buildGameState(game.getId());
                if(actioner.getLeftCard()==actionValue) { // 만약 왼쪽 카드가 해당 행동과 일치하면
                    gameState.setCardOpen(0); // 왼쪽 카드 오픈
                } else if(actioner.getRightCard()==actionValue) { // 만약 오른쪽 카드가 해당 행동과 일치하면
                    gameState.setCardOpen(1); // 오른쪽 카드 오픈
                } else { // 만약 해당 카드가 없다면
                    Random random = new Random();
                    gameState.setCardOpen(random.nextInt(2)); // 왼쪽, 오른쪽 랜덤 오픈
                }
                game.setCardOpen(gameState.getCardOpen());
                gameRepository.save(game);
                return returnState;
            }
        } else {
            return "gptChallengeNone";
        }
    }

    public String handleGPTPerformChallenge(MessageDto message) {
        Game game = returnGame(message);
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        int cardOpen = Integer.parseInt(mainMessage.get("cardOpen"));
        String challengerId = game.getHistory().get(game.getHistory().size()-1).getPlayerTrying();
        return challenge(game, challengerId, cardOpen);
    }

    public String handleGPTCounterActionChallenge(MessageDto message) {
        Game game = returnGame(message);
        String returnState;
        String[] challengeResult;
        String challenger;
        do{
            challengeResult = gptResponseGetter.challengeApi(game.getId());
            challenger = challengeResult[0];
        }while(!challenger.equals("2")&&!challenger.equals("3")&&!challenger.equals("4")&&!challenger.equals("none"));
        if (!"none".equals(challenger)) {
            returnState = "gptCounterActionChallenge";
        } else {
            returnState = "gptChallengeNone";
        }
        return returnState;
    }

    public String handleGPTPerformCounterActionChallenge(MessageDto message) {
        Game game = returnGame(message);
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        int cardOpen = Integer.parseInt(mainMessage.get("cardOpen"));
        String challengerId = game.getHistory().get(game.getHistory().size()-1).getPlayerTrying();
        return challenge(game, challengerId, cardOpen);
    }

    private Game returnGame(MessageDto message) {
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        String gameId = mainMessage.get("cookie");
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    }

    public String handleGPTCounterAction(MessageDto message) {
        Game game = returnGame(message);
        String[] counterActionResult; // 대응자와 대응을 저장할 배열
        History lastAction = game.getHistory().get(game.getHistory().size() - 1);
        ActionType originalActionType = ActionType.fromActionValue(lastAction.getActionId()); // 해당 턴의 액션
        ActionType counterActionType; // 액션에 대한 대응
        do {
            counterActionResult = gptResponseGetter.counterActionApi(game.getId());
            if ("none".equals(counterActionResult[1])) { // 대응이 없으면 gptCounterActionNone 리턴
                return "gptCounterActionNone";
            } else if(counterActionResult[1].equals("duke")||counterActionResult[1].equals("captain")||counterActionResult[1].equals("ambassador")||counterActionResult[1].equals("contessa")) {
                counterActionType = ActionType.fromActionValue(convertCounterActionToValue(counterActionResult[1]));
            } else {
                return "gptCounterActionNone";
            }
        } while (!isCounterActionValid(originalActionType, counterActionType)); // 대응이 있으면 유효성 검증 후 유효하지 않다면 gpt api 재호출
        String counterActionerNum = counterActionResult[0];
        String counterAction = counterActionResult[1];
        int counterActionValue = convertCounterActionToValue(counterAction); // 대응에 해당하는 넘버 추출
        String counterActionerId = game.getMemberIds().get(Integer.parseInt(counterActionerNum)-1); // 대응자의 id 추출
        // GPT 대사 호출
        GameMember counterActioner = findPlayerByName(game, counterActionerId);
        GameMember actioner = findPlayerByName(game, lastAction.getPlayerTrying());
        String counterActionDialog = gptResponseGetter.counterActionDialogApi(counterActioner.getName(), counterAction, actioner.getName(), originalActionType.getName(), counterActioner.getPersonality());
        recordHistory(game, counterActionValue, null, counterActionerId, lastAction.getPlayerTrying(), counterActionDialog); // 히스토리에 기록
        gameRepository.save(game); // DB에 저장
        return "gptCounterAction"; // gptCounterAction 리턴
    }

//    private void handleGPTChallengeAgainstCounterAction(Game game, int counterActionValue) {
//        String[] counterActionChallengeResult = gptResponseGetter.counterActionChallengeApi(game.getId());
//        String counterActionChallenger = counterActionChallengeResult[0];
//        if (!"none".equals(counterActionChallenger)) {
//            challenge(game, counterActionChallenger);
//        }
//    }

    public String handlePlayerChallenge(MessageDto message) {
        Game game = returnGame(message);
        int index = game.getHistory().size()-1;
        History history = game.getHistory().get(index);
        while(history.getActionId()>=10 && history.getActionId()<=13) {
            index--;
            history = game.getHistory().get(index);
        }
        String counterActionerId = history.getPlayerTrying(); // 해당 대응 수행한 사람의 아이디 추출
        recordHistory(game, 8, null, "1", counterActionerId, null);
        return "cardOpen";
    }

    public String handlePlayerPerformChallenge(MessageDto message) {
        Game game = returnGame(message);
        // 카드 deadCardOpen
        // 카드 공개 로직 추가
        int index = game.getHistory().size()-1;
        History history = game.getHistory().get(index);
        while(history.getActionId()>7) {
            index--;
            history = game.getHistory().get(index);
        }
        // 해당 턴의 액션 히스토리 추출
        int actionValue = history.getActionId(); // 의심한 액션 추출
        actionValue = switch (actionValue) {
            case 3 -> 1;
            case 4 -> 2;
            case 5 -> 3;
            case 6 -> 5;
            default -> actionValue;
        };
        String actionerId = history.getPlayerTrying(); // 해당 행동 수행한 사람의 아이디 추출
        GameMember actioner = findPlayerByName(game, actionerId); // 해당 행동 수행한 플레이어 추출

        GameStateDto gameState = new GameStateDto();
        gameState = buildGameState(game.getId());
        System.out.println(actioner);
        System.out.println(actionValue);

        if(isGPTPlayer(actioner)) {
            if(actioner.getLeftCard()==actionValue) { // 만약 왼쪽 카드가 해당 행동과 일치하면
                gameState.setCardOpen(0); // 왼쪽 카드 오픈
                System.out.println("왼쪽 카드를 오픈합니다");
            } else if(actioner.getRightCard()==actionValue) { // 만약 오른쪽 카드가 해당 행동과 일치하면
                gameState.setCardOpen(1); // 오른쪽 카드 오픈
                System.out.println("오른쪽 카드를 오픈합니다");
            } else { // 만약 해당 카드가 없다면
                if(actioner.getLeftCard()>0 && actioner.getRightCard()>0) {
                    Random random = new Random();
                    gameState.setCardOpen(random.nextInt(2)); // 왼쪽, 오른쪽 랜덤 오픈
                } else if(actioner.getLeftCard()>0) {
                    gameState.setCardOpen(0); // 왼쪽 오픈
                    System.out.println("랜덤 왼쪽 카드를 오픈합니다");
                } else {
                    gameState.setCardOpen(1); // 오른쪽 오픈
                    System.out.println("랜덤 오른쪽 카드를 오픈합니다");
                }
            }
        }
        game.setCardOpen(gameState.getCardOpen());
        gameRepository.save(game);

        int cardOpen = game.getCardOpen();
        history = game.getHistory().get(game.getHistory().size()-1);
        return challenge(game, history.getPlayerTrying(), cardOpen);
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
            gameState = counterAction(game, counterActorName, actionValue);

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
            case "income":
                return 1;
            case "foreign_aid":
                return 2;
            case "tax":
                return 3;
            case "steal":
                return 4;
            case "assassinate":
                return 5;
            case "exchange":
                return 6;
            case "coup":
                return 7;
            case "duke":
                return 10;
            case "captain":
                return 11;
            case "ambassador":
                return 12;
            case "contessa":
                return 13;
            default:
                throw new IllegalArgumentException("Unknown action: " + action);
        }
    }

    private int convertCounterActionToValue(String counterAction) {
        switch (counterAction.toLowerCase()) {
            case "duke":
                return 10;
            case "captain":
                return 11;
            case "ambassador":
                return 12;
            case "contessa":
                return 13;
            default:
                throw new IllegalArgumentException("Unknown counterAction: " + counterAction);
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

    public boolean validateAction(Game game, String playerId, String targetId, String actionName) {
        GameMember player = findPlayerByName(game, playerId);

        // 존재하는 액션인지 검증
        Action action = actionRepository.findByEnglishName(actionName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid action: " + actionName));

        // 코인이 충분한지 검증
        int requiredCoins = getRequiredCoinsForAction(actionName);
        if (player.getCoin() < requiredCoins) {
            return false;
        }

        // 코인이 10개 이상일 때 coup을 하는지 검증
        if(player.getCoin() >= 10 && !actionName.equals("coup")) {
            return false;
        }

        // target이 필요한 액션일 때 target이 자기자신이 아닌지 검증
        if(!targetId.equals("none") && playerId.equals(targetId)) {
            System.out.println(targetId);
            return false;
        }

        return true;
    }

    private int getRequiredCoinsForAction(String actionName) {
        return switch (actionName) {
            case "coup" -> 7;
            case "assassinate" -> 3;
            default -> 0;
        };
    }

    public String deadCardOpen(MessageDto message) {
        Game game = returnGame(message);
        // 이 턴의 액션 추출
        int index = game.getHistory().size()-1;
        History history = game.getHistory().get(index);
        while(history.getActionId()>7) {
            index--;
            history = game.getHistory().get(index);
        }
        int actionValue = history.getActionId();
        if(actionValue==5||actionValue==7) { // 해당 턴의 액션이 coup 또는 assassinate 이고
            String targetPlayerId = history.getPlayerTried();
            GameMember target = findPlayerByName(game, targetPlayerId);
            if(isGPTPlayer(target)) { // target이 gpt 라면
                return  performAction(message); // 알아서 카드 공개하고 마무리
            }
            return "deadCardOpen"; // target이 플레이어라면 카드 선택 필요
        } else { // 해당 턴의 액션이 coup 또는 assassinate 이 아니면 액션 처리 후 마무리
            return performAction(message);
        }
    }

    public String performAction(MessageDto message) {
        Game game = returnGame(message);
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        int index = game.getHistory().size()-1;
        History history = game.getHistory().get(index);
        while(history.getActionId()>7) {
            index--;
            history = game.getHistory().get(index);
        }
        int actionValue = history.getActionId();
        GameMember player = findPlayerByName(game, history.getPlayerTrying());
        String targetPlayerId = history.getPlayerTried();
        System.out.println("targetPlayerName: " + targetPlayerId);
        ActionType actionType = ActionType.fromActionValue(actionValue);
        Action action = actionRepository.findByEnglishName(actionType.name())
                .orElseThrow(() -> new IllegalArgumentException("Invalid action: " + actionType.name()));

        GameMember target = null;
        if (targetPlayerId != null && !targetPlayerId.isEmpty() && !targetPlayerId.equals("none")) {
            target = findPlayerByName(game, targetPlayerId);
        }

        int cardOpen = 0;

        switch (actionType) {
            case INCOME:
                player.setCoin(player.getCoin() + 1);
                break;
            case FOREIGN_AID:
                player.setCoin(player.getCoin() + 2);
                break;
            case COUP:
                if(!isGPTPlayer(target)) {
                    cardOpen = Integer.parseInt(mainMessage.get("cardOpen"));
                } else {
                    if(target.getLeftCard()>0 && target.getRightCard()>0) {
                        Random random = new Random();
                        cardOpen = random.nextInt(2);
                    } else if(target.getLeftCard()>0) {
                        cardOpen = 0;
                    } else {
                        cardOpen = 1;
                    }
                }
                player.setCoin(player.getCoin() - 7);
                if (target != null) {
                    loseInfluence(target, cardOpen);
                }
                break;
            case TAX:
                player.setCoin(player.getCoin() + 3);
                break;
            case ASSASSINATE:
                if(!isGPTPlayer(target)) {
                    cardOpen = Integer.parseInt(mainMessage.get("cardOpen"));
                } else {
                    if(target.getLeftCard()>0 && target.getRightCard()>0) {
                        Random random = new Random();
                        cardOpen = random.nextInt(2);
                    } else if(target.getLeftCard()>0) {
                        cardOpen = 0;
                    } else {
                        cardOpen = 1;
                    }
                }
                if (target != null) {
                    loseInfluence(target, cardOpen);
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
        recordHistory(game, action.getId(), true, player.getId(), targetPlayerId, null);
        gameMemberRepository.save(player);
        if (targetPlayerId != null && !targetPlayerId.isEmpty() && !targetPlayerId.equals("none")) {
            gameMemberRepository.save(target);
        }
        gameRepository.save(game);

        return isGameOver(message);
    }

    public String isGameOver(MessageDto message) {
        Game game = returnGame(message);
        game.setWhoseTurn((game.getWhoseTurn() + 1) % 4);
        // 죽은 플레이어 턴 스킵 로직 추가
        GameMember nextPlayer = findPlayerByName(game, game.getMemberIds().get(game.getWhoseTurn())); // 다음 플레이어가
        while(nextPlayer.getLeftCard()<0 && nextPlayer.getRightCard()<0) { // 죽었으면
            game.setWhoseTurn((game.getWhoseTurn() + 1) % 4); // 다음 플레이어로 턴 증가
            nextPlayer = findPlayerByName(game, game.getMemberIds().get(game.getWhoseTurn()));
        }
        game.setTurn(game.getTurn()+1);
        gameRepository.save(game);

        String winPlayerId = "";
        int deadPlayerCnt = 0;
        for(int i=0; i<4; i++) {
            String memberId = game.getMemberIds().get(i);
            GameMember gameMember = findPlayerByName(game, memberId);
            if(gameMember.getLeftCard()<0 && gameMember.getRightCard()<0) {
                deadPlayerCnt++;
                if(i==0) return "playerDown"; // 플레이어가 죽으면 게임 종료
            } else {
                winPlayerId = gameMember.getId();
            }
        }
        if(deadPlayerCnt==3) { // 최후의 1인이 남으면 게임 종료
            recordHistory(game, 15, null, winPlayerId, null, null);
            return "gameOver";
        }
        return "gameState"; // 게임이 종료되지 않았다면 다음 턴으로 진행 위해 gameState 리턴
    }

    private void loseInfluence(GameMember target, int cardOpen) {
        if (cardOpen == 0 && target.getLeftCard() > 0) {
            target.setLeftCard(-target.getLeftCard());
        } else if (cardOpen == 1 && target.getRightCard() > 0) {
            target.setRightCard(-target.getRightCard());
        }

        if ((target.getLeftCard() == null || target.getLeftCard() < 0) &&
                (target.getRightCard() == null || target.getRightCard() < 0)) {
            target.setPlayer(false);
        }
        gameMemberRepository.save(target);
    }

    private GameMember findPlayerByName(Game game, String playerId) {
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

        Map<String, Integer> possibleActions = getPossibleActions(game);

        return getGameStateDto(game, members, possibleActions);
    }

    private Map<String, Integer> getPossibleActions(Game game) {
        if (game.isAwaitingChallenge()) {
            return getChallengeActions();
        } else if (game.isAwaitingCounterAction()) {
            return getCounterActions(game.getCurrentAction());
        } else {
            return getPlayerTurnActions(game);
        }
    }

    private Map<String, Integer> getPlayerTurnActions(Game game) {
        Map<String, Integer> actions = new HashMap<>();
        actions.put("income", 1);
        actions.put("foreign_aid", 2);
        actions.put("tax", 3);
        actions.put("steal", 4);
        actions.put("assassinate", 5);
        actions.put("exchange", 6);
        actions.put("coup", 7);
        return actions;
    }

    private Map<String, Integer> getChallengeActions() {
        Map<String, Integer> actions = new HashMap<>();
        actions.put("challenge", 8);
        actions.put("permit", 9);
        return actions;
    }

    private Map<String, Integer> getCounterActions(int currentAction) {
        Map<String, Integer> actions = new HashMap<>();
        switch (currentAction) {
            case 2: // foreign_aid
                actions.put("block_duke", 10);
                break;
            case 4: // steal
                actions.put("block_captain", 11);
                actions.put("block_ambassador", 12);
                break;
            case 5: // assassinate
                actions.put("block_contessa", 13);
                break;
        }
        return actions;
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

    public void recordHistory(Game game, Integer actionNumber, Boolean actionState, String playerTrying, String playerTried, String dialog) {
        History history = new History(UUID.randomUUID().toString(), actionNumber, playerTrying, playerTried);
        history.setActionState(actionState);
        history.setTurn(game.getTurn());
        history.setDialog(dialog);
        game.addHistory(history);
        gameRepository.save(game);
    }

    public GameStateDto buildGameState(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found with ID: " + gameId));

        GameStateDto gameStateDto = getGameState(gameId);
        gameStateDto.setMessage(gameId);
        gameStateDto.setCardOpen(game.getCardOpen());
        gameStateDto.setAwaitingChallenge(game.isAwaitingChallenge());
        gameStateDto.setAwaitingCounterAction(game.isAwaitingCounterAction());
        gameStateDto.setAwaitingChallengeActionValue(game.getAwaitingChallengeActionValue());
        gameStateDto.setAwaitingCounterActionValue(game.getAwaitingCounterActionValue());

        return gameStateDto;
    }

    @Override
    public String nextTurn(MessageDto message) {
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        if (mainMessage.get("cookie") != null) {
            Optional<Game> existGame = gameRepository.findById(mainMessage.get("cookie"));
            if (existGame.isPresent()) {
                Game game = existGame.get();
                GameMember currentPlayer = findPlayerByName(game, game.getMemberIds().get(game.getWhoseTurn()));
                System.out.println("GameMember: "+currentPlayer);
                if (isGPTPlayer(currentPlayer)) {
                    performGPTAction(game, currentPlayer);
                    return "gptAction";
                } else {
                    recordHistory(game, 18, null, "none", "none", null);
                    return "action";
                }
            }
        }
        return "gameState";
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
        Game game = gameGenerator.init(messageDto.getRoomId());
        // 게임 시작 히스토리 작성
        recordHistory(game, 17, null, "none", "none", null);
        return game.getId();
    }

    public String challenge(Game game, String challengerId, int cardOpen) {

        GameMember challenger = findPlayerByName(game, challengerId);

        // 마지막 액션 가져오기
        int index = game.getHistory().size()-1;
        History lastAction = game.getHistory().get(index);
        while(lastAction.getActionId()>7) {
            index--;
            lastAction = game.getHistory().get(index);
        }
        History history = game.getHistory().get(game.getHistory().size()-1);
        ActionType lastActionType = ActionType.fromActionValue(lastAction.getActionId());
        GameMember target = findPlayerByName(game, history.getPlayerTried());

        // 도전 성공 여부 확인
        boolean challengeSuccess = !target.hasCard(lastActionType.getValue(), cardOpen);

        if (challengeSuccess) {
            // 도전 성공 내역 히스토리에 기록
            recordHistory(game, 14, true, challengerId, target.getId(), null);

            // gpt가 target인 경우 cardOpen 세팅
            if(isGPTPlayer(target)) {
                if(target.getLeftCard()>0 && target.getRightCard()>0) {
                    Random random = new Random();
                    cardOpen = random.nextInt(2);
                } else if(target.getLeftCard()>0) {
                    cardOpen = 0;
                } else {
                    cardOpen = 1;
                }
            }

            // 도전 성공: 타겟 플레이어가 영향력 상실
            loseInfluence(target, cardOpen);

            gameRepository.save(game);
        } else {
            // 도전 실패 내역 히스토리에 기록
            recordHistory(game, 14, false, challengerId, target.getId(), null);

            // gpt가 target인 경우 cardOpen 세팅
            if(isGPTPlayer(challenger)) {
                if(challenger.getLeftCard()>0 && challenger.getRightCard()>0) {
                    Random random = new Random();
                    cardOpen = random.nextInt(2);
                } else if(challenger.getLeftCard()>0) {
                    cardOpen = 0;
                } else {
                    cardOpen = 1;
                }
                // 도전 실패: 도전자가 영향력 상실
                loseInfluence(challenger, cardOpen);

                int targetIndex = 0;
                for(int i=0; i<4; i++) {
                    if(target.getId()==game.getMemberIds().get(i)) {
                        targetIndex = i;
                        break;
                    }
                }
                // 타겟 플레이어는 새 카드를 받음
                //giveNewCard(target, cardOpen, targetIndex);
            } else {
                return "playerCardOpen";
            }
        }
        return challengeSuccess?"challengeSuccess":"challengeFail";
    }

    public String playerChallengeCardOpen(MessageDto message) {
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        int cardOpen = Integer.parseInt(mainMessage.get("cardOpen"));
        Game game = returnGame(message);
        GameMember player = findPlayerByName(game, "1");
        loseInfluence(player, cardOpen);
        GameMember target = new GameMember();
        int targetIndex = 0;
        for(int i=0; i<4; i++) {
            if(target.getId()==game.getMemberIds().get(i)) {
                targetIndex = i;
                break;
            }
        }
        // 타겟 플레이어는 새 카드를 받음
        //giveNewCard(target, cardOpen, targetIndex);
        return "endGame";
    }

    public GameStateDto counterAction(Game game, String counterActioner, Integer counterAction) {
        String counterActionerId = game.getMemberIds().get(Integer.parseInt(counterActioner)-1); // 대응자의 id 추출

        // 마지막 액션 가져오기
        int index = game.getHistory().size()-1;
        History lastAction = game.getHistory().get(index);
        while(lastAction.getActionId()>7) {
            index--;
            lastAction = game.getHistory().get(index);
        }

        ActionType originalActionType = ActionType.fromActionValue(lastAction.getActionId());
        ActionType counterActionType = ActionType.fromActionValue(counterAction);

        // 대응 액션 추가
        game.addHistory(new History(UUID.randomUUID().toString(), counterAction, counterActionerId, lastAction.getPlayerTrying()));
        gameRepository.save(game);

        return buildGameState(game.getId());
    }

    private boolean isCounterActionValid(ActionType originalAction, ActionType counterAction) {
        Map<ActionType, List<ActionType>> validCounterActions = Map.of(
                ActionType.FOREIGN_AID, List.of(ActionType.BLOCK_DUKE),
                ActionType.ASSASSINATE, List.of(ActionType.BLOCK_CONTESSA),
                ActionType.STEAL, List.of(ActionType.BLOCK_CAPTAIN, ActionType.BLOCK_AMBASSADOR)
        );
        return validCounterActions.getOrDefault(originalAction, List.of()).contains(counterAction);
    }

    private void giveNewCard(GameMember player, int cardOpen, int playerIndex) {
        String body = "{ \"name\": \"ch_win\", \"player_id\": " + playerIndex + " }";
        List<MMResponse> dataFromAIoTServer = aIoTSocket.getDataFromAIoTServer(body);
        if(cardOpen==0) {
            player.setLeftCard(dataFromAIoTServer.get(playerIndex).getLeft_card());
        } else {
            player.setRightCard(dataFromAIoTServer.get(playerIndex).getLeft_card());
        }
    }
}