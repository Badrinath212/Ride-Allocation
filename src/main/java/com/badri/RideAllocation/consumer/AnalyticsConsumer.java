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
                    .date(date)
                    .build();
        }

        // Need to research about optimistic locking vs pessimistic locking

        switch (rideEvent.getEventType()) {
            case REQUESTED -> {
                Long totalRequests = dateObj.getTotalRequests();
                if(totalRequests == null) {
                    totalRequests = 0L;
                }
                dateObj.setTotalAccepted(totalRequests + 1);

                // save the obj in table
                dailyRideAnalyticsTable.putItem(dateObj);
                System.out.println("Total requests are updated in daily analytics");
            }
            case ACCEPTED -> {
                Long totalAccepted = dateObj.getTotalAccepted();
                if(totalAccepted == null) {
                    totalAccepted = 0L;
                }
                dateObj.setTotalAccepted(totalAccepted + 1);

                // save the obj in table
                dailyRideAnalyticsTable.putItem(dateObj);
                System.out.println("Total accepted requests are updated in daily analytics");
            }
            case CANCELLED -> {
                Long totalCancelled = dateObj.getTotalCancelled();
                if(totalCancelled == null) {
                    totalCancelled = 0L;
                }
                dateObj.setTotalCancelled(totalCancelled + 1);

                // save the obj in table
                dailyRideAnalyticsTable.putItem(dateObj);
                System.out.println("Total cancelled requests are updated in daily analytics");
            }
            case STARTED -> {
                Long totalStarted = dateObj.getTotalStarted();
                if(totalStarted == null) {
                    totalStarted = 0L;
                }
                dateObj.setTotalStarted(totalStarted + 1);

                // save the obj in table
                dailyRideAnalyticsTable.putItem(dateObj);
                System.out.println("Total started requests are updated in daily analytics");
            }
            case REJECTED -> {
                Long totalRejected = dateObj.getTotalRejected();
                if(totalRejected == null) {
                    totalRejected = 0L;
                }
                dateObj.setTotalRejected(totalRejected + 1);

                // save the obj in table
                dailyRideAnalyticsTable.putItem(dateObj);
                System.out.println("Total rejected requests are updated in daily analytics");
            }
            case COMPLETED -> {
                Long totalCompleted = dateObj.getTotalCompleted();
                Double totalRevenue = dateObj.getTotalRevenue();
                if(totalCompleted == null) {
                    totalCompleted = 0L;
                }
                if(totalRevenue == null) {
                    totalRevenue = 0.0;
                }
                dateObj.setTotalCompleted(totalCompleted + 1);
                dateObj.setTotalRevenue(totalRevenue + rideEvent.getTotalFare());

                // save the obj in table
                dailyRideAnalyticsTable.putItem(dateObj);
                System.out.println("Total completed requests are updated in daily analytics");
            }
            case null, default -> {
                System.out.println("Event type is required ");
            }
        }
    }
}
