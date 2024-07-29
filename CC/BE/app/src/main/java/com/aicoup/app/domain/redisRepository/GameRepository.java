package com.aicoup.app.domain.redisRepository;

import com.aicoup.app.domain.entity.game.Game;
import org.springframework.data.repository.ListCrudRepository;

public interface GameRepository extends ListCrudRepository<Game, String> {

}
