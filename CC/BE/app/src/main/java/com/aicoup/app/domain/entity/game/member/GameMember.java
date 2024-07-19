package com.aicoup.app.domain.entity.game.member;

import com.aicoup.app.domain.entity.BaseEntity;
import com.aicoup.app.domain.entity.MutableBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter @Setter
@Table(name = "game_member")
public class GameMember extends MutableBaseEntity {

    @Id
    @Column(name = "game_member_id")
    private Integer id;
    private String name;
    private Integer positionX;
    private Integer positionY;
    private Integer leftCard;
    private Integer rightCard;
    private Integer coin;
    private Boolean isPlayer;
}
