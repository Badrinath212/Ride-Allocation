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
public class DriverProfile {
    private String driverId;
    private Long totalRequests;
    private Long totalAccepted;
    private Long totalRejected;
    private Instant lastUpdatedAt;
    private Integer version;

    @DynamoDbPartitionKey
    public String getDriverId() {
        return driverId;
    }

    @DynamoDbVersionAttribute
    public Integer getVersion() {
        return version;
    }
}
