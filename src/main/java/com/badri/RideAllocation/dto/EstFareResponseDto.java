package com.badri.RideAllocation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstFareResponseDto {
    private String pickupLat;
    private String pickupLng;
    private String dropLat;
    private String dropLng;
    private Double estimatedFare;


}
