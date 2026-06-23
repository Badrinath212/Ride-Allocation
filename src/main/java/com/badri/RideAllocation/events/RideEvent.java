package com.badri.RideAllocation.events;

import com.badri.RideAllocation.enums.RideEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideEvent {
    private RideEventType eventType;
    private String rideId;
    private String driverId;
    private String timestamp;
    private Double totalFare;
    private String cancelledBy;
}
