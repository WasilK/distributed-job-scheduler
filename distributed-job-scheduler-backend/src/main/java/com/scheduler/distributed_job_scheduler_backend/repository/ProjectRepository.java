package com.scheduler.distributed_job_scheduler_backend.repository;

import com.scheduler.distributed_job_scheduler_backend.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
