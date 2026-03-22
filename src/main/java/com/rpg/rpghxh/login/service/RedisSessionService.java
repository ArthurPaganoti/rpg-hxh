package com.rpg.rpghxh.login.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
public class RedisSessionService {

    private static final String KEY_PREFIX = "auth:";
    private final StringRedisTemplate redisTemplate;

    public RedisSessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveSession(String email, String token, Instant createdAt, Instant expiresAt) {
        String key = KEY_PREFIX + email;

        Map<String, String> sessionData = Map.of(
                "token", token,
                "createdAt", createdAt.toString(),
                "expiresAt", expiresAt.toString()
        );

        redisTemplate.opsForHash().putAll(key, sessionData);
        redisTemplate.expire(key, Duration.between(Instant.now(), expiresAt));
    }

    public void removeSession(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
