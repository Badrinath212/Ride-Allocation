package com.badri.RideAllocation.service;

import com.badri.RideAllocation.dto.RideResponseDto;
import com.badri.RideAllocation.model.DriverProfile;
import com.badri.RideAllocation.model.Ride;
import com.badri.RideAllocation.vo.DispatchRetryEvent;
import com.badri.RideAllocation.vo.RideQueueEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SQSPollingService {
    private final SqsClient sqsClient;
    private final DynamoDbTable<Ride> rideTable;
    private final String rideQueueUrl = "http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/ride-queue";
    private final DriverService driverService;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final String rideResponseQueueUrl = "http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/ride-process";
    private final String dispatchSchedulingQueueUrl = "http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/dispatch-scheduling-queue";
    private final StringRedisTemplate redisTemplate;
    private final PresenceService presenceService;
    private final NotificationService notificationService;
    private final DynamoDbTable<DriverProfile> driverProfileTable;

    public SQSPollingService(SqsClient sqsClient,
                             DynamoDbTable<Ride> rideTable, DriverService driverService,
                             ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate,
                             StringRedisTemplate redisTemplate, PresenceService presenceService,
                             NotificationService notificationService,
                             DynamoDbTable<DriverProfile> driverProfileTable) {
        System.out.println("constructor called");
        this.sqsClient = sqsClient;
        this.rideTable = rideTable;
        this.driverService = driverService;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.presenceService = presenceService;
        this.notificationService = notificationService;
        this.driverProfileTable = driverProfileTable;
    }

    @PostConstruct
    private void startPolling() {
        new Thread(this::pollMessages).start();
        new Thread(this::pollRideResponses).start();
        new Thread(this::pollSchedulingQueue).start();
    }

    private void pollSchedulingQueue() {
        while(true) {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(dispatchSchedulingQueueUrl)
                    .waitTimeSeconds(5)
                    .maxNumberOfMessages(10)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

            if(messages.isEmpty()) {
                System.out.println("scheduling messsages are empty");
            }

            for(Message message: messages) {
                try {
                    DispatchRetryEvent dispatchRetryEvent = objectMapper.readValue(message.body(), DispatchRetryEvent.class);
                    String rideId = dispatchRetryEvent.getRideId();
                    int startIndex = dispatchRetryEvent.getStartIndex();

                    // check the rideId status
                    Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());


                    if(!"REQUESTED".equals(rideItem.getStatus())) {
                        System.out.println("ride is accepted by the driver");
                        deleteMessage(message.receiptHandle(), dispatchSchedulingQueueUrl);
                    } else {
                        String redisKey = "ride:" + rideId + ":candidates";
                        List<String> driverBatch = driverService.fetchCandidateDrivers(startIndex,startIndex+4, redisKey);
                        if(startIndex >= 50 || driverBatch.isEmpty()) {
                            deleteMessage(message.receiptHandle(), dispatchSchedulingQueueUrl);
                            String userId = rideItem.getUserId();
                            String msg = "No drivers are available for your ride";

                            notificationService.notifyRider(userId, msg);
                            System.out.println("No drivers are there for your ride");
                        } else {

                            System.out.println("Received scheduling event");
                            System.out.println("startIndex = " + startIndex);

                            System.out.println("driverBatch = " + driverBatch);

                            RideQueueEvent rideData = new RideQueueEvent(rideId, rideItem.getStatus(), rideItem.getPickupLat(), rideItem.getPickupLng(), String.valueOf(rideItem.getEstimatedFare()));

                            for(String driverId: driverBatch) {
                                if(presenceService.isOnline(driverId)) {
                                    notificationService.notifyRideRequestToDriverThroughWebSocket(driverId, rideData);
                                    System.out.println("Notification is sent to: " + driverId);

                                    // update the total requests count for the driver
                                    DriverProfile driverProfile = driverProfileTable.deleteItem(Key.builder().partitionValue(driverId).build());

                                    if(driverProfile == null) {
                                        System.out.println("Driver profile not available: " + driverId);
                                        continue;
                                    }

                                    driverProfile.setTotalRequests(driverProfileTable.getTotalRequests() + 1);
                                    driverProfileTable.putItem(driverProfile);

                                    System.out.println("TotalRequests count is updated for driver: " + driverId);
                                }
                            }

                            deleteMessage(message.receiptHandle(), dispatchSchedulingQueueUrl);

                            dispatchRetryEvent.setStartIndex(startIndex+5);

                            String jsonBody = objectMapper.writeValueAsString(dispatchRetryEvent);
                            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                                    .queueUrl(dispatchSchedulingQueueUrl)
                                    .delaySeconds(10)
                                    .messageBody(jsonBody)
                                    .build();

                            sqsClient.sendMessage(sendMessageRequest);

                            System.out.println("message is sent to dispatch queue");
                        }
                    }
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }

        }
    }

    private void pollRideResponses() {
        System.out.println("pollRideResponses method called");
        while(true) {
            System.out.println("inside the while loop of poll responses");
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(rideResponseQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(10)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

            System.out.println("ride process size " + messages.size());

            for(Message message: messages) {
                try {
                    System.out.println("message in processrideresponse");
                    System.out.println(message.body());
                    RideResponseDto dto = objectMapper.readValue(message.body(), RideResponseDto.class);

                    Ride ride = rideTable.getItem(Key.builder().partitionValue(dto.getRideId()).build());

                    processRideResponse(dto);

                    deleteMessage(message.receiptHandle(), rideResponseQueueUrl);
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private void pollMessages() {
        while(true) {
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(rideQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(10)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveMessageRequest).messages();

            for(Message message: messages) {
                try {
                    System.out.println("Message body: " + message.body());

//                    Map<String, String> rideData = objectMapper.readValue(message.body(),
//                            new TypeReference<Map<String, String>>(){});

                    RideQueueEvent rideQueueEvent = objectMapper.readValue(message.body(), RideQueueEvent.class);

                    Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideQueueEvent.getRideId()).build());

                    if(!"REQUESTED".equals(rideItem.getStatus())) {
                        deleteMessage(message.receiptHandle(), rideQueueUrl);
                        continue;
                    }

                    processRide(rideQueueEvent);

                    deleteMessage(message.receiptHandle(),rideQueueUrl);
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private void deleteMessage(String receiptHandle, String queueUrl) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build()
        );
    }

    private String processRide(RideQueueEvent rideDate) {

        try {

            String rideId = rideDate.getRideId();
            // check the idempotency
            Key key = Key.builder()
                    .partitionValue(rideId)
                    .build();
            Ride rideItem = rideTable.getItem(key);


            if (!"REQUESTED".equals(rideItem.getStatus())) {
                System.out.println("Driver is already assigned");
                return "Driver is already assigned";
            }

            // { rideId, pickUplat, pickupLng, estimatedFare }  redis member in active_drivers

            // call the driver service

            Double pickupLat = Double.parseDouble(rideDate.getPickupLat());
            Double pickupLng = Double.parseDouble(rideDate.getPickupLng());

            System.out.println("pickupLat :" + pickupLat);
            System.out.println("pickLng :" + pickupLng);


            List<GeoResult<RedisGeoCommands.GeoLocation<String>>> results = driverService.getNearestNDrivers(pickupLat, pickupLng);

            List<String> drivers = new ArrayList<>();

            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                String driverId = result.getContent().getName();
                drivers.add(driverId);
            }

            String redisKey = "ride:" + rideId + ":candidates";

            System.out.println("Drivers: " + drivers);

            driverService.storeCandidateDrivers(drivers, redisKey, rideId);

            // initial notify for 5 drivers
            List<String> driverBatch = driverService.fetchCandidateDrivers(0, 4, redisKey);

            for (String driverId : driverBatch) {
                if(presenceService.isOnline(driverId)) {
                    notificationService.notifyRideRequestToDriverThroughWebSocket(driverId, rideDate);
                    System.out.println("Request sent");

                    // update the total requests count for the driver
                    DriverProfile driverProfile = driverProfileTable.deleteItem(Key.builder().partitionValue(driverId).build());

                    if(driverProfile == null) {
                        System.out.println("Driver profile not available: " + driverId);
                        continue;
                    }

                    driverProfile.setTotalRequests(driverProfileTable.getTotalRequests() + 1);
                    driverProfileTable.putItem(driverProfile);

                    System.out.println("TotalRequests count is updated for driver: " + driverId);
                }
            }

            DispatchRetryEvent event = new DispatchRetryEvent(rideId, 5);

            String jsonBody = objectMapper.writeValueAsString(event);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(dispatchSchedulingQueueUrl)
                    .messageBody(jsonBody)
                    .delaySeconds(10)
                    .build();

            sqsClient.sendMessage(sendMessageRequest);

            System.out.println("initial dispatch event sent to queue");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "SQS Polling";
    }

    public void processRideResponse(RideResponseDto dto) {

        try {
            System.out.println("process Ride Response method called");
            String rideId = dto.getRideId();
            String driverId = dto.getDriverId();
            String status = dto.getStatus();

            System.out.println("process ride body: " + dto.toString());

            System.out.println(rideId);
            System.out.println(driverId);
            System.out.println(status);

            Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());

            System.out.println(rideItem.toString());

            // Driver rejects the ride & N drivers rejects need to query next N drivers

            System.out.println("driverId: " + driverId);

            if (status.equals("REJECTED")) {
                System.out.println("Ride is rejected by the driver");

                // update the driver requests

                return;
            }

            // Distributed lock to the ride if two drivers accept the same ride at same time first driver will win by using this lock

            System.out.println("Before redis lock operations");
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent("ride:" + rideId + ":lock", driverId, Duration.ofMinutes(5));
            System.out.println("After redis lock operations");

            if (Boolean.TRUE.equals(acquired)) {
//            rideItem.setStatus(status);
//            rideItem.setDriverId(driverId);
//            rideTable.updateItem(rideItem);

                // remove the driver from active drivers
                System.out.println("Before remove driver from active drivers");
                driverService.removeDriverFromActiveDrivers(driverId, "active_drivers");
                System.out.println("Before remove driver from active drivers");

                String userId = rideItem.getUserId();
                String msg = "Captain is on the way!";

                notificationService.notifyRider(userId, msg);
                // need to change the state from the accepted to the driver assigned

                rideItem.setStatus("DRIVER_ASSIGNED");
                rideItem.setRideAssignedAt(Instant.now());
                rideItem.setDriverId(driverId);
                rideTable.updateItem(rideItem);

                System.out.println("Ride is processed");
                System.out.println("Ride is assigned to driver: " + driverId);
            } else {
                System.out.println("ride is already assigned to other driver");
                System.out.println("/topic/driver/" + driverId);

                String msg = "Ride is already assigned to other driver";

                notificationService.notifyDriver(driverId, msg);

            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
