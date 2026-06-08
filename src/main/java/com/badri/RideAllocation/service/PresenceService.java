package com.badri.RideAllocation.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PresenceService {
    private final StringRedisTemplate redisTemplate;

    public PresenceService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void markDriverOnline(String driverId) {
        redisTemplate.opsForValue().set("driver:" + driverId, "ONLINE");
    }
    public void markDriverOffline(String driverId) {
        redisTemplate.delete("driver:" + driverId);
    }
}
