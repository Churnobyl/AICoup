package com.aicoup.app.domain.repository;

import com.aicoup.app.domain.entity.game.action.PossibleAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PossibleActionRepository extends JpaRepository<PossibleAction, Integer> {
}
