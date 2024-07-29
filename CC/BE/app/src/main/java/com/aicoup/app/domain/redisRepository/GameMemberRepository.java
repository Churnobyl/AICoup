package com.aicoup.app.domain.redisRepository;

import com.aicoup.app.domain.entity.game.member.GameMember;
import org.springframework.data.repository.ListCrudRepository;

public interface GameMemberRepository extends ListCrudRepository<GameMember, String>, SubGameMemberRepository {

}
