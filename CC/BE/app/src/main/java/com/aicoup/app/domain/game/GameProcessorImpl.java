package com.aicoup.app.domain.game;

import com.aicoup.app.domain.entity.game.Game;
import com.aicoup.app.domain.entity.game.history.History;
import com.aicoup.app.domain.redisRepository.GameRepository;
import com.aicoup.app.pipeline.gpt.ChatGPTSocket;
import com.aicoup.app.pipeline.gpt.service.GPTResponseGetter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
@RequiredArgsConstructor
public class GameProcessorImpl implements GameProcessor {

    private final GameRepository gameRepository;
    private final SimpMessagingTemplate template;
    private final GPTResponseGetter gptResponseGetter;

    Game presentGame = null;

    @Override
    public String run(Game game) {
        // 게임 객체 저장
        presentGame = game;

        // 턴 증가
        presentGame.setTurn(presentGame.getTurn() + 1);
        gameRepository.save(presentGame);

        // 현재 게임 상태 불러오기
        LinkedList<History> actionContext = presentGame.getActionContext();

        String returnMessage = null;

        // 만약 새롭게 턴을 시작한다면
        if (actionContext.isEmpty()) {
            returnMessage = runInitContext();
        } else {
            returnMessage = runReactionContext();
        }

        return returnMessage;
    }

    @Override
    public String runInitContext() {
        // 이번 턴의 행동을 수행할 사람
        int whoseTurn = presentGame.getWhoseTurn();
        String nextPlayer = presentGame.getMemberIds().get(whoseTurn);
        System.out.println("nextPlayer = " + nextPlayer);

        // 이번 턴의 행동을 수행할 사람이 플레이어일 경우
        if (nextPlayer.equals("1")) {
            // 플레이어 턴이라고 프론트에 알려줌
            History history = new History(presentGame.getId(), 18, "1", "0");
            presentGame.addHistory(history);
            gameRepository.save(presentGame);
            return "yourTurn";
        } else { // GPT의 행동일 경우
            String[] dataFromGptApiForAction = gptResponseGetter.actionApi(presentGame.getId());

            History history = new History(presentGame.getId(), 18, nextPlayer, dataFromGptApiForAction[1].equals("none") ? "none" : dataFromGptApiForAction[1]);
            presentGame.addHistory(history);
            presentGame.getActionContext().add(history);
            gameRepository.save(presentGame);
            return "gameState";
        }
    }

    @Override
    public String runReactionContext() {
        // 이번 턴의 행동을 수행할 사람
        int whoseTurn = presentGame.getWhoseTurn();
        String nextPlayer = presentGame.getMemberIds().get(whoseTurn);

        return "";
    }

    private boolean GPTResponseValidate() {

        return true;
    }
}
