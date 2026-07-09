package com.badri.RideAllocation.model;

import lombok.*;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
@Builder
public class DriverProfile {
    private String driverId;
    private Long totalRequests;
    private Long totalAccepted;
    private Long totalRejected;
    private Long totalCompleted;
    private Instant lastUpdatedAt;
    private String vehicleType;
    private String name;
    private Integer rating;
    private Integer version;
    private Double acceptanceRate;

    @DynamoDbPartitionKey
    public String getDriverId() {
        return driverId;
    }

    @DynamoDbVersionAttribute
    public Integer getVersion() {
        return version;
    }
}
