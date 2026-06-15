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
    public ResponseEntity<String> completeRide(@Valid @RequestBody CompleteRideDto dto) {
        System.out.println(dto.toString());

        String response = rideService.rideComplete(dto.getRideId(), dto.getFinalLat(), dto.getFinalLng());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/arrived")
    public ResponseEntity<String> driverArrived(@Valid @RequestBody RideArrivedDto dto) {
        String response = rideService.rideArrived(dto.getRideId(), dto.getDriverId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/start")
    public ResponseEntity<String> rideStart(@Valid @RequestBody RideStartDto dto) {
        String response = rideService.rideStart(dto.getRideId(), dto.getDriverId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/cancel/rider")
    public ResponseEntity<String> rideCancelByRider(@Valid @RequestBody CancelRideDto dto) {
        String response = rideService.rideCancelByRider(dto.getRideId(), dto.getDriverId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/cancel/driver")
    public ResponseEntity<String> rideCancelByDriver(@Valid @RequestBody CancelRideDto dto) {
        String response = rideService.rideCancelByDriver(dto.getRideId(), dto.getDriverId(), dto.getCancelReason());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
