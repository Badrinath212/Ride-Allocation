package com.badri.RideAllocation.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class AnalyticsConsumer {

    private final ObjectMapper objectMapper;

    public AnalyticsConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "ride-events",
            groupId = "analytics-consumer"
    )
    public void consumeRideRequestEvent(String event) {

    }
}
