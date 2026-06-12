package com.badri.RideAllocation.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
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

    public String updateDriverLocation(String driverId, String lng, String lat, String lastSeen) {
        String redisKey = "driver:presence:" + driverId;

        redisTemplate.opsForHash().put(redisKey, "lng", lng);
        redisTemplate.opsForHash().put(redisKey, "lat", lat);
        redisTemplate.opsForHash().put(redisKey, "lastSeen", lastSeen);
        redisTemplate.expire(redisKey, Duration.ofSeconds(30));

        return "Location Updated";

    }

    public boolean isOnline(String driverId) {
        String redisKey = "driver:presence:" + driverId;

        Object statusObj = redisTemplate.opsForHash().get(redisKey, "status");

        if(statusObj == null) return false;

        return "ONLINE".equals(statusObj.toString());

    }
}
