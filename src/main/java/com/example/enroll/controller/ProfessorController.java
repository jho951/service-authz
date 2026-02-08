package com.example.enroll.controller;

import com.example.enroll.api.ApiResponse;
import com.example.enroll.api.BusinessException;
import com.example.enroll.api.ErrorCode;
import com.example.enroll.domain.professor.Professor;
import com.example.enroll.repository.ProfessorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProfessorController {
    private final ProfessorRepository professorRepository;

    public ProfessorController(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    @GetMapping("/professors")
    public ApiResponse<List<ProfessorResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ProfessorResponse> data = professorRepository.findAll(pageable)
                .map(ProfessorResponse::from)
                .toList();
        return ApiResponse.of(data);
    }

    @GetMapping("/professors/{professorId}")
    public ApiResponse<ProfessorResponse> get(@PathVariable Long professorId) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
        return ApiResponse.of(ProfessorResponse.from(professor));
    }

    public record ProfessorResponse(Long id, String name, Long departmentId, String departmentName) {
        public static ProfessorResponse from(Professor professor) {
            return new ProfessorResponse(
                    professor.getId(),
                    professor.getName(),
                    professor.getDepartmentId(),
                    professor.getDepartmentName()
            );
        }
    }
}
