package com.badri.RideAllocation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
@Builder
public class Ride {
    private String rideId;
    private String userId;
    private String pickupLng;
    private String pickupLat;
    private String dropLng;
    private String dropLat;
    private Double estimatedFare;
    private Double finalFare;
    private String driverId;
    private String status;
    private Integer version;
    private Integer retryCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant rideStartedAt;
    private Instant rideCompletedAt;
    private Instant rideAssignedAt;
    private Instant driverArrivedAt;

    @DynamoDbPartitionKey
    public String getRideId() {
        return rideId;
    }

    @DynamoDbVersionAttribute
    public Integer getVersion() {
        return version;
    }
}
