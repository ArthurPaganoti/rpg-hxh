package com.rpg.rpghxh.rooms.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class RedisInviteService {

    private static final String KEY_PREFIX = "invite:";
    private static final Duration INVITE_TTL = Duration.ofHours(8);

    private final StringRedisTemplate redisTemplate;

    public RedisInviteService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getOrCreateInvite(UUID roomId, Long masterId) {
        String key = KEY_PREFIX + roomId;

        String existingHash = (String) redisTemplate.opsForHash().get(key, "inviteHash");
        if (existingHash != null) {
            return existingHash;
        }

        String inviteHash = UUID.randomUUID().toString();

        Map<String, String> inviteData = Map.of(
                "inviteHash", inviteHash,
                "masterId", masterId.toString(),
                "createdAt", Instant.now().toString()
        );

        redisTemplate.opsForHash().putAll(key, inviteData);
        redisTemplate.expire(key, INVITE_TTL);

        return inviteHash;
    }

    public void removeInvite(UUID roomId) {
        redisTemplate.delete(KEY_PREFIX + roomId);
    }
}
