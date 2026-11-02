package com.scheduler.distributed_job_scheduler_backend.dto.auth;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String email;
}
