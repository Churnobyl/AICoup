package com.aicoup.app.domain.repository;

import com.aicoup.app.domain.entity.game.GameData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameDataRepository extends JpaRepository<GameData, String> {
}
