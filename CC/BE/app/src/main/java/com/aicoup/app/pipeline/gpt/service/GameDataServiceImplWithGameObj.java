package com.aicoup.app.pipeline.gpt.service;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.GameData;
import com.aicoup.app.domain.entity.game.card.CardInfo;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.redisRepository.GameMemberRepository;
import com.aicoup.app.domain.redisRepository.GameRepository;
import com.aicoup.app.domain.repository.CardInfoRepository;
import com.aicoup.app.domain.repository.GameDataRepository;
import com.aicoup.app.pipeline.aiot.dto.MMResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

//@Service
@RequiredArgsConstructor
public class GameDataServiceImplWithGameObj implements GameDataService {

    private final GameRepository gameRepository;
    private final GameMemberRepository gameMemberRepository;
    private final CardInfoRepository cardInfoRepository;
    private final GameDataRepository gameDataRepository;
    private final ObjectMapper objectMapper;

    @Override
    public String getGameDataAsJson(String gameId) {
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        Game game = null;

        if (optionalGame.isPresent()) {
            game = optionalGame.get();
            List<GameMember> members = game.getMemberIds().stream()
                    .map(gameMemberRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            Map<Integer, CardInfo> cardInfoMap = cardInfoRepository.findAll().stream()
                    .collect(Collectors.toMap(CardInfo::getId, Function.identity()));


            GameData gameData = new GameData();
            gameData.setId(game.getId());
            gameData.setPlayerinfo("[\n" +
                    "                    {\n" +
                    "            \"1\": {\"cards\": [\" " + cardInfoMap.get(members.get(0).getLeftCard()).getEnglishName() + "\", \"" + cardInfoMap.get(members.get(0).getRightCard()).getEnglishName() + "\"], \"cards_open\": [" + (members.get(0).getLeftCard() > 0 ? "false" : "true") + ", " + (members.get(0).getRightCard() > 0 ? "false" : "true") + "], \"coins\": " + members.get(0).getCoin() + "},\n" +
                    "            \"2\": {\"cards\": [\" " + cardInfoMap.get(members.get(1).getLeftCard()).getEnglishName() + "\", \"" + cardInfoMap.get(members.get(1).getRightCard()).getEnglishName() + "\"], \"cards_open\": [" + (members.get(1).getLeftCard() > 0 ? "false" : "true") + ", " + (members.get(1).getRightCard() > 0 ? "false" : "true") + "], \"coins\": " + members.get(1).getCoin() + "},\n" +
                    "            \"3\": {\"cards\": [\" " + cardInfoMap.get(members.get(2).getLeftCard()).getEnglishName() + "\", \"" + cardInfoMap.get(members.get(2).getRightCard()).getEnglishName() + "\"], \"cards_open\": [" + (members.get(2).getLeftCard() > 0 ? "false" : "true") + ", " + (members.get(2).getRightCard() > 0 ? "false" : "true") + "], \"coins\": " + members.get(2).getCoin() + "},\n" +
                    "            \"4\": {\"cards\": [\" " + cardInfoMap.get(members.get(3).getLeftCard()).getEnglishName() + "\", \"" + cardInfoMap.get(members.get(3).getRightCard()).getEnglishName() + "\"], \"cards_open\": [" + (members.get(3).getLeftCard() > 0 ? "false" : "true") + ", " + (members.get(3).getRightCard() > 0 ? "false" : "true") + "], \"coins\": " + members.get(3).getCoin() + "},\n" +
                    "               }\n" +
                    "           ]");
//            gameData.setHistory();
        }




        return "";
    }

    @Override
    public String getFormattedGameDataAsJson(String gameId) {
        return "";
    }
}
