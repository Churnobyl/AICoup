package com.aicoup.app.domain.entity.game;

import com.aicoup.app.domain.entity.MutableBaseEntity;
import com.aicoup.app.domain.entity.game.history.History;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * <pre>* For Redis</pre>
 * 1개 게임의 정보를 담는 Entity
 * @id String 게임 아이디
 * @name String 게임 이름
 * @turn Integer 현재 턴수
 * @memberIds List&lt;String&gt; 게임에 참가하고 있는 참가자 아이디
 * @deck Integer[] 덱에 있는 남은 카드 수
 * @history List&lt;History&gt; 게임 히스토리
 */
@RedisHash(value = "game", timeToLive = 3600L)
@NoArgsConstructor
@Getter @Setter
public class Game extends MutableBaseEntity {

    @Id
    @Indexed
    private String id;
    private String name;
    private Integer turn;
    private int whoseTurn;
    private LinkedList<History> actionContext = new LinkedList<>();
    private List<String> memberIds = new ArrayList<>();
    private int[] deck = new int[6];
    private List<History> history = new ArrayList<>();

    /**
     * id 생성자
     * @param id
     */
    public Game(String id) {
        this.id = id;
    }

    /**
     * 초기 카드 세팅
     */
    public void setInitCards() {
        for (int i = 1; i < 6; i++) {
            deck[i] += 3;
        }
    }

    /**
     * 히스토리 추가
     * @param oneHistory
     */
    public void addHistory(History oneHistory) {
        history.add(oneHistory);
    }
}
