package com.aicoup.app.domain.entity.game.history;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@RedisHash("history")
@NoArgsConstructor
@Getter @Setter
public class History {

    @Id @Indexed
    private String id;
    private Integer actionId;
    private String playerTrying;
    private String playerTried;

}
