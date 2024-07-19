package com.aicoup.app.domain.entity.game.action;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "action")
@NoArgsConstructor
@Getter @Setter
public class Action {

    @Id
    @Column(name = "action_id")
    private Integer id;
    private String name;
    private String english_name;
    private String description;
    @Column(name = "specific_character")
    private Integer specificCharacter;
    @Column(name = "counteraction_character1")
    private Integer counteractionCharacter1;
    @Column(name = "counteraction_character2")
    private Integer counteractionCharacter2;
    @Column(name = "can_be_blocked")
    private Boolean canBeBlocked;
    @Column(name = "can_be_challenged")
    private Boolean canBeChallenged;

}
