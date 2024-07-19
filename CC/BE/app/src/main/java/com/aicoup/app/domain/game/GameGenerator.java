package com.aicoup.app.domain.game;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.repository.GameMemberRepository;
import com.aicoup.app.domain.repository.GameRepository;
import com.aicoup.app.pipeline.aiot.AIoTSocket;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GameGenerator {

    private final GameRepository gameRepository;
    private final GameMemberRepository gameMemberRepository;
    private final AIoTSocket aIoTSocket;

    @Transactional
    public Integer init(String gamename, Integer participants) {
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

        List<GameMember> gameMembers = gameMemberRepository.saveAll(participantList);
        return 0;
    }
}
