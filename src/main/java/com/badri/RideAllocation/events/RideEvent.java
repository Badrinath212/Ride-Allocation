package com.badri.RideAllocation.events;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RideEvent {
    private String eventType;
    private String rideId;
    private String driverId;
    private String timestamp;
}
