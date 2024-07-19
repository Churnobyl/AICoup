package com.aicoup.app.domain.entity.game;

import com.aicoup.app.domain.entity.BaseEntity;
import com.aicoup.app.domain.entity.MutableBaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter @Setter
public class Game extends MutableBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Integer id;
    private String name;
    private Integer turn;


    public Game(String name) {
        this.name = name;
    }
}
