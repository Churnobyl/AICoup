package com.aicoup.app.domain.entity.game.card;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter @Setter
@Table(name = "card_info")
public class CardInfo {

    @Id
    @Column(name ="card_info_id")
    private Integer id;
    private String name;
    private String description;
    private String imageUrl;
}
