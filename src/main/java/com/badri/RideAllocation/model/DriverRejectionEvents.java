package com.badri.RideAllocation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class DriverRejectionEvents {
    private String driverId;
    private Instant timestamp;
    private String rideId;
    private Integer pickupDistance;
    private Double estimatedFare;
    private String reason;
    private Integer version;

    @DynamoDbPartitionKey
    public String getDriverId() {
        return driverId;
    }

    @DynamoDbSortKey
    public Instant getTimestamp() {
        return timestamp;
    }

    @DynamoDbVersionAttribute
    public Integer getVersion() {
        return version;
    }

}
