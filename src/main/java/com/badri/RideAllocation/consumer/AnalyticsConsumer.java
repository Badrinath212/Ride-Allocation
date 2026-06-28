package com.badri.RideAllocation.consumer;

import com.badri.RideAllocation.events.RideEvent;
import com.badri.RideAllocation.model.DailyRideAnalytics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class AnalyticsConsumer {

    private final ObjectMapper objectMapper;
    private final DynamoDbTable<DailyRideAnalytics> dailyRideAnalyticsTable;

    public AnalyticsConsumer(ObjectMapper objectMapper, DynamoDbTable<DailyRideAnalytics> dailyRideAnalyticsTable) {
        this.dailyRideAnalyticsTable = dailyRideAnalyticsTable;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "ride-events",
            groupId = "analytics-consumer"
    )
    public void consumeRideEvent(String event) {
        RideEvent rideEvent = objectMapper.readValue(event, RideEvent.class);


        Instant timestamp = Instant.parse(rideEvent.getTimestamp());
        System.out.println("Parsed instant: " + timestamp);

        ZoneId zoneId = ZoneId.of("Asia/Kolkata");
        ZonedDateTime zonedDateTime = timestamp.atZone(zoneId);

        String date = zonedDateTime.toLocalDate().toString();  //yyyy-MM-dd
        System.out.println("Extracted date: " + date);

        // fetch the rideEvent
        DailyRideAnalytics dateObj = dailyRideAnalyticsTable.getItem(
                Key.builder().partitionValue(date).build()
        );

        if(dateObj == null) {
            dateObj = DailyRideAnalytics.builder()
                    .data(date)
                    .build();
        }

        // Need to research about optimistic locking vs pessimistic locking

        switch (rideEvent.getEventType()) {
            case REQUESTED -> {
                dateObj.setTotalAccepted();
            }
            case ACCEPTED -> {

            }
            case CANCELLED -> {

            }
            case STARTED -> {

            }
            case REJECTED -> {

            }
            case COMPLETED -> {

            }
            case null, default -> {
                System.out.println("Event type is required ");
            }
        }
    }
}
