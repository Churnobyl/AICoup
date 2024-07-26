package com.aicoup.app.domain.entity.game.action;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <pre>* For RDB</pre>
 * 플레이어들이 수행하는 액션
 * 
 * @id Integer 액션 아이디
 * @name String 액션 이름
 * @englishName String 영어 액션 이름
 * @description String 액션 설명
 * @specificCharacter Integer 액션을 수행할 수 있는 캐릭터
 * @counteractionCharacter1 Integer 액션을 막을 수 있는 캐릭터1
 * @counteractionCharacter2 Integer 액션을 막을 수 있는 캐릭터2
 * @canBeBlocked Boolean 막을 수 있는 액션인지 여부
 * @canBeChallenged Boolean 의심할 수 있는 액션인지 여부
 */
@Entity
@Table(name = "action")
@NoArgsConstructor
@Getter @Setter
public class Action {

    @Id
    @Column(name = "action_id")
    private Integer id;
    private String name;
    @Column(name = "english_name")
    private String englishName;
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
