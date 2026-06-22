package com.badri.RideAllocation.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class RideEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public RideEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishRideRequestEvent(String event, String id) {
        kafkaTemplate.send(
                "ride-events",
                id,
                event
        );
        System.out.println("Ride request sent to Kafka: " + event);
    }
}
