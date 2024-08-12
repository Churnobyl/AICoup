package com.aicoup.app.domain.entity.game.history;

import jakarta.persistence.Id;
import lombok.*;
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
@RedisHash(value = "history", timeToLive = 3600L)
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class History {

    @Id @Indexed
    private String id;
    private int turn;
    private Integer actionId;
    private Boolean actionState;
    private String playerTrying;
    private String playerTried;

    public History(String id, Integer actionId, String playerTrying, String playerTried) {
        this.id = id;
        this.actionId = actionId;
        this.playerTrying = playerTrying;
        this.playerTried = playerTried;
    }

    @Override
    public String toString() {
        return "History{" +
                "id='" + id + '\'' +
                ", turn=" + turn +
                ", actionId=" + actionId +
                ", actionState=" + actionState +
                ", playerTrying='" + playerTrying + '\'' +
                ", playerTried='" + playerTried + '\'' +
                '}';
    }
}
