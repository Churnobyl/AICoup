package com.aicoup.app.domain.game;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.redisRepository.GameMemberRepository;
import com.aicoup.app.domain.redisRepository.GameRepository;
import com.aicoup.app.pipeline.aiot.AIoTSocket;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class AIoTGameGenerator implements GameGenerator {

    private final GameRepository gameRepository;
    private final GameMemberRepository gameMemberRepository;
    private final AIoTSocket aIoTSocket;

    @Transactional
    public String init(String gamename, Integer participants) {
        Game newGame = new Game(gamename);
        Game savedGame = gameRepository.save(newGame);

        String data = aIoTSocket.getDataFromAIoTServer();
        List<GameMember> participantList = new ArrayList<>();

        GameMember player = new GameMember();
        player.setIsPlayer(true);

        participantList.add(player);

        for (int i = 0; i < participants - 1; i++) {
            GameMember gameMember = new GameMember();
            participantList.add(gameMember);
        }

        List<GameMember> gameMembers = (List<GameMember>) gameMemberRepository.saveAll(participantList);
        return "a";
    }
}
