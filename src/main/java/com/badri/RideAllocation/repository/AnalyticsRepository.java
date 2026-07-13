package com.badri.RideAllocation.repository;

import com.badri.RideAllocation.dto.DailyAnalyticsResponseDto;
import com.badri.RideAllocation.dto.DriverAnalyticsDto;
import com.badri.RideAllocation.dto.HourlyAnalyticsResponseDto;
import com.badri.RideAllocation.model.DailyRideAnalytics;
import com.badri.RideAllocation.model.DriverProfile;
import com.badri.RideAllocation.model.HourlyRideAnalytics;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Repository
public class AnalyticsRepository {

    private final DynamoDbTable<DailyRideAnalytics> dailyRideAnalyticsTable;
    private final DynamoDbTable<HourlyRideAnalytics> hourlyRideAnalyticsTable;
    private final DynamoDbTable<DriverProfile> driverProfileTable;

    public AnalyticsRepository(DynamoDbTable<DailyRideAnalytics> dailyRideAnalyticsTable,
                               DynamoDbTable<HourlyRideAnalytics> hourlyRideAnalyticsTable,
                               DynamoDbTable<DriverProfile> driverProfileTable) {
        this.dailyRideAnalyticsTable = dailyRideAnalyticsTable;
        this.hourlyRideAnalyticsTable = hourlyRideAnalyticsTable;
        this.driverProfileTable = driverProfileTable;
    }

    public DailyAnalyticsResponseDto getDailyAnalytics(String date) {

        DailyRideAnalytics dailyRideAnalytics = dailyRideAnalyticsTable.getItem(
                Key.builder().partitionValue(date).build()
        );

        if(dailyRideAnalytics == null) return null;
        return DailyAnalyticsResponseDto.builder()
                .date(dailyRideAnalytics.getDate())
                .totalAccepted(dailyRideAnalytics.getTotalAccepted())
                .totalCancelled(dailyRideAnalytics.getTotalCancelled())
                .totalCompleted(dailyRideAnalytics.getTotalCompleted())
                .totalRejected(dailyRideAnalytics.getTotalRejected())
                .totalRequests(dailyRideAnalytics.getTotalRequests())
                .totalStarted(dailyRideAnalytics.getTotalStarted())
                .totalRevenue(dailyRideAnalytics.getTotalRevenue())
                .build();
    }

    public HourlyAnalyticsResponseDto getHourlyAnalytics(String dateHour) {

        HourlyRideAnalytics hourlyRideAnalytics = hourlyRideAnalyticsTable.getItem(
                Key.builder().partitionValue(dateHour).build()
        );
        if(hourlyRideAnalytics == null) return null;
        return HourlyAnalyticsResponseDto.builder()
                .dateHour(hourlyRideAnalytics.getDateHour())
                .totalCompleted(hourlyRideAnalytics.getTotalCompleted())
                .totalAccepted(hourlyRideAnalytics.getTotalAccepted())
                .totalCancelled(hourlyRideAnalytics.getTotalCancelled())
                .totalRejected(hourlyRideAnalytics.getTotalRejected())
                .totalRequests(hourlyRideAnalytics.getTotalRequests())
                .totalRevenue(hourlyRideAnalytics.getTotalRevenue())
                .totalStarted(hourlyRideAnalytics.getTotalStarted())
                .build();
    }

    public DriverAnalyticsDto getDriverAnalytics(String driverId) {

        DriverProfile driverProfile = driverProfileTable.getItem(
                Key.builder().partitionValue(driverId).build()
        );


        return DriverAnalyticsDto.builder()
                .driverId(driverProfile.getDriverId())
                .name(driverProfile.getName())
                .acceptanceRate(driverProfile.getAcceptanceRate())
                .totalRequests(driverProfile.getTotalRequests())
                .totalRejected(driverProfile.getTotalRejected())
                .totalCompleted(driverProfile.getTotalCompleted())
                .rating(driverProfile.getRating())
                .build();
    }
}
