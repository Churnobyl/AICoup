package com.aicoup.app.domain.entity.game.action;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "possible_action")
@NoArgsConstructor
@Getter
@Setter
public class PossibleAction {
    @Id
    @Column(name = "possible_action_id")
    private Integer id;
    @Column(name = "action_id")
    private Integer actionId;
    @Column(name = "can_action_id")
    private Integer canActionId;
}
