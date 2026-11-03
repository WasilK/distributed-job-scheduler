package com.scheduler.distributed_job_scheduler_backend.dto.projects;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectRequest {
    @NotBlank
    private String projectName;
}
