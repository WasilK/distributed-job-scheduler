package com.scheduler.distributed_job_scheduler_backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @NotBlank
    @Column(nullable = false, unique = true)
    @Email
    private String email;
    @NotBlank
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
