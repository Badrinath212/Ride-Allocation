package com.badri.RideAllocation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompleteRideDto {
    @NotNull
    private String rideId;
    @NotNull
    private String finalLat;
    @NotNull
    private String finalLng;
    @NotNull
    private String timeStamp;
}
