package com.badri.RideAllocation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatchRetryEvent {
    private String rideId;
    private int startIndex;
}
