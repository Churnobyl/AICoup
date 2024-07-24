package com.aicoup.app.domain.repository;

import com.aicoup.app.domain.entity.game.card.CardInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Integer> {
}
