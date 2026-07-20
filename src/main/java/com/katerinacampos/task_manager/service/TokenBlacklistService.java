package com.katerinacampos.task_manager.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private static final String PREFIX = "blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(
                PREFIX + token,
                "true",
                expirationMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(PREFIX + token);
    }
}