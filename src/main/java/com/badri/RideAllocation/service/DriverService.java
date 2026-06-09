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
import java.util.Set;
import java.util.function.DoubleToIntFunction;

@Service
public class DriverService {

    private final StringRedisTemplate redisTemplate;
    private final PresenceService presenceService;

    public DriverService(StringRedisTemplate redisTemplate, PresenceService presenceService) {
        this.redisTemplate = redisTemplate;
        this.presenceService = presenceService;
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
    public void storeCandidateDrivers(List<String> drivers, String redisKey, String rideId) {

        Set<String> rejectedDrivers = fetchRideRejectedDrivers(rideId);
        if(rejectedDrivers == null || rejectedDrivers.isEmpty()) {
            System.out.println("Rejected drivers are not available for this ride");
        } else {
            System.out.println("Rejected Drivers: " + rejectedDrivers);
            drivers.removeAll(rejectedDrivers);
        }

        redisTemplate.opsForList().rightPushAll(redisKey, drivers);
        redisTemplate.expire(redisKey, Duration.ofMinutes(5));
        Long size = redisTemplate.opsForList().size(redisKey);
        System.out.println("Candidate list size = " + size);

        System.out.println("candidate drivers data pushed");
    }

    public Set<String> fetchRideRejectedDrivers(String rideId) {
        try {
            String redisKey = "ride:" + rideId + ":rejectedDrivers";

            return redisTemplate.opsForSet().members(redisKey);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
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

    public void addDriverToActiveDrivers(String driverId, String lng, String lat, String redisKey) {
        try {
            redisTemplate.opsForGeo()
                    .add(
                        redisKey,
                        new Point(Double.parseDouble(lng), Double.parseDouble(lat)),
                        driverId
                    );
            System.out.println("Driver: " + driverId + " is added to active drivers");
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateDriverHeartbeat(String driverId, String heartbeatTime) {
        try {
            presenceService.updateDriverHeartbeat(driverId, heartbeatTime);

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
