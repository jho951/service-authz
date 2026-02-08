package com.example.enroll.repository;

import com.example.enroll.domain.course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByDepartmentId(Long departmentId, Pageable pageable);
}
