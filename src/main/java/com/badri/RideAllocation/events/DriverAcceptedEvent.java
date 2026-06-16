package com.badri.RideAllocation.events;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DriverAcceptedEvent {
    private String driverId;
}
