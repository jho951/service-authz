package com.example.enroll.service.registration;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class CourseLockManager {
    private final ConcurrentHashMap<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ReentrantLock getLock(Long courseId) {
        return locks.computeIfAbsent(courseId, id -> new ReentrantLock());
    }
}
