package com.aicoup.app.domain.entity.game;

import com.aicoup.app.domain.entity.MutableBaseEntity;
import com.aicoup.app.domain.entity.game.card.CardInfo;
import com.aicoup.app.domain.entity.game.history.History;
import com.aicoup.app.domain.entity.game.member.GameMember;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;

@RedisHash("game")
@NoArgsConstructor
@Getter @Setter
public class Game extends MutableBaseEntity {

    @Id
    @Indexed
    private String id;
    private String name;
    private Integer turn;
    private List<String> memberIds = new ArrayList<>();
    private Integer[] deck = new Integer[6];
    private List<List<History>> history = new ArrayList();

    public Game(String id) {
        this.id = id;
    }

    public void setInitCards() {
        for (int i = 1; i < 6; i++) {
            if (deck[i] == null) {
                deck[i] = 0;
            }
            deck[i] += 3;
        }
    }
}
