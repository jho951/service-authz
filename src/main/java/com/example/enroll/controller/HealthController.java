package com.example.enroll.controller;

import com.example.enroll.service.seeder.DataSeeder;
import com.example.enroll.service.seeder.SeedingStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {
    private final DataSeeder dataSeeder;

    public HealthController(DataSeeder dataSeeder) {
        this.dataSeeder = dataSeeder;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> counts = new HashMap<>();
        counts.put("students", dataSeeder.getStudentCount());
        counts.put("courses", dataSeeder.getCourseCount());
        counts.put("professors", dataSeeder.getProfessorCount());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("seeding", dataSeeder.getStatus() == SeedingStatus.DONE ? "DONE" : "SEEDING");
        response.put("counts", counts);
        return response;
    }
}
