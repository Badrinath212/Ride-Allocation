package com.badri.RideAllocation.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.extensions.annotations.DynamoDbVersionAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;

@DynamoDbBean
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyRideAnalytics {
    private String data;
    private Long totalRequests;
    private Long totalAccepted;
    private Long totalRejected;
    private Long totalStarted;
    private Long totalCompleted;
    private Long totalCancelled;
    private Double totalRevenue;
    private Long version;

    @DynamoDbPartitionKey
    public String getDate() {
        return data;
    }

    @DynamoDbVersionAttribute
    public Long getVersion() {
        return version;
    }
}
