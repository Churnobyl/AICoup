package com.aicoup.app.domain.redisRepository;

import com.aicoup.app.domain.entity.game.history.History;
import org.springframework.data.repository.ListCrudRepository;

public interface HistoryRepository extends ListCrudRepository<History, String> {
}
