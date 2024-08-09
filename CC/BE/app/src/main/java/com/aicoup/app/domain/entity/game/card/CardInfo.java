package com.aicoup.app.domain.entity.game.card;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <pre>* For RDB</pre>
 * 카드의 정보
 *
 * @id Integer 카드 아이디
 * @name String 카드 이름
 * @imageUrl String 카드 이미지 url
 */
@Entity
@NoArgsConstructor
@Getter @Setter
@Table(name = "card_info")
public class CardInfo {
    @Id
    @Column(name ="card_info_id")
    private Integer id;
    private String name;
    @Column(name = "english_name")
    private String englishName;
    private String imageUrl;

    @Override
    public String toString() {
        return "CardInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", englishName='" + englishName + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
