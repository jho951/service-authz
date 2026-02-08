package com.example.enroll.domain.course;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long departmentId;
    private Long professorId;
    private int credits;
    private int capacity;
    private int enrolled;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<CourseSchedule> schedules = new ArrayList<>();

    protected Course() {
    }

    public Course(String name, Long departmentId, Long professorId, int credits, int capacity) {
        this.name = name;
        this.departmentId = departmentId;
        this.professorId = professorId;
        this.credits = credits;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public int getCredits() {
        return credits;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEnrolled() {
        return enrolled;
    }

    public List<CourseSchedule> getSchedules() {
        return schedules;
    }

    public void increaseEnrolled() {
        this.enrolled += 1;
    }

    public void decreaseEnrolled() {
        if (this.enrolled > 0) {
            this.enrolled -= 1;
        }
    }
}
