package com.scheduler.distributed_job_scheduler_backend.repository;

import com.scheduler.distributed_job_scheduler_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
}
