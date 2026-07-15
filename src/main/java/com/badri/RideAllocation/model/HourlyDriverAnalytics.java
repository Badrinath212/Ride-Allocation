package com.badri.RideAllocation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class HourlyDriverAnalytics {
    private String driverId;
    private String dateHour;
    private Long totalRequests;
    private Long totalAccepted;
    private Long totalRejected;
    private Long totalStarted;
    private Long totalCompleted;
    private Long totalCancelled;
    private Double totalRevenue;

    @DynamoDbPartitionKey
    public String getDriverId() {
        return driverId;
    }

    @DynamoDbSortKey
    public String getDateHour() {
        return dateHour;
    }
}
