package com.example.enroll.repository;

import com.example.enroll.domain.registration.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Optional<Registration> findByStudentIdAndCourseId(Long studentId, Long courseId);
    List<Registration> findByStudentId(Long studentId);
    List<Registration> findByCourseId(Long courseId);
}
