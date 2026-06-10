package com.badri.RideAllocation.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class PresenceService {
    private final StringRedisTemplate redisTemplate;

    public PresenceService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateDriverPresence(String driverId, String status, String lastSeen) {
        String redisKey = "driver:presence:" + driverId;
        redisTemplate.opsForHash().put(redisKey, "status", status);
        redisTemplate.opsForHash().put(redisKey, "lastSeen", lastSeen);
        redisTemplate.expire(redisKey, Duration.ofSeconds(30));
    }

    public void updateDriverHeartbeat(String driverId, String lastSeen) {
        String redisKey = "driver:presence:" + driverId;
        if(redisTemplate.hasKey(redisKey)) {
            redisTemplate.opsForHash().put(redisKey, "lastSeen", lastSeen);
            redisTemplate.expire(redisKey, Duration.ofSeconds(30));
        }
    }
}
