package com.badri.RideAllocation.service;

import com.badri.RideAllocation.dto.BookRideResponseDto;
import com.badri.RideAllocation.dto.EstFareResponseDto;
import com.badri.RideAllocation.model.Ride;
import org.springframework.core.ParameterizedTypeReference;
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

    public RideService(WebClient webClient, DynamoDbEnhancedClient ddcEnhanced,
                       DynamoDbTable<Ride> rideTable, SqsClient sqsClient,
                       ObjectMapper objectMapper, DriverService driverService) {
        this.webClient = webClient;
        this.ddcEnhanced = ddcEnhanced;
        this.rideTable = rideTable;
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.driverService = driverService;
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

            String queueUrl = "http://sqs.ap-south-1.localhost.localstack.cloud:4566/000000000000/ride-queue";
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

    public String rideComplete(String rideId, String finalLat, String finalLng, String timeStamp) {
        try {
            // mark the ride as completed
            Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());
            rideItem.setStatus("COMPLETED");
            rideTable.updateItem(rideItem);

            // need to add the driver to active_drivers
            driverService.addDriverToActiveDrivers(rideItem.getDriverId(), finalLng, finalLat, "active_drivers");

            return "Ride Completed";

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return "Something went wrong";
    }

    public String rideArrived(String rideId, String driverId, String timeStamp) {
        try {
            Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());

            if(!driverId.equals(rideItem.getDriverId())) {
                return "Driver do not have access to this ride";
            }

            rideItem.setStatus("DRIVER_ARRIVED");
            rideTable.updateItem(rideItem);

            return "Driver arrived!";
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return "Something went wrong";
    }

    public String rideStart(String rideId, String driverId, String timeStamp) {
        try {
            Ride rideItem = rideTable.getItem(Key.builder().partitionValue(rideId).build());

            if(!driverId.equals(rideItem.getDriverId())) {
                return "Driver do not have access to this ride";
            }

            rideItem.setStatus("RIDE_STARTED");
            rideTable.updateItem(rideItem);

            return "Ride started. Happy journey!";

        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        return "Something went wrong";
    }
}
