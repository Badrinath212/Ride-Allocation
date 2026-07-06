package com.badri.RideAllocation.consumer;

import com.badri.RideAllocation.events.RideEvent;
import com.badri.RideAllocation.model.DailyRideAnalytics;
import com.badri.RideAllocation.model.HourlyRideAnalytics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class AnalyticsConsumer {

    private final ObjectMapper objectMapper;
    private final DynamoDbClient dynamoDbClient;

    public AnalyticsConsumer(ObjectMapper objectMapper,
                             DynamoDbClient dynamoDbClient) {
        this.objectMapper = objectMapper;
        this.dynamoDbClient = dynamoDbClient;
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
        String hour = String.valueOf(zonedDateTime.getHour());
        System.out.println("Extracted hour: " + hour);

        String dateHour = date + "#" + hour;

        switch (rideEvent.getEventType()) {
            case REQUESTED -> {

                // update the totalRequests of daily analytics
                UpdateItemRequest updateDailyItemRequest = UpdateItemRequest.builder()
                        .tableName("daily-analytics")
                        .key(Map.of("date", AttributeValue.fromS(date)))
                        .updateExpression("ADD totalRequests :inc")
                        .expressionAttributeValues(Map.of(":inc", AttributeValue.fromN("1")))
                        .build();
                dynamoDbClient.updateItem(updateDailyItemRequest);
                System.out.println("Total requests are updated in daily analytics");

                // update the totalRequests of hourly analytics
                UpdateItemRequest updateHourlyItemRequest = UpdateItemRequest.builder()
                        .tableName("hourly-analytics")
                        .key(Map.of("dateHour", AttributeValue.fromS(dateHour)))
                        .updateExpression("ADD totalRequests :inc")
                        .expressionAttributeValues(Map.of(":inc", AttributeValue.fromN("1")))
                        .build();
                dynamoDbClient.updateItem(updateHourlyItemRequest);
                System.out.println("Total requests are updated in hourly analytics");


            }
            case ACCEPTED -> {

                UpdateItemRequest updateDailyItemRequest = UpdateItemRequest.builder()
                        .tableName("daily-analytics")
                        .key(Map.of("date", AttributeValue.fromS(date)))
                        .updateExpression("ADD totalAccepted :inc")
                        .expressionAttributeValues(Map.of(":inc", AttributeValue.fromN("1")))
                        .build();
                dynamoDbClient.updateItem(updateDailyItemRequest);
                System.out.println("Total accepted requests are updated in daily analytics");

                UpdateItemRequest updateHourlyItemRequest = UpdateItemRequest.builder()
                        .tableName("hourly-analytics")
                        .key(Map.of("dateHour", AttributeValue.fromS(dateHour)))
                        .updateExpression("ADD totalAccepted :inc")
                        .expressionAttributeValues(Map.of(":inc", AttributeValue.fromN("1")))
                        .build();
                dynamoDbClient.updateItem(updateHourlyItemRequest);
                System.out.println("Total accepted requests are updated in hourly analytics");
            }
            case CANCELLED -> {
                UpdateItemRequest updateDailyItemRequest = UpdateItemRequest.builder()
                        .tableName("daily-analytics")
                        .key(Map.of("date", AttributeValue.fromS(date)))
                        .updateExpression("ADD totalCancelled :inc")
                        .expressionAttributeValues(Map.of(":inc", AttributeValue.fromN("1")))
                        .build();
                dynamoDbClient.updateItem(updateDailyItemRequest);
                System.out.println("Total cancelled requests are updated in daily analytics");

                UpdateItemRequest updateHourlyItemRequest = UpdateItemRequest.builder()
                        .tableName("hourly-analytics")
                        .key(Map.of("dateHour", AttributeValue.fromS(dateHour)))
                        .updateExpression("ADD totalCancelled :inc")
                        .expressionAttributeValues(Map.of(":inc", AttributeValue.fromN("1")))
                        .build();
                dynamoDbClient.updateItem(updateHourlyItemRequest);
                System.out.println("Total Cancelled requests are updated in hourly analytics");
            }
            case STARTED -> {
                UpdateItemRequest updateDailyItemRequest = UpdateItemRequest.builder()
                        .tableName("daily-analytics")
                        .key(Map.of("date", AttributeValue.fromS(date)))
                        .updateExpression("ADD totalStarted :inc")
                        .expressionAttributeValues(Map.of(":inc", AttributeValue.fromN("1")))
                        .build();
                dynamoDbClient.updateItem(updateDailyItemRequest);
                System.out.println("Total started requests are updated in daily analytics");

                UpdateItemRequest updateHourlyItemRequest = UpdateItemRequest.builder()
                        .tableName("hourly-analytics")
                        .key(Map.of("dateHour", AttributeValue.fromS(dateHour)))
                        .updateExpression("ADD totalStarted :inc")
                        .expressionAttributeValues(Map.of(":inc", AttributeValue.fromN("1")))
                        .build();
                dynamoDbClient.updateItem(updateHourlyItemRequest);
                System.out.println("Total started requests are updated in hourly analytics");
            }
            case REJECTED -> {
                UpdateItemRequest updateDailyItemRequest = UpdateItemRequest.builder()
                        .tableName("daily-analytics")
                        .key(Map.of("date", AttributeValue.fromS(date)))
                        .updateExpression("ADD totalRejected :inc")
                        .expressionAttributeValues(Map.of(":inc", AttributeValue.fromN("1")))
                        .build();
                dynamoDbClient.updateItem(updateDailyItemRequest);
                System.out.println("Total rejected requests are updated in daily analytics");

                UpdateItemRequest updateHourlyItemRequest = UpdateItemRequest.builder()
                        .tableName("hourly-analytics")
                        .key(Map.of("dateHour", AttributeValue.fromS(dateHour)))
                        .updateExpression("ADD totalRejected :inc")
                        .expressionAttributeValues(Map.of(":inc", AttributeValue.fromN("1")))
                        .build();
                dynamoDbClient.updateItem(updateHourlyItemRequest);
                System.out.println("Total rejected requests are updated in hourly analytics");
            }
            case COMPLETED -> {

                Double totalFare = rideEvent.getTotalFare();
                if(totalFare == null) {
                    System.out.println("Total fare for this ride is null");
                }

                UpdateItemRequest updateDailyItemRequest = UpdateItemRequest.builder()
                        .tableName("daily-analytics")
                        .key(Map.of("date", AttributeValue.fromS(date)))
                        .updateExpression("ADD totalCompleted :inc, totalRevenue :fare")
                        .expressionAttributeValues(Map.of(
                                ":inc", AttributeValue.fromN("1"),
                                ":fare", AttributeValue.fromN(String.valueOf(rideEvent.getTotalFare()))
                        ))
                        .build();
                dynamoDbClient.updateItem(updateDailyItemRequest);
                System.out.println("Total completed requests are updated in daily analytics");

                UpdateItemRequest updateHourlyItemRequest = UpdateItemRequest.builder()
                        .tableName("hourly-analytics")
                        .key(Map.of("dateHour", AttributeValue.fromS(dateHour)))
                        .updateExpression("ADD totalCompleted :inc, totalRevenue :fare")
                        .expressionAttributeValues(Map.of(
                                ":inc", AttributeValue.fromN("1"),
                                ":fare", AttributeValue.fromN(String.valueOf(rideEvent.getTotalFare()))
                        ))
                        .build();
                dynamoDbClient.updateItem(updateHourlyItemRequest);
                System.out.println("Total completed requests are updated in hourly analytics");
            }
            case null, default -> {
                System.out.println("Event type is required ");
            }
        }
    }
}
