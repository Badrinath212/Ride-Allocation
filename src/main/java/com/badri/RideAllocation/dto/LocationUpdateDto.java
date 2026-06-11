package com.badri.RideAllocation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocationUpdateDto {
    @NotNull
    private String driverId;
    @NotNull
    private String lng;
    @NotNull
    private String lat;
}
