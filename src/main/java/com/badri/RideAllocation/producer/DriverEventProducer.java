package com.badri.RideAllocation.producer;

import com.badri.RideAllocation.events.DriverAcceptedEvent;
import tools.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DriverEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DriverEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishDriverAccepted(DriverAcceptedEvent event) {

        String json = objectMapper.writeValueAsString(event);
        kafkaTemplate.send("driver-events", event.getDriverId(), json);
        System.out.println("message sent by Kafka");
    }
}
