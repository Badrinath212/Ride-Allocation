package com.badri.RideAllocation.service;

import com.badri.RideAllocation.dto.BookRideResponseDto;
import com.badri.RideAllocation.dto.EstFareResponseDto;
import com.badri.RideAllocation.enums.RideEventType;
import com.badri.RideAllocation.events.DriverRideResponseEvent;
import com.badri.RideAllocation.events.RideEvent;
import com.badri.RideAllocation.model.DriverProfile;
import com.badri.RideAllocation.model.DriverRejectionEvents;
import com.badri.RideAllocation.model.Ride;
import com.badri.RideAllocation.producer.DriverEventProducer;
import com.badri.RideAllocation.producer.RideEventProducer;
import com.badri.RideAllocation.utilities.Utility;
import com.badri.RideAllocation.events.RideQueueEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RideService {

    private final WebClient webClient;
    private final DynamoDbTable<Ride> rideTable;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final DriverService driverService;
    private final StringRedisTemplate redisTemplate;
    private final String queueUrl = "http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/ride-queue";
    private final NotificationService notificationService;
    private final DynamoDbTable<DriverProfile> driverProfileTable;
    private final DynamoDbTable<DriverRejectionEvents> driverRejectionEventsTable;
    private final DriverEventProducer driverEventProducer;
    private final RideEventProducer rideEventProducer;

    public RideService(WebClient webClient, DynamoDbTable<Ride> rideTable, SqsClient sqsClient,
                       ObjectMapper objectMapper, DriverService driverService,
                       StringRedisTemplate redisTemplate, NotificationService notificationService,
                       DynamoDbTable<DriverProfile> driverProfileTable,
                       DynamoDbTable<DriverRejectionEvents> driverRejectionEventsTable,
                       DriverEventProducer driverEventProducer, RideEventProducer rideEventProducer) {
        this.webClient = webClient;
        this.rideTable = rideTable;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.driverService = driverService;
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
        this.driverProfileTable = driverProfileTable;
        this.driverRejectionEventsTable = driverRejectionEventsTable;
        this.driverEventProducer = driverEventProducer;
        this.rideEventProducer = rideEventProducer;
    }

    public EstFareResponseDto getEstFare(String pickupLat,String pickupLng, String dropLat, String dropLng, String profile) {
        try {
            String uriTemplate = "/route?point={p1lat},{p1lng}&point={p2lat},{p2lng}&profile={prof}";

            Map<String, Object> data = webClient
                    .get()
                    .uri(uriTemplate, pickupLat, pickupLng, dropLat, dropLng, profile)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            System.out.println(data);
            List<Map<String,Object>> paths = (List<Map<String,Object>>) data.get("paths");

            Map<String,Object> route = paths.get(0);

            double distanceKM = Double.parseDouble(route.get("distance").toString()) / 1000;
            double timeMins = Double.parseDouble((route.get("time").toString())) / (1000*60);

            System.out.println("Distance in Km: " + distanceKM);
            System.out.println("Time in Mins: " + timeMins);

            int baseFare = 50;
            double estFare = baseFare + (10*distanceKM) + (2*timeMins);

            return new EstFareResponseDto(pickupLat, pickupLng, dropLat, dropLng, estFare);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return new EstFareResponseDto();
    }

    public BookRideResponseDto bookRide(String userId, String pickupLat, String pickupLng, String dropLat, String dropLng, String estimatedFare) {
        try {
            //  need to generate the uuid for ride id
            UUID uuid = UUID.randomUUID();
            String rideId = uuid.toString();

            String status = "REQUESTED";
            //  save to db

            Ride ride = Ride.builder()
                    .rideId(rideId)
                    .userId(userId)
                    .pickupLat(pickupLat)
                    .pickupLng(pickupLng)
                    .dropLat(dropLat)
                    .dropLng(dropLng)
                    .estimatedFare(Double.valueOf(estimatedFare))
                    .status(status)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .retryCount(0)
                    .build();

            rideTable.putItem(ride);

            //  send this into amazon sqs queue

            Map<String, String> map = new HashMap<>();

            map.put("rideId", rideId);
            map.put("status", status);
            map.put("pickupLat", pickupLat);
            map.put("pickupLng", pickupLng);
            map.put("estimatedFare", estimatedFare);

            String jsonBody = objectMapper.writeValueAsString(map);

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .messageBody(jsonBody)
                    .queueUrl(queueUrl)
                    .build();

            sqsClient.sendMessage(sendMessageRequest);

            System.out.println("message is sent to sqs");

            // send the ride request event to Kafka
            RideEvent rideEvent = RideEvent.builder()
                            .eventType(RideEventType.REQUESTED)
                            .rideId(rideId)
                            .timestamp(Instant.now().toString())
                            .build();
            String rideEventJson = objectMapper.writeValueAsString(rideEvent);
            rideEventProducer.publishRideEvent(rideEventJson, rideId);
            System.out.println("Ride Requested Event to Kafka");

            return new BookRideResponseDto(rideId, estimatedFare, status);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String rideComplete(String rideId, String finalLat, String finalLng) {
        try {
            // mark the ride as completed
            Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());
            System.out.println("Status: " + rideItem.getStatus());
            if(!rideItem.getStatus().equals("RIDE_STARTED") || rideItem.getStatus().equals("COMPLETED")){
                return "Invalid request";
            }
            rideItem.setStatus("COMPLETED");
            rideItem.setRideCompletedAt(Instant.now());
            rideItem.setTotalFare(rideItem.getEstimatedFare());
            rideTable.updateItem(rideItem);

            // need to add the driver to active_drivers
            driverService.addDriverToActiveDrivers(rideItem.getDriverId(), finalLng, finalLat, "active_drivers");

            // send ride completed event to Kafka
            RideEvent rideEvent = RideEvent.builder()
                    .eventType(RideEventType.COMPLETED)
                    .rideId(rideId)
                    .driverId(rideItem.getDriverId())
                    .timestamp(Instant.now().toString())
                    .totalFare(rideItem.getEstimatedFare())
                    .build();

            String rideEventJson = objectMapper.writeValueAsString(rideEvent);
            rideEventProducer.publishRideEvent(rideEventJson, rideId);
            System.out.println("Ride completed Event sent to Kafka");

            // update driver profile
            DriverProfile driverProfile = driverProfileTable.getItem(
                    Key.builder().partitionValue(rideItem.getDriverId()).build()
            );

            if(driverProfile != null) {
                driverProfile.setTotalCompleted(driverProfile.getTotalCompleted() + 1);
                System.out.println("Driver total completed rides updated");

                // send the Kafka request for driver analytics
                RideEvent rideCompletedEvent = RideEvent.builder()
                        .rideId(rideId)
                        .eventType(RideEventType.COMPLETED)
                        .driverId(rideItem.getDriverId())
                        .timestamp(Instant.now().toString())
                        .totalFare(rideItem.getTotalFare())
                        .build();
                String rideCompletedJson = objectMapper.writeValueAsString(rideCompletedEvent);
                System.out.println("RideCompletedJson : " + rideCompletedJson);

                driverEventProducer.publishDriverEvent(rideCompletedJson, rideItem.getDriverId());
                System.out.printf("Ride complete event sent to Kafka for driver analytics: %s", rideItem.getDriverId());
            }

            return "Ride Completed";

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return "Something went wrong";
    }

    public String rideArrived(String rideId, String driverId) {
        try {
            Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());

            if(!driverId.equals(rideItem.getDriverId())) {
                return "Driver do not have access to this ride";
            }

            rideItem.setStatus("DRIVER_ARRIVED");
            rideItem.setDriverArrivedAt(Instant.now());
            rideTable.updateItem(rideItem);

            return "Driver arrived!";
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return "Something went wrong";
    }

    public String rideStart(String rideId, String driverId) {
        try {
            Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());

            if(!driverId.equals(rideItem.getDriverId())) {
                return "Driver do not have access to this ride";
            }

            rideItem.setStatus("RIDE_STARTED");
            rideItem.setRideStartedAt(Instant.now());
            rideTable.updateItem(rideItem);

            // send the ride started event to Kafka
            RideEvent rideEvent = RideEvent.builder()
                    .eventType(RideEventType.STARTED)
                    .rideId(rideId)
                    .driverId(driverId)
                    .timestamp(Instant.now().toString())
                    .build();

            String rideEventJson = objectMapper.writeValueAsString(rideEvent);
            rideEventProducer.publishRideEvent(rideEventJson, rideId);

            // send the started event to Kafka
            RideEvent rideStartedEvent = RideEvent.builder()
                    .rideId(rideId)
                    .driverId(driverId)
                    .eventType(RideEventType.STARTED)
                    .timestamp(Instant.now().toString())
                    .build();

            String rideStartedJson = objectMapper.writeValueAsString(rideStartedEvent);
            driverEventProducer.publishDriverEvent(rideStartedJson, driverId);

            System.out.println("Ride Cancelled event sent to Kafka driver-events");
            return "Ride started. Happy journey!";

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return "Something went wrong";
    }

    public String rideCancelByRider(String rideId, String driverId) {
        try {
            Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());

            if("COMPLETED".equals(rideItem.getStatus())) {
                return "Ride is already completed";
            }

            if("RIDE_STARTED".equals(rideItem.getStatus())) {
                return "Ride is already started. Not able to cancel";
            }

            // change the ride status
            rideItem.setStatus("RIDER_CANCELLED");
            rideTable.updateItem(rideItem);

            if(rideItem.getDriverId() != null) {
                // if present
                // remove the ride from ride-queue
                // remove the ride from dispatch queue
                // remove the ride from ride response queue(driver response)
                // above cases handled by the polling service

                // for fetching the driver lng & lat it calls the api from driver service to the driver device
                String lng = "12.77";
                String lat = "77.88";

                // validate the driver
                if(!rideItem.getDriverId().equals(driverId)) {
                    return "Invalid Driver";
                }

                // add the driver back to active users
                driverService.addDriverToActiveDrivers(driverId, lng, lat, "active_drivers");

                // ride lock automatically remove after five minutes
                redisTemplate.delete("ride:" + rideId + ":lock");

                // notify the driver as ride cancelled
                String msg = "Ride is cancelled by rider";
                notificationService.notifyDriver(driverId, msg);

            }

            RideEvent rideEvent = RideEvent.builder()
                    .rideId(rideId)
                    .eventType(RideEventType.CANCELLED)
                    .driverId(driverId)
                    .cancelledBy("rider")
                    .timestamp(Instant.now().toString())
                    .build();

            String rideEventJson = objectMapper.writeValueAsString(rideEvent);
            rideEventProducer.publishRideEvent(rideEventJson, rideId);
            System.out.println("Rider Ride Rejection sent to Kafka");

            return "Ride is cancelled";

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String rideCancelByDriver(String rideId, String driverId, String cancelReason) {
        try {
            Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());

            // ride check
            if(rideItem == null) return "Ride not found";

            // verify the driver
            if(!driverId.equals(rideItem.getDriverId())) {
                return "Invalid Driver";
            }

            if("RIDE_STARTED".equals(rideItem.getStatus())) {
                return "Ride is already started. Not able to cancel";
            }

            if("COMPLETED".equals(rideItem.getStatus())) {
                return "Ride is already completed";
            }

            // update the ride as driver cancelled
            rideItem.setStatus("REQUESTED");
            rideItem.setDriverId(null);
            rideTable.updateItem(rideItem);

            //intimate the rider
            String userId = rideItem.getUserId();
            String msg = "Ride cancelled by driver.";
            notificationService.notifyRider(userId, msg);

            // remove the ride lock
            redisTemplate.delete("ride:" + rideId + ":lock");

            //add the driver to active drivers
            String lng = "12.11";
            String lat = "74.11";
            driverService.addDriverToActiveDrivers(driverId, lng, lat, "active_drivers");

            // need to exclude from the ride
            String redisKey = "ride:" + rideId + ":rejectedDrivers";
            redisTemplate.opsForSet().add(redisKey, driverId);
            redisTemplate.expire(redisKey, Duration.ofHours(1));

            // need add it back to ride-queue
            RideQueueEvent event = new RideQueueEvent(rideItem.getRideId(), "REQUESTED", rideItem.getPickupLat(), rideItem.getPickupLng(), String.valueOf(rideItem.getEstimatedFare()));

            String jsonEvent = objectMapper.writeValueAsString(event);
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                                                                .queueUrl(queueUrl)
                                                                .messageBody(jsonEvent)
                                                                .build();
            sqsClient.sendMessage(sendMessageRequest);

            System.out.println("Message sent back to ride queue");

            // update driver totalAccepted & rejected count
            DriverProfile driverProfile = driverProfileTable.getItem(Key.builder().partitionValue(driverId).build());

            if(driverProfile == null) {
                System.out.println("Driver profile not available");
            } else {
                driverProfile.setTotalRejected(driverProfile.getTotalRejected() + 1);
                driverProfile.setTotalAccepted(Math.max(0, driverProfile.getTotalAccepted() - 1));

                driverProfileTable.putItem(driverProfile);
                System.out.println("Driver profile is updated");

                // send the event to Kafka for metrics
                DriverRideResponseEvent driverRideResponseEvent = DriverRideResponseEvent.builder()
                        .driverId(driverId)
                        .build();

                String json = objectMapper.writeValueAsString(driverRideResponseEvent);

                driverEventProducer.publishDriverRideResponseEvent(json, driverId);

                // send the Cancelled event to kafka
                RideEvent rideCancelledEvent = RideEvent.builder()
                        .rideId(rideId)
                        .driverId(driverId)
                        .eventType(RideEventType.CANCELLED)
                        .timestamp(Instant.now().toString())
                        .build();

                String rideCancelledJson = objectMapper.writeValueAsString(rideCancelledEvent);
                driverEventProducer.publishDriverEvent(rideCancelledJson, driverId);

                System.out.println("Ride Cancelled event sent to Kafka driver-events");

            }

            // update driver rejection events to db table

            // pickup location
            double pickupLat = Double.parseDouble(rideItem.getPickupLat());
            double pickupLng = Double.parseDouble(rideItem.getPickupLng());

            // current driver location
            String redisKeyForDriverLocation = "driver:presence:" + driverId;
            System.out.println("redisKey for presence: " + redisKeyForDriverLocation);
            Object driverLng = redisTemplate.opsForHash().get(redisKeyForDriverLocation, "lng");
            Object driverLat = redisTemplate.opsForHash().get(redisKeyForDriverLocation, "lat");

            if(driverLat == null || driverLng == null) {
                System.out.println("Driver location is not available: " + driverId);
            } else {

                double driverLatitude = Double.parseDouble(driverLat.toString());
                double driverLongitude = Double.parseDouble(driverLng.toString());

                double distance = Utility.calculateDistance(
                        pickupLat,
                        pickupLng,
                        driverLatitude,
                        driverLongitude
                );
                // Add the driver rejection ride to db table
                DriverRejectionEvents driverRejectionEvents = DriverRejectionEvents.builder()
                        .driverId(driverId)
                        .rideId(rideId)
                        .timestamp(Instant.now())
                        .cancelReason(cancelReason)
                        .estimatedFare(rideItem.getEstimatedFare())
                        .cancelDistance(distance)
                        .build();

                driverRejectionEventsTable.putItem(driverRejectionEvents);

                System.out.println("Driver Rejection Event is added");
            }

            // send the event to Kafka
            RideEvent rideEvent = RideEvent.builder()
                    .rideId(rideId)
                    .eventType(RideEventType.CANCELLED)
                    .driverId(driverId)
                    .cancelledBy("driver")
                    .timestamp(Instant.now().toString())
                    .build();

            String rideEventJson = objectMapper.writeValueAsString(rideEvent);
            rideEventProducer.publishRideEvent(rideEventJson, rideId);
            System.out.println("Driver Rejection sent to Kafka");

            return "Ride cancelled successfully!";
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return "Something went wrong";
    }
}
