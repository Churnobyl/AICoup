package com.aicoup.app.domain.game;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.redisRepository.GameMemberRepository;
import com.aicoup.app.domain.redisRepository.GameRepository;
import com.aicoup.app.pipeline.aiot.AIoTSocket;
import com.aicoup.app.pipeline.aiot.dto.MMResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AIoTGameGenerator implements GameGenerator {
    private final GameRepository gameRepository;
    private final GameMemberRepository gameMemberRepository;
    private final AIoTSocket aIoTSocket;

    public String init(String roomId) {
        Game newGame = new Game(roomId);
        newGame.setTurn(0);
        newGame.setInitCards();
        newGame.setId(UUID.randomUUID().toString());

        List<GameMember> participantList = new ArrayList<>();

        GameMember player = new GameMember("1", "userA");
        player.setPlayer(true);
        participantList.add(player);

        List<MMResponse> dataFromAIoTServer = aIoTSocket.getDataFromAIoTServer();
        int participants = dataFromAIoTServer.size();

        Random random = new Random();
        
        // 랜덤으로 플레이어 순서 세팅
//        newGame.setWhoseTurn(random.nextInt(participants));
        newGame.setWhoseTurn(0);

        GPTPlayerCreate(participantList, participants);

        for (int i = 0; i < participants; i++) {
            GameMember gameMember = participantList.get(i);
            MMResponse data = dataFromAIoTServer.get(i);

            gameMember.setCoin(2);
            gameMember.setLeftCard(data.getLeft_card());
            gameMember.setRightCard(data.getRight_card());

            int[] deck = newGame.getDeck();
            deck[data.getLeft_card()]--;
            deck[data.getRight_card()]--;

            gameMemberRepository.save(gameMember);
            newGame.getMemberIds().add(gameMember.getId());
        }

        gameRepository.save(newGame);
        return newGame.getId();
    }

    private void GPTPlayerCreate(List<GameMember> participantList, int participants) {
        for (int i = 0; i < participants - 1; i++) {
            GameMember gameMember = new GameMember();
            int randomNumber = (int) (Math.random() * 100) + 10;
            gameMember.setName("GPT" + randomNumber);
            gameMember.setId(UUID.randomUUID().toString());
            participantList.add(gameMember);
        }
    }
}
