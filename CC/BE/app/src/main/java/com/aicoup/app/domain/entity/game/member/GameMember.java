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
import java.util.Objects;

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
    private String personality;
    private CardInfo leftCardInfo;
    private CardInfo rightCardInfo;
    private List<Integer> actionHistory = new ArrayList<>();

    public GameMember(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean hasCard(Integer actionValue, int cardOpen) {
        Integer cardId = switch (actionValue) {
            case 3 -> 1;
            case 4 -> 2;
            case 5 -> 3;
            case 6 -> 5;
            default -> 0;
        };
        if (cardOpen==0 && Objects.equals(leftCard, cardId)) {
            return true;
        }
        else if (cardOpen==1 && Objects.equals(rightCard, cardId)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "GameMember{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isPlayer=" + isPlayer +
                ", coin=" + coin +
                ", leftCard=" + leftCard +
                ", rightCard=" + rightCard +
                ", leftCardInfo=" + leftCardInfo +
                ", rightCardInfo=" + rightCardInfo +
                ", actionHistory=" + actionHistory +
                '}';
    }
}
