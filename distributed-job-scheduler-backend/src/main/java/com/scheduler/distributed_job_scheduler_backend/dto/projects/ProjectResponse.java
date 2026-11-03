package com.scheduler.distributed_job_scheduler_backend.dto.projects;

import lombok.Data;

@Data
public class ProjectResponse {
    private Long projectId;
    private String projectName;
    private Long ownerId;
}
