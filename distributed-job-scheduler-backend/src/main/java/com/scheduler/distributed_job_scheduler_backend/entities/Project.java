package com.scheduler.distributed_job_scheduler_backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;
    @NotBlank
    @Column(nullable = false)
    private String projectName;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User owner;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
