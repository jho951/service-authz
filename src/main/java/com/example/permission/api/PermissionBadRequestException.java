package com.example.permission.api;

public class PermissionBadRequestException extends RuntimeException {
    public PermissionBadRequestException(String message) {
        super(message);
    }
}
