package com.aicoup.app.pipeline.gpt.converter;


import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.GameData;
import com.aicoup.app.domain.entity.game.card.CardInfo;
import com.aicoup.app.domain.entity.game.history.History;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.redisRepository.GameMemberRepository;
import com.aicoup.app.domain.redisRepository.GameRepository;
import com.aicoup.app.domain.repository.CardInfoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class GPTConverter {

    private final GameRepository gameRepository;
    private final GameMemberRepository gameMemberRepository;
    private final CardInfoRepository cardInfoRepository;

    private final Map<Integer, String> actionCharacterMapper = new HashMap<>() {{
        put(3, "duke");
        put(4, "captain");
        put(5, "assassin");
        put(6, "ambassdor");
        put(22, "contessa");
    }};

    public GameData run(String gameId) {
        GameData gameData = new GameData();

        Optional<Game> optionalGame = gameRepository.findById(gameId);
        Game game = null;

        Map<Integer, GPTPlayer> players = new HashMap<>();

        if (optionalGame.isPresent()) {
            game = optionalGame.get();
            List<GameMember> members = game.getMemberIds().stream()
                    .map(gameMemberRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

            Map<Integer, CardInfo> cardInfoMap = cardInfoRepository.findAll().stream()
                    .collect(Collectors.toMap(CardInfo::getId, Function.identity()));

            for (int i = 1; i <= members.size(); i++) {
                players.put(i, new GPTPlayer(Arrays.asList(cardInfoMap.get(members.get(i - 1).getLeftCard()).getEnglishName(), cardInfoMap.get(members.get(i - 1).getRightCard()).getEnglishName()), Arrays.asList(members.get(0).getLeftCard() <= 0, members.get(0).getRightCard() <= 0), members.get(i - 1).getCoin()));
            }

            GPTGameState gameState = new GPTGameState(players);

            try {
                gameData.setPlayerinfo(serializeGameState(gameState));
//                gameData.setHistory();
                gameData.setId(game.getId());
                gameData.setCurrentPlayer(game.getWhoseTurn());
                gameData.setPlayerNum(game.getMemberIds().size());

                // 추가 정보
                LinkedList<History> actionContext = game.getActionContext();
                if (!actionContext.isEmpty()) {
                    int size = actionContext.size();

                    History last = actionContext.getLast();

                    // 앞전 플레이에 대응하거나 의심
                    if (size == 1) {
                        gameData.setCurrentAction(cardInfoMap.get(last.getActionId()).getEnglishName());
                        String targetMemberId = last.getPlayerTried();
                        gameData.setTarget(targetMemberId == null ? "none" : String.valueOf(IntStream.range(0, members.size())
                                .filter(i -> members.get(i).getId().equals(targetMemberId))
                                .findFirst().getAsInt() + 1));
                    } else if (size == 2) { // 앞전 플레이는 대응한 거라 거기에 의심
                        gameData.setCounterAction(actionCharacterMapper.get(last.getActionId()));
                        String targetMemberId = last.getPlayerTried();
                        gameData.setCounterActioner(String.valueOf(IntStream.range(0, members.size())
                                .filter(i -> members.get(i).getId().equals(targetMemberId))
                                .findFirst().getAsInt() + 1));
                    }
                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return gameData;
    }

    private static String serializeGameState(GPTGameState gameState) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        ObjectNode playersNode = mapper.createObjectNode();

        for (Map.Entry<Integer, GPTPlayer> entry : gameState.getPlayers().entrySet()) {
            ObjectNode playerNode = mapper.createObjectNode();
            playerNode.putPOJO("cards", entry.getValue().getCards());
            playerNode.putPOJO("cards_open", entry.getValue().getCardsOpen());
            playerNode.put("coins", entry.getValue().getCoins());
            playersNode.set(entry.getKey().toString(), playerNode);
        }

        rootNode.setAll(playersNode);

        return "[" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode) + "]";
    }
}
