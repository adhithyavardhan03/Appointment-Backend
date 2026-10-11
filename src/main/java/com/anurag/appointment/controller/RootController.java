package com.anurag.appointment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "status", "running",
                "message", "Appointment backend is running successfully.",
                "frontend", "http://localhost:5173"
        );
    }
}
