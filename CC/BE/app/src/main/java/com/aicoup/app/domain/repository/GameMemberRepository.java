package com.aicoup.app.domain.repository;

import com.aicoup.app.domain.entity.game.member.GameMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameMemberRepository extends JpaRepository<GameMember, String> {
}
