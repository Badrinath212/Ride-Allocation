package com.badri.RideAllocation.service;

import org.springframework.data.geo.*;
import org.springframework.data.mapping.AccessOptions;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.function.DoubleToIntFunction;

@Service
public class DriverService {

    private final StringRedisTemplate redisTemplate;

    public DriverService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public List<GeoResult<RedisGeoCommands.GeoLocation<String>>> getNearestNDrivers(Double pickupLat, Double pickupLng) {
        try {
            GeoOperations<String, String> geoOperations = redisTemplate.opsForGeo();

            GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                    geoOperations.search("active_drivers",
                            GeoReference.fromCoordinate(pickupLng, pickupLat),
                            new Distance(10, Metrics.KILOMETERS),
                            RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(50)
                            );

            return results.getContent();

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    public void storeCandidateDrivers(List<String> drivers, String redisKey) {

        redisTemplate.opsForList().rightPushAll(redisKey, drivers);
        redisTemplate.expire(redisKey, Duration.ofMinutes(5));

        System.out.println("candidate drivers data pushed");
    }

    public List<String> fetchCandidateDrivers(int start,int end, String redisKey) {
        List<String> driverBatch = redisTemplate.opsForList().range(redisKey, start, end);
        return driverBatch;
    }

    public void removeDriverFromActiveDrivers(String driverId, String redisKey) {

        try {
            Long count = redisTemplate.opsForZSet().remove(redisKey, driverId);
            if (count != null && count > 0) {
                System.out.println("Driver: " + driverId + " is remove from active drivers");
            } else {
                System.out.println("Something went wrong! while deleting the driver: " + driverId);
            }
        } catch (Exception e) {
            System.out.println("while removing the driver from active_drivers. Failed due to " + e.getMessage());
        }
    }

}
