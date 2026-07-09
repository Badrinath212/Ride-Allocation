package com.badri.RideAllocation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyAnalyticsResponseDto {
    private String dateHour;
    private Long totalRequests;
    private Long totalAccepted;
    private Long totalRejected;
    private Long totalStarted;
    private Long totalCompleted;
    private Long totalCancelled;
    private Double totalRevenue;
}
