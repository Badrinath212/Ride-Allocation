package com.badri.RideAllocation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CancelRideDto {
    @NotNull
    private String rideId;
    private String driverId;
    private String cancelReason;
}
