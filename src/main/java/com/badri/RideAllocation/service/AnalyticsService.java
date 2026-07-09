package com.badri.RideAllocation.service;

import com.badri.RideAllocation.dto.DailyAnalyticsResponseDto;
import com.badri.RideAllocation.dto.DriverAnalyticsDto;
import com.badri.RideAllocation.dto.HourlyAnalyticsResponseDto;
import com.badri.RideAllocation.model.DailyRideAnalytics;
import com.badri.RideAllocation.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public DailyAnalyticsResponseDto getDailyAnalytics(String date) {
        return analyticsRepository.getDailyAnalytics(date);
    }

    public HourlyAnalyticsResponseDto getHourlyAnalytics(String dateHour) {
        return analyticsRepository.getHourlyAnalytics(dateHour);
    }

    public DriverAnalyticsDto getDriverAnalytics(String driverId) {
        return analyticsRepository.getDriverAnalytics(driverId);
    }
}
