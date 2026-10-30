package com.scheduler.distributed_job_scheduler_backend.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request){
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(errorResponse);
    }
}
