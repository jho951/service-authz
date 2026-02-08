package com.example.enroll.controller;

import com.example.enroll.api.ApiResponse;
import com.example.enroll.domain.registration.Registration;
import com.example.enroll.service.registration.RegistrationService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/registrations")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(@RequestBody RegisterRequest request) {
        Registration registration = registrationService.register(request.studentId(), request.courseId());
        return ResponseEntity.status(201).body(ApiResponse.of(RegistrationResponse.from(registration)));
    }

    @DeleteMapping("/registrations/{registrationId}")
    public ResponseEntity<ApiResponse<RegistrationResponse>> cancel(@PathVariable Long registrationId) {
        Registration registration = registrationService.cancel(registrationId);
        return ResponseEntity.ok(ApiResponse.of(RegistrationResponse.from(registration)));
    }

    public record RegisterRequest(@NotNull Long studentId, @NotNull Long courseId) {
    }

    public record RegistrationResponse(Long registrationId, Long studentId, Long courseId, String status) {
        public static RegistrationResponse from(Registration registration) {
            return new RegistrationResponse(
                    registration.getId(),
                    registration.getStudentId(),
                    registration.getCourseId(),
                    registration.getStatus()
            );
        }
    }
}
