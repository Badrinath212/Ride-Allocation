package com.badri.RideAllocation.controller;

import com.badri.RideAllocation.dto.DailyAnalyticsResponseDto;
import com.badri.RideAllocation.dto.DriverAnalyticsDto;
import com.badri.RideAllocation.dto.HourlyAnalyticsResponseDto;
import com.badri.RideAllocation.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/ride/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/daily")
    public ResponseEntity<DailyAnalyticsResponseDto> getDailyAnalytics(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate date) {
        System.out.println("date :" + date);

        DailyAnalyticsResponseDto responseDto = analyticsService.getDailyAnalytics(date);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/hourly")
    public ResponseEntity<HourlyAnalyticsResponseDto> getHourlyAnalytics(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate date,
            @RequestParam
            String hour) {
        System.out.println("date hour:" + date + "#" + hour);

        HourlyAnalyticsResponseDto responseDto = analyticsService.getHourlyAnalytics(date, hour);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/driver")
    public ResponseEntity<DriverAnalyticsDto> getDriverAnalytics(@RequestParam String driverId) {
        System.out.println("DriverId :" + driverId);

        DriverAnalyticsDto responseDto = analyticsService.getDriverAnalytics(driverId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/weekly")
    public ResponseEntity<DailyAnalyticsResponseDto> getWeeklyAnalytics(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate startDate,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate endDate
    ) {
        System.out.println("StartDate: " + startDate + " " + "EndDate: " + endDate);

        return null;
    }
}
