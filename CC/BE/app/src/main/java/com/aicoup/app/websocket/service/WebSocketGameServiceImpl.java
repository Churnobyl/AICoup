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
import com.aicoup.app.websocket.model.dto.GameStateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

    /**
     * 게임 시작
     * @return
     */
    public String gameInit(String roomId) {
        return gameGenerator.init(roomId, 4);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public String nextTurn() {
        return "";
    }

    @Override
    public String myChoice() {
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
        GameStateDto gameStateDto = getGameState("1");
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
            return gameStateDto;
        } else {
            throw new RuntimeException("Game not found with ID: " + gameId);
        }
    }
}
