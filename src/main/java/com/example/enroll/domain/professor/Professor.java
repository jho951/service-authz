package com.example.enroll.domain.professor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Professor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long departmentId;

    protected Professor() {
    }

    public Professor(String name, Long departmentId) {
        this.name = name;
        this.departmentId = departmentId;
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
}
