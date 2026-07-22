package com.badri.RideAllocation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverAnalyticsDto {
    private String driverId;
    private Long totalRequests;
    private Long totalAccepted;
    private Long totalRejected;
    private Long totalCompleted;
    private String name;
    private Double rating;

    private String date;
    private Long totalStarted;
    private Long totalCancelled;
    private Double totalRevenue;
    private String dateHour;

    private Double acceptanceRate;
    private Double cancellationRate;
    private Double completionRate;
    private Double avgRevenuePerRide;
}
