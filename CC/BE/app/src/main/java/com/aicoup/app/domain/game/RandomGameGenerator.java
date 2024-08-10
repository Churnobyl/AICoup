package com.aicoup.app.domain.game;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.redisRepository.GameMemberRepository;
import com.aicoup.app.domain.redisRepository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor
public class RandomGameGenerator implements GameGenerator {

    private final GameRepository gameRepository;
    private final GameMemberRepository gameMemberRepository;

    public Game init(String roomId) {
        Game newGame = new Game(roomId);
        newGame.setTurn(0);
        newGame.setInitCards();
//        newGame.setId(UUID.randomUUID().toString());

        List<GameMember> participantList = new ArrayList<>();

        GameMember player = new GameMember("1", "userA");
        player.setPlayer(true);
        participantList.add(player);

        GPTPlayerCreate(participantList, 4);

        for (GameMember gameMember : participantList) {
            gameMember.setCoin(2);
            gameMember.setLeftCard(pickCard(newGame.getDeck()));
            gameMember.setRightCard(pickCard(newGame.getDeck()));
            gameMemberRepository.save(gameMember);
            newGame.getMemberIds().add(gameMember.getId());
        }

        gameRepository.save(newGame);
        return newGame;
    }

    private int pickCard(int[] deck) {
        Random random = new Random();
        while (true) {
            int i = random.nextInt(5) + 1;
            if (deck[i] == 0) continue;
            else {
                deck[i]--;
                return i;
            }
        }
    }

    private void GPTPlayerCreate(List<GameMember> participantList, int participants) {
//        for (int i = 0; i < participants - 1; i++) {
//            GameMember gameMember = new GameMember();
//            int randomNumber = (int) (Math.random() * 100) + 10;
//            gameMember.setName("GPT" + randomNumber);
//            gameMember.setId(UUID.randomUUID().toString());
//            participantList.add(gameMember);
//        }
    }
}
