package com.badri.RideAllocation.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookRideRequestDto {
    @NotNull
    private String userId;
    @NotNull
    private String pickupLat;
    @NotNull
    private String pickupLng;
    @NotNull
    private String dropLat;
    @NotNull
    private String dropLng;
    @NotNull
    private String estimatedFare;
}
