package com.badri.RideAllocation.repository;

import com.badri.RideAllocation.dto.DailyAnalyticsResponseDto;
import com.badri.RideAllocation.dto.DriverAnalyticsDto;
import com.badri.RideAllocation.dto.HourlyAnalyticsResponseDto;
import com.badri.RideAllocation.model.*;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.LocalDate;

@Repository
public class AnalyticsRepository {

    private final DynamoDbTable<DailyRideAnalytics> dailyRideAnalyticsTable;
    private final DynamoDbTable<HourlyRideAnalytics> hourlyRideAnalyticsTable;
    private final DynamoDbTable<DriverProfile> driverProfileTable;
    private final DynamoDbTable<DailyDriverAnalytics> dailyDriverAnalyticsTable;
    private final DynamoDbTable<HourlyDriverAnalytics> hourlyDriverAnalyticsTable;

    public AnalyticsRepository(DynamoDbTable<DailyRideAnalytics> dailyRideAnalyticsTable,
                               DynamoDbTable<HourlyRideAnalytics> hourlyRideAnalyticsTable,
                               DynamoDbTable<DriverProfile> driverProfileTable,
                               DynamoDbTable<DailyDriverAnalytics> dailyDriverAnalyticsTable,
                               DynamoDbTable<HourlyDriverAnalytics> hourlyDriverAnalyticsTable) {
        this.dailyRideAnalyticsTable = dailyRideAnalyticsTable;
        this.hourlyRideAnalyticsTable = hourlyRideAnalyticsTable;
        this.driverProfileTable = driverProfileTable;
        this.dailyDriverAnalyticsTable = dailyDriverAnalyticsTable;
        this.hourlyDriverAnalyticsTable = hourlyDriverAnalyticsTable;
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

    public DriverAnalyticsDto getDriverDailyAnalytics(String driverId, LocalDate date) {

        DailyDriverAnalytics dailyDriverAnalytics = dailyDriverAnalyticsTable.getItem(
                Key.builder().partitionValue(driverId).sortValue(date.toString()).build()
        );

        return DriverAnalyticsDto.builder()
                .driverId(dailyDriverAnalytics.getDriverId())
                .date(dailyDriverAnalytics.getDate())
                .totalRequests(dailyDriverAnalytics.getTotalRequests())
                .totalRejected(dailyDriverAnalytics.getTotalRejected())
                .totalCompleted(dailyDriverAnalytics.getTotalCompleted())
                .totalAccepted(dailyDriverAnalytics.getTotalAccepted())
                .totalStarted(dailyDriverAnalytics.getTotalStarted())
                .totalCancelled(dailyDriverAnalytics.getTotalCancelled())
                .totalRevenue(dailyDriverAnalytics.getTotalRevenue())
                .build();
    }

    public DriverAnalyticsDto getDriverHourlyAnalytics(String driverId, LocalDate date, int hour) {

        String dateHour = date.toString() + "#" + hour;
        HourlyDriverAnalytics hourlyDriverAnalytics = hourlyDriverAnalyticsTable.getItem(
                Key.builder().partitionValue(driverId).sortValue(dateHour).build()
        );

        return DriverAnalyticsDto.builder()
                .driverId(hourlyDriverAnalytics.getDriverId())
                .dateHour(hourlyDriverAnalytics.getDateHour())
                .totalRequests(hourlyDriverAnalytics.getTotalRequests())
                .totalRejected(hourlyDriverAnalytics.getTotalRejected())
                .totalCompleted(hourlyDriverAnalytics.getTotalCompleted())
                .totalAccepted(hourlyDriverAnalytics.getTotalAccepted())
                .totalStarted(hourlyDriverAnalytics.getTotalStarted())
                .totalCancelled(hourlyDriverAnalytics.getTotalCancelled())
                .totalRevenue(hourlyDriverAnalytics.getTotalRevenue())
                .build();

    }
}
