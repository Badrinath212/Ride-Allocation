package com.badri.RideAllocation.consumer;

import com.badri.RideAllocation.events.DriverRideResponseEvent;
import com.badri.RideAllocation.model.DriverProfile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import tools.jackson.databind.ObjectMapper;

@Component
public class MetricsConsumer {
    private final ObjectMapper objectMapper;
    private final DynamoDbTable<DriverProfile> driverProfileTable;

    public MetricsConsumer(ObjectMapper objectMapper, DynamoDbTable<DriverProfile> driverProfileTable) {
        this.objectMapper = objectMapper;
        this.driverProfileTable = driverProfileTable;
    }

    @KafkaListener(
            topics = "driver-events-metrics",
            groupId = "metrics-group"
    )
    public void consumeDriverRideResponseEvent(String event) {
        System.out.println("Driver Event from kafka: " + event);

        // convert JSON to object
        DriverRideResponseEvent driverAcceptedEvent = objectMapper.readValue(event, DriverRideResponseEvent.class);
        String driverId = driverAcceptedEvent.getDriverId();
        if(driverId == null) {
            System.out.println("Invalid Event");
            return;
        }

        DriverProfile driverProfile = driverProfileTable.getItem(
                Key.builder().partitionValue(driverId).build()
        );

        if(driverProfile == null) {
            System.out.println("Driver profile is not available");
            return;
        }

        Long totalAccepted = driverProfile.getTotalAccepted();
        Long totalRequests = driverProfile.getTotalRequests();

        double acceptanceRate = 0.0;
        if(totalRequests > 0) {
            acceptanceRate = (double) totalAccepted/totalRequests;
        }

        driverProfile.setAcceptanceRate(acceptanceRate);
        driverProfileTable.updateItem(driverProfile);

        System.out.println("Driver Acceptance Rate is updated for : " + driverId);;

    }

    @KafkaListener(
            topics = "test-events",
            groupId = "test-group"
    )
    public void consumeTestEvent(String event) {
        System.out.println("Driver Test Event: " + event);
    }
}
