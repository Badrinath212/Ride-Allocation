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

        return responseDto;
    }

    public HourlyAnalyticsResponseDto getHourlyAnalytics(LocalDate date, String hour) {

        String dateHour = date.toString() + "#" + hour;
        System.out.println("dateHour: " + dateHour);

        HourlyAnalyticsResponseDto responseDto = analyticsRepository.getHourlyAnalytics(dateHour);

        if(responseDto == null) throw new ResourceNotFoundException("Resource Not Available");
        return responseDto;
    }

    public DriverAnalyticsDto getDriverAnalytics(String driverId) {

        DriverAnalyticsDto responseDto = analyticsRepository.getDriverAnalytics(driverId);

        if(responseDto == null) throw new ResourceNotFoundException("Resource Not Available");

        return responseDto;
    }
}
