package com.aicoup.app.domain.repository;

import com.aicoup.app.domain.entity.game.action.PossibleAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface PossibleActionRepository extends JpaRepository<PossibleAction, Integer> {
    @Query("SELECT new map(a.name as name, a.id as id) " +
            "FROM PossibleAction pa " +
            "JOIN Action a ON pa.canActionId = a.id " +
            "WHERE pa.actionId = :actionId")
    Map<String, Integer> findCanActionNamesAndIdsByActionId(@Param("actionId") Integer actionId);
}
