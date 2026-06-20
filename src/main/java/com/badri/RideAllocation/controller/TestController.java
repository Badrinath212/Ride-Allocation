package com.badri.RideAllocation.controller;

import com.badri.RideAllocation.events.DriverRideResponseEvent;
import com.badri.RideAllocation.producer.DriverEventProducer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/test")
public class TestController {
    private final DriverEventProducer driverEventProducer;
    private final ObjectMapper objectMapper;

    public TestController(DriverEventProducer driverEventProducer, ObjectMapper objectMapper) {
        this.driverEventProducer = driverEventProducer;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/")
    public String welcome() {
        return "Welcome to the Ride Allocation Application";
    }

    @GetMapping("/testevent")
    public String testEvent() {
        DriverRideResponseEvent driverAcceptedEvent = DriverRideResponseEvent.builder()
                        .driverId("420")
                        .build();

        String json = objectMapper.writeValueAsString(driverAcceptedEvent);
        driverEventProducer.testEvent(json);
        return "Driver Test Event sent successfully";
    }
}
