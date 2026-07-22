package com.badri.RideAllocation.service;

import com.badri.RideAllocation.dto.DailyAnalyticsResponseDto;
import com.badri.RideAllocation.dto.DriverAnalyticsDto;
import com.badri.RideAllocation.dto.HourlyAnalyticsResponseDto;
import com.badri.RideAllocation.exception.ResourceNotFoundException;
import com.badri.RideAllocation.model.DailyRideAnalytics;
import com.badri.RideAllocation.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public DailyAnalyticsResponseDto getDailyAnalytics(LocalDate date) {

        DailyAnalyticsResponseDto responseDto = analyticsRepository.getDailyAnalytics(date.toString());
        if(responseDto == null) throw new ResourceNotFoundException("Resource Not Available");

        double acceptanceRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalAccepted() * 100.0 ) / responseDto.getTotalRequests();

        double cancellationRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalCancelled() * 100.0) / responseDto.getTotalRequests();

        double completionRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalCompleted() * 100.0) / responseDto.getTotalRequests();

        double avgRevenuePerRide = responseDto.getTotalCompleted() == 0
                ? 0.0
                : responseDto.getTotalRevenue() / responseDto.getTotalCompleted();


        return DailyAnalyticsResponseDto.builder()
                .date(responseDto.getDate())
                .totalAccepted(responseDto.getTotalAccepted())
                .totalCancelled(responseDto.getTotalCancelled())
                .totalCompleted(responseDto.getTotalCompleted())
                .totalRejected(responseDto.getTotalRejected())
                .totalRequests(responseDto.getTotalRequests())
                .totalStarted(responseDto.getTotalStarted())
                .totalRevenue(responseDto.getTotalRevenue())
                .acceptanceRate(acceptanceRate)
                .cancellationRate(cancellationRate)
                .avgRevenuePerRide(avgRevenuePerRide)
                .completionRate(completionRate)
                .build();
    }

    public HourlyAnalyticsResponseDto getHourlyAnalytics(LocalDate date, String hour) {

        String dateHour = date.toString() + "#" + hour;
        System.out.println("dateHour: " + dateHour);

        HourlyAnalyticsResponseDto responseDto = analyticsRepository.getHourlyAnalytics(dateHour);
        if(responseDto == null) throw new ResourceNotFoundException("Resource Not Available");

        double acceptanceRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalAccepted() * 100.0 ) / responseDto.getTotalRequests();

        double cancellationRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalCancelled() * 100.0) / responseDto.getTotalRequests();

        double completionRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalCompleted() * 100.0) / responseDto.getTotalRequests();

        double avgRevenuePerRide = responseDto.getTotalCompleted() == 0
                ? 0.0
                : responseDto.getTotalRevenue() / responseDto.getTotalCompleted();

        return HourlyAnalyticsResponseDto.builder()
                .dateHour(responseDto.getDateHour())
                .totalCompleted(responseDto.getTotalCompleted())
                .totalAccepted(responseDto.getTotalAccepted())
                .totalCancelled(responseDto.getTotalCancelled())
                .totalRejected(responseDto.getTotalRejected())
                .totalRequests(responseDto.getTotalRequests())
                .totalRevenue(responseDto.getTotalRevenue())
                .totalStarted(responseDto.getTotalStarted())
                .acceptanceRate(acceptanceRate)
                .completionRate(completionRate)
                .cancellationRate(cancellationRate)
                .avgRevenuePerRide(avgRevenuePerRide)
                .build();
    }

    public DriverAnalyticsDto getDriverAnalytics(String driverId) {

        DriverAnalyticsDto responseDto = analyticsRepository.getDriverAnalytics(driverId);
        if(responseDto == null) throw new ResourceNotFoundException("Resource Not Available");

        double acceptanceRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalAccepted() * 100.0 ) / responseDto.getTotalRequests();

        double cancellationRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalCancelled() * 100.0) / responseDto.getTotalRequests();

        double completionRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalCompleted() * 100.0) / responseDto.getTotalRequests();

        double avgRevenuePerRide = responseDto.getTotalCompleted() == 0
                ? 0.0
                : responseDto.getTotalRevenue() / responseDto.getTotalCompleted();

        return DriverAnalyticsDto.builder()
                .driverId(responseDto.getDriverId())
                .name(responseDto.getName())
                .acceptanceRate(responseDto.getAcceptanceRate())
                .totalRequests(responseDto.getTotalRequests())
                .totalRejected(responseDto.getTotalRejected())
                .totalCompleted(responseDto.getTotalCompleted())
                .rating(responseDto.getRating())
                .cancellationRate(cancellationRate)
                .completionRate(completionRate)
                .avgRevenuePerRide(avgRevenuePerRide)
                .build();
    }

    public DriverAnalyticsDto getDriverDailyAnalytics(String driverId, LocalDate date) {

        DriverAnalyticsDto responseDto = analyticsRepository.getDriverDailyAnalytics(driverId, date);
        if(responseDto == null) throw new ResourceNotFoundException("Resource Not Available");

        double acceptanceRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalAccepted() * 100.0 ) / responseDto.getTotalRequests();

        double cancellationRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalCancelled() * 100.0) / responseDto.getTotalRequests();

        double completionRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalCompleted() * 100.0) / responseDto.getTotalRequests();

        double avgRevenuePerRide = responseDto.getTotalCompleted() == 0
                ? 0.0
                : responseDto.getTotalRevenue() / responseDto.getTotalCompleted();

        return DriverAnalyticsDto.builder()
                .driverId(responseDto.getDriverId())
                .date(responseDto.getDate())
                .totalRequests(responseDto.getTotalRequests())
                .totalRejected(responseDto.getTotalRejected())
                .totalCompleted(responseDto.getTotalCompleted())
                .totalAccepted(responseDto.getTotalAccepted())
                .totalStarted(responseDto.getTotalStarted())
                .totalCancelled(responseDto.getTotalCancelled())
                .totalRevenue(responseDto.getTotalRevenue())
                .cancellationRate(cancellationRate)
                .completionRate(completionRate)
                .acceptanceRate(acceptanceRate)
                .avgRevenuePerRide(avgRevenuePerRide)
                .build();
    }

    public DriverAnalyticsDto getDriverHourlyAnalytics(String driverId, LocalDate date, int hour) {

        DriverAnalyticsDto responseDto = analyticsRepository.getDriverHourlyAnalytics(driverId, date, hour);
        if(responseDto == null) throw new ResourceNotFoundException("Resource Not Available");

        double acceptanceRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalAccepted() * 100.0 ) / responseDto.getTotalRequests();

        double cancellationRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalCancelled() * 100.0) / responseDto.getTotalRequests();

        double completionRate = responseDto.getTotalRequests() == 0
                ? 0.0
                : (responseDto.getTotalCompleted() * 100.0) / responseDto.getTotalRequests();

        double avgRevenuePerRide = responseDto.getTotalCompleted() == 0
                ? 0.0
                : responseDto.getTotalRevenue() / responseDto.getTotalCompleted();

        return DriverAnalyticsDto.builder()
                .driverId(responseDto.getDriverId())
                .dateHour(responseDto.getDateHour())
                .totalRequests(responseDto.getTotalRequests())
                .totalRejected(responseDto.getTotalRejected())
                .totalCompleted(responseDto.getTotalCompleted())
                .totalAccepted(responseDto.getTotalAccepted())
                .totalStarted(responseDto.getTotalStarted())
                .totalCancelled(responseDto.getTotalCancelled())
                .totalRevenue(responseDto.getTotalRevenue())
                .cancellationRate(cancellationRate)
                .completionRate(completionRate)
                .acceptanceRate(acceptanceRate)
                .avgRevenuePerRide(avgRevenuePerRide)
                .build();
    }
}
