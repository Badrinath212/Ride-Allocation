package com.badri.RideAllocation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/")
    public String welcome() {
        return "Welcome to the Ride Allocation Application";
    }
}
