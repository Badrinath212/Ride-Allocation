package com.badri.RideAllocation.producer;

import com.badri.RideAllocation.events.DriverAcceptedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DriverEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DriverEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDriverAccepted(DriverAcceptedEvent event) {
        kafkaTemplate.send("driver-events", event);
        System.out.println("message sent by Kafka");
    }
}
