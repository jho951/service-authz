package com.example.enroll.controller;

import com.example.enroll.api.ApiResponse;
import com.example.enroll.api.BusinessException;
import com.example.enroll.api.ErrorCode;
import com.example.enroll.domain.student.Student;
import com.example.enroll.repository.StudentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StudentController {
    private final StudentRepository studentRepository;

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping("/students")
    public ApiResponse<List<StudentResponse>> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<StudentResponse> data = studentRepository.findAll(pageable)
                .map(StudentResponse::from)
                .toList();
        return ApiResponse.of(data);
    }

    @GetMapping("/students/{studentId}")
    public ApiResponse<StudentResponse> get(@PathVariable Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REG_NOT_FOUND));
        return ApiResponse.of(StudentResponse.from(student));
    }

    public record StudentResponse(Long id, String name, Long departmentId, String departmentName) {
        public static StudentResponse from(Student student) {
            return new StudentResponse(
                    student.getId(),
                    student.getName(),
                    student.getDepartmentId(),
                    student.getDepartmentName()
            );
        }
    }
}
