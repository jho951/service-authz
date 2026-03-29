package com.example.permission.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.example.permission")
public class PermissionExceptionHandler {

    @ExceptionHandler(PermissionBadRequestException.class)
    public ResponseEntity<PermissionErrorResponse> handleBadRequest(PermissionBadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new PermissionErrorResponse(ex.getMessage()));
    }
}
