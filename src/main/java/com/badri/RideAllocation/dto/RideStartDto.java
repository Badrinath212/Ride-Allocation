package com.badri.RideAllocation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RideStartDto {
    @NotNull
    private String rideId;
    @NotNull
    private String driverId;
    @NotNull
    private String timeStamp;
}
