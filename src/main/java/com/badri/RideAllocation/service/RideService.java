package com.badri.RideAllocation.service;

import com.badri.RideAllocation.dto.BookRideResponseDto;
import com.badri.RideAllocation.dto.EstFareResponseDto;
import com.badri.RideAllocation.model.Ride;
import com.badri.RideAllocation.vo.RideQueueEvent;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RideService {

    private final WebClient webClient;
    private final DynamoDbEnhancedClient ddcEnhanced;
    private final DynamoDbTable<Ride> rideTable;
    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final DriverService driverService;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final String queueUrl = "http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/ride-queue";

    public RideService(WebClient webClient, DynamoDbEnhancedClient ddcEnhanced,
                       DynamoDbTable<Ride> rideTable, SqsClient sqsClient,
                       ObjectMapper objectMapper, DriverService driverService,
                       SimpMessagingTemplate messagingTemplate, StringRedisTemplate redisTemplate) {
        this.webClient = webClient;
        this.ddcEnhanced = ddcEnhanced;
        this.rideTable = rideTable;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.driverService = driverService;
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
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

            BookRideResponseDto responseDto = new BookRideResponseDto(rideId, estimatedFare, status);

            return responseDto;
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String rideComplete(String rideId, String finalLat, String finalLng) {
        try {
            // mark the ride as completed
            Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());
            rideItem.setStatus("COMPLETED");
            rideItem.setRideCompletedAt(Instant.now());
            rideTable.updateItem(rideItem);

            // need to add the driver to active_drivers
            driverService.addDriverToActiveDrivers(rideItem.getDriverId(), finalLng, finalLat, "active_drivers");

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
                messagingTemplate.convertAndSend(
                        "/topic/driver/" + driverId,
                        "Ride is cancelled by rider"
                );

            }

            return "Ride is cancelled";

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public String rideCancelByDriver(String rideId, String driverId) {
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
            messagingTemplate.convertAndSend(
                    "/topic/rider/" + rideItem.getUserId(),
                    "Ride cancelled by driver."
            );

            // remove the ride lock
            redisTemplate.delete("ride:" + rideId + ":lock");

            //add the driver to active drivers
            String lng = "12.11";
            String lat = "74.11";
            driverService.addDriverToActiveDrivers(driverId, lng, lat, "active_drivers");

            // need add it back to ride-queue
            RideQueueEvent event = new RideQueueEvent(rideItem.getRideId(), "REQUESTED", rideItem.getPickupLat(), rideItem.getPickupLng(), String.valueOf(rideItem.getEstimatedFare()));

            String jsonEvent = objectMapper.writeValueAsString(event);
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                                                                .queueUrl(queueUrl)
                                                                .messageBody(jsonEvent)
                                                                .build();
            sqsClient.sendMessage(sendMessageRequest);

            System.out.println("Message sent back to ride queue");

            return "Ride cancelled successfully!";
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return "Something went wrong";
    }
}
