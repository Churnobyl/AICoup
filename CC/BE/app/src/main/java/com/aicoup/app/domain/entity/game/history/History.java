package com.aicoup.app.domain.entity.game.history;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * <pre>* For Redis</pre>
 * 히스토리 객체
 * 
 * @id String 히스토리 아이디
 * @turn int 턴
 * @actionId Integer 해당 턴의 액션
 * @playerTrying Integer 시도하는 플레이어 넘버
 * @playerTried Integer 대상 플레이어 넘버
 */
@RedisHash("history")
@NoArgsConstructor
@Getter @Setter
public class History {

    @Id @Indexed
    private String id;
    private int turn;
    private Integer actionId;
    private Integer playerTrying;
    private Integer playerTried;

    public History(String id, Integer actionId, Integer playerTrying, Integer playerTried) {
        this.id = id;
        this.actionId = actionId;
        this.playerTrying = playerTrying;
        this.playerTried = playerTried;
    }
}
