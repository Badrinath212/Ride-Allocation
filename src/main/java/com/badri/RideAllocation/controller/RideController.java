package com.badri.RideAllocation.controller;


import com.badri.RideAllocation.dto.BookRideRequestDto;
import com.badri.RideAllocation.dto.BookRideResponseDto;
import com.badri.RideAllocation.dto.EstFareResponseDto;
import com.badri.RideAllocation.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
