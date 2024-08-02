package com.aicoup.app.websocket.service;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.action.Action;
import com.aicoup.app.domain.entity.game.action.PossibleAction;
import com.aicoup.app.domain.entity.game.card.CardInfo;
import com.aicoup.app.domain.entity.game.history.History;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.game.GameGenerator;
import com.aicoup.app.domain.game.GameProcessor;
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
    private final PossibleActionRepository possibleActionRepository;
    private final ActionRepository actionRepository;
    private final HistoryRepository historyRepository;
    private final AIoTSocket aIoTSocket;
    private final GameProcessor gameProcessor;

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

    @Override
    public Map<String, String> validate(MessageDto messageDto) {
        Map<String, String> returnMessage = new HashMap<>();
        Map<String, String> mainMessage = (Map<String, String>) messageDto.getMainMessage();

        if (mainMessage.get("cookie") != null) {
            Optional<Game> existGame = gameRepository.findById(mainMessage.get("cookie"));
            if (existGame.isPresent()) {
                Game game = existGame.get();
                List<MMResponse> dataFromAIoTServer = aIoTSocket.getDataFromAIoTServer();
                List<GameMember> members = game.getMemberIds().stream()
                        .map(gameMemberRepository::findById)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .toList();

                for (int i = 0; i < members.size(); i++) {
                    // 왼쪽 카드 비교
                    if (
                        // 왼쪽 카드 숫자 > 0 (오픈되지 않은 상태) && 왼쪽 카드 숫자 != 현실 왼쪽 카드 숫자
                            (members.get(i).getLeftCard() > 0 && Objects.equals(members.get(i).getLeftCard(), dataFromAIoTServer.get(i).getLeft_card())) ||
                                    // 왼쪽 카드 오픈된 상태 && 현실 왼쪽 카드 뒷면 안 보일 경우
                                    (members.get(i).getLeftCard() < 0 && dataFromAIoTServer.get(i).getLeft_card() != 0)
                    ) {
                        returnMessage.put("result", "fail");
                        returnMessage.put("message", members.get(i).getName() + "님의 왼쪽 카드 상태가 서버와 다릅니다.");
                        break;
                    }

                    // 오른쪽 카드 비교
                    if (
                        // 오른쪽 카드 숫자 > 0 (오픈되지 않은 상태) && 오른쪽 카드 숫자 != 현실 오른쪽 카드 숫자
                            (members.get(i).getRightCard() > 0 && Objects.equals(members.get(i).getRightCard(), dataFromAIoTServer.get(i).getRight_card())) ||
                                    // 오른쪽 카드 오픈된 상태 && 현실 오른쪽 카드 뒷면 안 보일 경우
                                    (members.get(i).getRightCard() < 0 && dataFromAIoTServer.get(i).getRight_card() != 0)
                    ) {
                        returnMessage.put("result", "fail");
                        returnMessage.put("message", members.get(i).getName() + "님의 오른쪽 카드 상태가 서버와 다릅니다.");
                        break;
                    }
                }
            }
        }

        returnMessage.put("result", "ok");
        returnMessage.put("message", "");

        return returnMessage;
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

    @Override
    public void recordHistory(String gameId, Integer actionNumber, String playerTrying, String playerTried) {
        History history = new History(UUID.randomUUID().toString(), actionNumber, playerTrying, playerTried);

        Optional<Game> gameOptional = gameRepository.findById(gameId);
        if (gameOptional.isPresent()) {
            Game game = gameOptional.get();
            history.setTurn(game.getTurn());
            game.addHistory(history);
            gameRepository.save(game);
        } else {
            throw new RuntimeException("Game not found with ID: " + gameId);
        }
    }

    public GameStateDto buildGameState(String message) {
        GameStateDto gameStateDto = getGameState(message);
        gameStateDto.setMessage(message);
        return gameStateDto;
    }

    public GameStateDto getGameState(String gameId) {
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        if (gameOptional.isPresent()) {
            Game game = gameOptional.get();
            List<GameMember> members = game.getMemberIds().stream()
                    .map(gameMemberRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            Map<Integer, CardInfo> cardInfoMap = cardInfoRepository.findAll().stream()
                    .collect(Collectors.toMap(CardInfo::getId, Function.identity()));

            members.forEach(member -> {
                if (member.getLeftCard() != null) {
                    CardInfo leftCardInfo = cardInfoMap.get(member.getLeftCard());
                    member.setLeftCardInfo(leftCardInfo);
                }
                if (member.getRightCard() != null) {
                    CardInfo rightCardInfo = cardInfoMap.get(member.getRightCard());
                    member.setRightCardInfo(rightCardInfo);
                }
            });

            // 가능한 액션 가져오기
            Map<String, Integer> result = null;
            if (game.getActionContext().isEmpty()) {
                result = new HashMap<>();
                result.put("수입", 1);
                result.put("해외원조", 2);
                result.put("징세", 3);
                result.put("강탈", 4);
                result.put("암살", 5);
                result.put("교환", 6);
                result.put("쿠", 7);
            } else {
                System.out.println("game.getActionContext().getLast().getActionId() = " + game.getActionContext().getLast().getActionId());
                result = possibleActionRepository.findCanActionNamesAndIdsByActionId(game.getActionContext().getLast().getActionId());
            }

            GameStateDto gameStateDto = getGameStateDto(game, members, result);

            return gameStateDto;
        } else {
            throw new RuntimeException("Game not found with ID: " + gameId);
        }
    }

    private static GameStateDto getGameStateDto(Game game, List<GameMember> members, Map<String, Integer> result) {
        GameStateDto gameStateDto = new GameStateDto();
        gameStateDto.setTurn(game.getTurn());
        gameStateDto.setMembers(members);
        gameStateDto.setHistory(game.getHistory());
        gameStateDto.setWhoseTurn(game.getWhoseTurn());
        gameStateDto.setCanAction(result);
        gameStateDto.setLastContext(game.getActionContext().isEmpty() ? null : game.getActionContext().getLast());
        gameStateDto.setDeck(game.getDeck());
        return gameStateDto;
    }
}
