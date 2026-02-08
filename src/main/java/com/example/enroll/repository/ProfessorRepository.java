package com.example.enroll.repository;

import com.example.enroll.domain.professor.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
}
