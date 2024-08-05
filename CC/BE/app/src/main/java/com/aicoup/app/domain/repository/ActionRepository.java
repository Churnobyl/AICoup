package com.aicoup.app.domain.repository;

import com.aicoup.app.domain.entity.game.action.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActionRepository extends JpaRepository<Action, Integer> {
    Optional<Action> findByEnglishName(String englishName);
}