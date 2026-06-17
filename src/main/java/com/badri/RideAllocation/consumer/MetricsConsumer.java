package com.badri.RideAllocation.consumer;

import com.badri.RideAllocation.events.DriverAcceptedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MetricsConsumer {
    @KafkaListener(
            topics = "driver-events",
            groupId = "metrics-group"
    )
    public void consumeDriverAcceptedEvent(DriverAcceptedEvent event) {
        System.out.println("Driver Event from kafka: " + event.toString());
    }
}
