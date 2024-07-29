package com.aicoup.app.domain.entity.game;

import com.aicoup.app.domain.entity.MutableBaseEntity;
import com.aicoup.app.domain.entity.game.member.GameMember;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.util.ArrayList;
import java.util.List;

//@RedisHash("game")
@NoArgsConstructor
@Getter @Setter
public class GameKeep extends MutableBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Integer id;
    private String name;
    private Integer turn;
    @OneToMany(mappedBy = "game")
    private List<GameMember> members = new ArrayList<>();
    private Integer[] deck = new Integer[6];

    public GameKeep(String name) {
        this.name = name;
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
