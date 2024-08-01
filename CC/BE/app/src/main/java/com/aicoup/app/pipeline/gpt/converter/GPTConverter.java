package com.aicoup.app.pipeline.gpt.converter;


import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.GameData;
import com.aicoup.app.domain.entity.game.card.CardInfo;
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

@Component
@RequiredArgsConstructor
public class GPTConverter {

    private final GameRepository gameRepository;
    private final GameMemberRepository gameMemberRepository;
    private final CardInfoRepository cardInfoRepository;

    public String run(String gameId) {
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
                    .collect(Collectors.toList());

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
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public static String serializeGameState(GPTGameState gameState) throws JsonProcessingException {
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

        rootNode.setAll(playersNode); // Set all player nodes to the root node

        return "[" + mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode) + "]";
    }
}
