package com.aicoup.app.domain.game;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.member.GameMember;
import com.aicoup.app.domain.redisRepository.GameMemberRepository;
import com.aicoup.app.domain.redisRepository.GameRepository;
import com.aicoup.app.pipeline.aiot.AIoTSocket;
import com.aicoup.app.pipeline.aiot.AIoTSocketImpl;
import com.aicoup.app.pipeline.aiot.dto.MMResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class AIoTGameGenerator implements GameGenerator {
    private final GameRepository gameRepository;
    private final GameMemberRepository gameMemberRepository;
    private final AIoTSocket aIoTSocket;

    public Game init(String roomId) {
        Game newGame = new Game(roomId);
        newGame.setTurn(0);
        newGame.setInitCards();
        newGame.setId(UUID.randomUUID().toString());

        List<GameMember> participantList = new ArrayList<>();

        GameMember player = new GameMember("1", "Player");
        player.setPlayer(true);
        participantList.add(player);

        String jsonBody = "{ \"name\": \"\" }";

        List<MMResponse> dataFromAIoTServer = aIoTSocket.getDataFromAIoTServer(jsonBody); // 이 함수 파라미터에 문자열 통으로 넣으면 됨
        int participants = dataFromAIoTServer.size();

        Random random = new Random();
        
        // 랜덤으로 플레이어 순서 세팅
//        newGame.setWhoseTurn(random.nextInt(participants));
        newGame.setWhoseTurn(0);

        GPTPlayerCreate(participantList, participants);

        List<String> personalities = new ArrayList<>(Arrays.asList("anger", "joy", "sadness", "fear", "disgust"));
        Collections.shuffle(personalities);

        for (int i = 0; i < participants; i++) {
            GameMember gameMember = participantList.get(i);
            MMResponse data = dataFromAIoTServer.get(i);

            gameMember.setCoin(2);
            gameMember.setLeftCard(data.getLeft_card());
            gameMember.setRightCard(data.getRight_card());

            // 성격 할당
            if (i < personalities.size()) {
                gameMember.setPersonality(personalities.get(i));

                if (i > 0) {
                    gameMember.setName(personalities.get(i) + " " + gameMember.getName());
                }
            } else {
                // 만약 참가자 수가 성격 유형보다 많다면, 랜덤하게 성격을 재사용
                gameMember.setPersonality(personalities.get(new Random().nextInt(personalities.size())));
            }

            int[] deck = newGame.getDeck();
            deck[data.getLeft_card()]--;
            deck[data.getRight_card()]--;

            gameMemberRepository.save(gameMember);
            newGame.getMemberIds().add(gameMember.getId());
        }

        gameRepository.save(newGame);
        return newGame;
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
