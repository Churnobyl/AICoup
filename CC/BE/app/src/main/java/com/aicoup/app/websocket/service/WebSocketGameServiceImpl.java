package com.aicoup.app.websocket.service;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.card.CardInfo;
import com.aicoup.app.domain.entity.game.history.History;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.game.GameGenerator;
import com.aicoup.app.domain.redisRepository.GameMemberRepository;
import com.aicoup.app.domain.redisRepository.GameRepository;
import com.aicoup.app.domain.redisRepository.HistoryRepository;
import com.aicoup.app.domain.repository.CardInfoRepository;
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
    private final HistoryRepository historyRepository;
    private final AIoTSocket aIoTSocket;

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
            if (existGame.isPresent()) {
                return existGame.get().getId();
            }
        }

        String gameId = gameGenerator.init(messageDto.getRoomId());
        recordHistory(gameId, 17, 0, 0);

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
                        .collect(Collectors.toList());

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
                game.setTurn(game.getTurn() + 1);
                gameRepository.save(game);
            }
        }

        return "gameState";
    }

    @Override
    public String myChoice(MessageDto messageDto) {
        return "";
    }

    @Override
    public void recordHistory(String gameId, Integer actionNumber, Integer playerTrying, Integer playerTried) {
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

            GameStateDto gameStateDto = new GameStateDto();
            gameStateDto.setTurn(game.getTurn());
            gameStateDto.setMembers(members);
            gameStateDto.setHistory(game.getHistory());
            gameStateDto.setWhoseTurn(game.getWhoseTurn());
            gameStateDto.setLastAction(game.getActionList().isEmpty() ? null : game.getActionList().getLast());
            gameStateDto.setDeck(game.getDeck());
            return gameStateDto;
        } else {
            throw new RuntimeException("Game not found with ID: " + gameId);
        }
    }
}
