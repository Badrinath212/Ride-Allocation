package com.badri.RideAllocation.controller;

import com.badri.RideAllocation.dto.LocationUpdateDto;
import com.badri.RideAllocation.service.DriverService;
import com.badri.RideAllocation.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/driver")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> driverHeartbeat(@RequestBody Map<String, Object> requestBody) {
        if(requestBody.isEmpty() || !requestBody.containsKey("driverId")) {
            System.out.println("DriverId is not present for heartbeat update");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String driverId = requestBody.get("driverId").toString();
        System.out.println("Driver controller: " + driverId);
        String heartbeatTime = String.valueOf(Instant.now().toEpochMilli());
        driverService.updateDriverHeartbeat(driverId, heartbeatTime);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/location")
    public String driverLocation(@Valid @RequestBody LocationUpdateDto dto) {
        String driverId = dto.getDriverId();
        String lng = dto.getLng();
        String lat = dto.getLat();
        String lastSeen = String.valueOf(Instant.now().toEpochMilli());

        driverService.updateDriverLocation(driverId, lng, lat, lastSeen);

        return " ";
    }
}
