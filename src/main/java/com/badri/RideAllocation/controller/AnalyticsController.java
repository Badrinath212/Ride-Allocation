package com.badri.RideAllocation.controller;

import com.badri.RideAllocation.dto.DailyAnalyticsResponseDto;
import com.badri.RideAllocation.dto.DriverAnalyticsDto;
import com.badri.RideAllocation.dto.HourlyAnalyticsResponseDto;
import com.badri.RideAllocation.service.AnalyticsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/ride/analytics")
@Validated
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
            LocalDate endDate) {
        System.out.println("StartDate: " + startDate + " " + "EndDate: " + endDate);
        return null;
    }

    @GetMapping("/driver-analytics/daily")
    public ResponseEntity<DriverAnalyticsDto> getDriverDailyAnalytics(
            @RequestParam
            String driverId,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate date) {

        System.out.println("driverId: " + driverId + " date: " + date);
        DriverAnalyticsDto response = analyticsService.getDriverDailyAnalytics(driverId, date);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/driver-analytics/hourly")
    public ResponseEntity<DriverAnalyticsDto> getDriverHourlyAnalytics(
            @RequestParam
            String driverId,
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            LocalDate date,
            @RequestParam
            @Min(value = 0, message = "Hour must be at least 0")
            @Max(value = 23, message = "Hour cannot be exceed 23")
            int hour) {

        System.out.println("driverId: " + driverId + " date: " + date + " hour: " + hour);
        DriverAnalyticsDto response = analyticsService.getDriverHourlyAnalytics(driverId, date, hour);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
