package com.aicoup.app.websocket.service;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.action.Action;
import com.aicoup.app.domain.entity.game.card.CardInfo;
import com.aicoup.app.domain.entity.game.history.History;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.redisRepository.GameMemberRepository;
import com.aicoup.app.domain.redisRepository.GameRepository;
import com.aicoup.app.domain.redisRepository.HistoryRepository;
import com.aicoup.app.domain.repository.ActionRepository;
import com.aicoup.app.domain.repository.CardInfoRepository;
import com.aicoup.app.domain.repository.PossibleActionRepository;
import com.aicoup.app.pipeline.aiot.AIoTSocket;
import com.aicoup.app.pipeline.aiot.dto.MMResponse;
import com.aicoup.app.websocket.model.dto.GameStateDto;
import com.aicoup.app.websocket.model.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.aicoup.app.domain.game.GameProcessor;
import com.aicoup.app.domain.game.GameGenerator;

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
    private final HistoryRepository historyRepository;
    private final PossibleActionRepository possibleActionRepository;
    private final AIoTSocket aIoTSocket;
    private final GameProcessor gameProcessor;

    @Override
    public Map<String, String> validate(MessageDto message) {
        Map<String, String> returnMessage = new HashMap<>();
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();

        if (mainMessage.get("cookie") == null || !gameRepository.existsById(mainMessage.get("cookie"))) {
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
                .collect(Collectors.toList());

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

    @Override
    public GameStateDto processAction(MessageDto message) {
        Map<String, String> mainMessage = (Map<String, String>) message.getMainMessage();
        String gameId = mainMessage.get("cookie");
        String playerName = message.getWriter();
        String actionName = mainMessage.get("action");

        validateAction(gameId, playerName, actionName);

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        GameMember currentPlayer = gameMemberRepository.findById(game.getMemberIds().get(game.getWhoseTurn()))
                .orElseThrow(() -> new IllegalArgumentException("Current player not found"));

        String targetPlayerName = mainMessage.get("targetPlayerName");

        performAction(game, currentPlayer, actionName, targetPlayerName);

        // 턴 종료 및 다음 플레이어로 이동
        game.setWhoseTurn((game.getWhoseTurn() + 1) % game.getMemberIds().size());
        game.setTurn(game.getTurn() + 1);

        gameRepository.save(game);

        return buildGameState(gameId);
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

    private void performAction(Game game, GameMember player, String actionName, String targetPlayerName) {
        Action action = actionRepository.findByEnglishName(actionName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid action: " + actionName));

        GameMember target = null;
        if (targetPlayerName != null && !targetPlayerName.isEmpty()) {
            target = findPlayerByName(game, targetPlayerName);
        }

        switch (actionName) {
            case "Income":
                player.setCoin(player.getCoin() + 1);
                break;
            case "ForeignAid":
                player.setCoin(player.getCoin() + 2);
                break;
            case "Coup":
                player.setCoin(player.getCoin() - 7);
                if (target != null) {
                    loseInfluence(target);
                }
                break;
            case "Tax":
                player.setCoin(player.getCoin() + 3);
                break;
            case "Assassinate":
                player.setCoin(player.getCoin() - 3);
                if (target != null) {
                    loseInfluence(target);
                }
                break;
            case "Steal":
                if (target != null) {
                    int stolenCoins = Math.min(2, target.getCoin());
                    target.setCoin(target.getCoin() - stolenCoins);
                    player.setCoin(player.getCoin() + stolenCoins);
                }
                break;
            case "Exchange":
                // Exchange 로직 구현
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + actionName);
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
    private GameMember findPlayerByName(Game game, String playerName) {
        return game.getMemberIds().stream()
                .map(id -> gameMemberRepository.findById(id).orElseThrow())
                .filter(member -> member.getName().equals(playerName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found: " + playerName));
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
        defaultActions.put("Income", 1);
        defaultActions.put("ForeignAid", 2);
        defaultActions.put("Tax", 3);
        defaultActions.put("Steal", 4);
        defaultActions.put("Assassinate", 5);
        defaultActions.put("Exchange", 6);
        defaultActions.put("Coup", 7);
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
        GameStateDto gameStateDto = getGameState(gameId);
        gameStateDto.setMessage(gameId);
        return gameStateDto;
    }

    @Override
    public String nextTurn(MessageDto messageDto) {
        Map<String, String> mainMessage = (Map<String, String>) messageDto.getMainMessage();

        if (mainMessage.get("cookie") != null) {
            Optional<Game> existGame = gameRepository.findById(mainMessage.get("cookie"));
            if (existGame.isPresent()) {
                Game game = existGame.get();
                return gameProcessor.run(game);
            }
        }

        return "gameState";
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

}