package com.example.enroll.repository;

import com.example.enroll.domain.department.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
