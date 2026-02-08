package com.example.enroll.service.seeder;

import org.springframework.stereotype.Component;

@Component
public class DataSeeder {
    private volatile SeedingStatus status = SeedingStatus.SEEDING;

    public SeedingStatus getStatus() {
        return status;
    }

    public void markDone() {
        this.status = SeedingStatus.DONE;
    }
}
