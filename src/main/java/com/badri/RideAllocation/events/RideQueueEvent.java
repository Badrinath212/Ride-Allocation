package com.badri.RideAllocation.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideQueueEvent {
    private String rideId;
    private String status;
    private String pickupLat;
    private String pickupLng;
    private String estimatedFare;
}
