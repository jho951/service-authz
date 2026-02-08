package com.example.enroll.repository;

import com.example.enroll.domain.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}
