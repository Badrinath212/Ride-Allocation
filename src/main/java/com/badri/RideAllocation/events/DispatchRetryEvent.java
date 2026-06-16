package com.badri.RideAllocation.events;

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
