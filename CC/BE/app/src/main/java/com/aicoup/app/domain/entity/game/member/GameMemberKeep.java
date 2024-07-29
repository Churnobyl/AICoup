package com.aicoup.app.domain.entity.game.member;

import com.aicoup.app.domain.entity.MutableBaseEntity;
import com.aicoup.app.domain.entity.game.Game;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

//@RedisHash("game_member")
@NoArgsConstructor
@Getter @Setter
@Table(name = "game_member")
public class GameMemberKeep extends MutableBaseEntity {

    @Id
    @Column(name = "game_member_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    private String name;
    private Integer positionX;
    private Integer positionY;
    private Integer leftCard;
    private Integer rightCard;
    private Integer coin;
    private Boolean isPlayer;
}
