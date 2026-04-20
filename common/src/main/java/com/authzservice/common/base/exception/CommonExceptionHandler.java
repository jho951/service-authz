package com.authzservice.common.base.exception;

import com.authzservice.common.base.constant.ErrorCode;
import com.authzservice.common.base.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.authzservice.app")
public class CommonExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(CommonExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        logWithRequest(request, errorCode, ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        logWithRequest(request, errorCode, ex.getClass().getSimpleName(), ex.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(new ErrorResponse(errorCode.getCode(), errorCode.getMessage()));
    }

    private void logWithRequest(HttpServletRequest request, ErrorCode errorCode, String exceptionType, String exceptionMessage) {
        if (request == null) {
            log.warn("request failed status={} code={} exceptionType={} message={}",
                    errorCode.getHttpStatus(),
                    errorCode.getCode(),
                    exceptionType,
                    exceptionMessage);
            return;
        }

        log.warn("request failed status={} code={} method={} uri={} ip={} exceptionType={} message={}",
                errorCode.getHttpStatus(),
                errorCode.getCode(),
                request.getMethod(),
                request.getRequestURI(),
                resolveClientIp(request),
                exceptionType,
                exceptionMessage);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int commaIndex = forwardedFor.indexOf(',');
            return commaIndex > -1 ? forwardedFor.substring(0, commaIndex).trim() : forwardedFor.trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null || remoteAddr.isBlank() ? "unknown" : remoteAddr;
    }
}
