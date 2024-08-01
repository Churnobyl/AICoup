package com.aicoup.app.domain.entity.game.member;

import com.aicoup.app.domain.entity.MutableBaseEntity;
import com.aicoup.app.domain.entity.game.card.CardInfo;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.ArrayList;
import java.util.List;

@RedisHash(value = "game_member", timeToLive = 3600L)
@Getter @Setter
@NoArgsConstructor
public class GameMember extends MutableBaseEntity {

    @Id
    @Indexed
    private String id;
    private String name;
    private boolean isPlayer;
    private Integer coin;
    private Integer leftCard;
    private Integer rightCard;
    private CardInfo leftCardInfo;
    private CardInfo rightCardInfo;
    private List<Integer> actionHistory = new ArrayList<>();

    public GameMember(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
