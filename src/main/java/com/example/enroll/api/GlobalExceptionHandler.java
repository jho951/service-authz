package com.example.enroll.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        ErrorCode code = ex.errorCode();
        return ResponseEntity.status(code.status())
                .body(ErrorResponse.of(code, ex.getMessage(), ex.detail()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        ErrorCode code = ErrorCode.REG_NOT_FOUND;
        return ResponseEntity.status(code.status())
                .body(ErrorResponse.of(code, "Unexpected error", Map.of()));
    }
}
