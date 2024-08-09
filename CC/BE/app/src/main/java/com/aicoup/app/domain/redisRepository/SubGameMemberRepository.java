package com.aicoup.app.domain.redisRepository;

public interface SubGameMemberRepository {
    boolean existsGameMembersByName(String name);
}
