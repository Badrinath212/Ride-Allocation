package com.badri.RideAllocation.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RideResponseDto {
    private String rideId;
    private String driverId;
    private String status;
}
