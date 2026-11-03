package com.scheduler.distributed_job_scheduler_backend.repository;

import com.scheduler.distributed_job_scheduler_backend.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByOwner_UserIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Project> findByProjectIdAndOwner_UserId(Long projectId, Long ownerId);
}
