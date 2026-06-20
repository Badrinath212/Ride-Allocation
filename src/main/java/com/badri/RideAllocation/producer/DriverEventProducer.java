package com.badri.RideAllocation.producer;

import tools.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DriverEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public DriverEventProducer(KafkaTemplate<String, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }


    public void testEvent(String event) {
        try {
            System.out.println("Before sending the test event");
            kafkaTemplate.send("test-events", event);
            System.out.println("Test event sent successfully");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void publishDriverRideResponseEvent(String event, String id) {

        kafkaTemplate.send("driver-events", id, event);
        System.out.println("message sent by Kafka");
    }
}
