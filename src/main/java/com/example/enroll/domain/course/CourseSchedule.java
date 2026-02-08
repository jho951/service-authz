package com.example.enroll.domain.course;

import jakarta.persistence.Embeddable;

@Embeddable
public class CourseSchedule {
    private String dayOfWeek;
    private String startTime;
    private String endTime;

    protected CourseSchedule() {
    }

    public CourseSchedule(String dayOfWeek, String startTime, String endTime) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
