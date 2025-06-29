package com.example.idea_match.user.jwt;


import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@AllArgsConstructor
@Service
public class RedisTokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX = "blacklisted:";

    public void blacklistToken(String token, long ttlMillis) {
        redisTemplate.opsForValue().set(PREFIX + token, "true", Duration.ofMillis(ttlMillis));
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}
