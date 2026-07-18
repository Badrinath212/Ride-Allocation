package com.badri.RideAllocation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverAnalyticsDto {
    private String driverId;
    private Long totalRequests;
    private Long totalAccepted;
    private Long totalRejected;
    private Long totalCompleted;
    private String name;
    private Double rating;
    private Double acceptanceRate;

    private String date;
    private Long totalStarted;
    private Long totalCancelled;
    private Double totalRevenue;
    private String dateHour;
}
