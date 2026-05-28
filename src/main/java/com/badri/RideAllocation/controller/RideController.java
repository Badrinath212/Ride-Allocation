package com.badri.RideAllocation.controller;


import com.badri.RideAllocation.dto.*;
import com.badri.RideAllocation.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ride")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @GetMapping("/estFare")
    public ResponseEntity<EstFareResponseDto>getEstFare(@RequestParam String pickupLat,
                                     @RequestParam String pickupLng,
                                     @RequestParam String dropLat,
                                     @RequestParam String dropLng,
                                     @RequestParam String profile) {
        EstFareResponseDto dto = rideService.getEstFare(pickupLat, pickupLng, dropLat, dropLng, profile);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping("/bookRide")
    public ResponseEntity<BookRideResponseDto> bookRide(@Valid @RequestBody BookRideRequestDto dto) {
        System.out.println(dto.toString());

        BookRideResponseDto bookRideResponseDto = rideService.bookRide(dto.getUserId(),dto.getPickupLat(),dto.getPickupLng(),dto.getDropLat(),dto.getDropLng(),dto.getEstimatedFare());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookRideResponseDto);
    }

    @PostMapping("/complete")
    public String completeRide(@Valid @RequestBody CompleteRideDto dto) {
        System.out.println(dto.toString());

        return rideService.rideComplete(dto.getRideId(), dto.getFinalLat(), dto.getFinalLng(), dto.getTimeStamp());
    }

    @PostMapping("/arrived")
    public String driverArrived(@Valid @RequestBody RideArrivedDto dto) {
        return rideService.rideArrived(dto.getRideId(), dto.getDriverId(), dto.getTimeStamp());
    }

    @PostMapping("/start")
    public String rideStart(@Valid @RequestBody RideStartDto dto) {
        return rideService.rideStart(dto.getRideId(), dto.getDriverId(), dto.getTimeStamp());
    }
}
