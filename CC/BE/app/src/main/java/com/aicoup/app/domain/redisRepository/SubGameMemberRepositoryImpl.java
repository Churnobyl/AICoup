package com.aicoup.app.domain.redisRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class SubGameMemberRepositoryImpl implements SubGameMemberRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean existsGameMembersByName(String name) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("game_member:1"));
    }
}
