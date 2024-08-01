package com.aicoup.app.domain.entity.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class GameData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @Column(name = "player_num", nullable = false)
    private int playerNum;

    @Column(name = "current_player", nullable = false)
    private int currentPlayer;

    @Column(name = "playerinfo", columnDefinition = "JSON", nullable = false)
    private String playerinfo;

    @Column(name = "history", columnDefinition = "JSON", nullable = false)
    private String history;

    @Column(name = "current_action", nullable = true)
    private String currentAction;

    @Column(name = "target", nullable = true)
    private String target;

    @Column(name = "counter_actioner", nullable = true)
    private String counterActioner;

    @Column(name = "counter_action", nullable = true)
    private String counterAction;
}
