package com.badri.RideAllocation.controller;

import com.badri.RideAllocation.service.ScriptsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/scripts")
public class ScriptsController {
    private final ScriptsService scriptsService;

    public ScriptsController(ScriptsService scriptsService) {
        this.scriptsService = scriptsService;
    }

    @PostMapping("/load-drivers")
    public String loadDrivers() {
        scriptsService.addDriverProfilesData();
        return "Success";
    }
}
