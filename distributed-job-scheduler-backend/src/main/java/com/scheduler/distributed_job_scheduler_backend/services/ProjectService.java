package com.scheduler.distributed_job_scheduler_backend.services;

import com.scheduler.distributed_job_scheduler_backend.dto.projects.ProjectRequest;
import com.scheduler.distributed_job_scheduler_backend.dto.projects.ProjectResponse;
import com.scheduler.distributed_job_scheduler_backend.entities.Project;
import com.scheduler.distributed_job_scheduler_backend.entities.User;
import com.scheduler.distributed_job_scheduler_backend.repository.ProjectRepository;
import com.scheduler.distributed_job_scheduler_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository){
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public ProjectResponse getProjectById(Long projectId, String email){
        User owner = findUserByEmail(email);
        return toResponse(findOwnedProject(projectId, owner.getUserId()));
    }

    public List<ProjectResponse> getAllProjects(String email){
        User owner = findUserByEmail(email);
        return projectRepository.findAllByOwner_UserIdOrderByCreatedAtDesc(owner.getUserId())
                .stream()
                .map(ProjectService::toResponse)
                .toList();
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, String email){
        String projectName = validateProjectName(request);
        User owner = findUserByEmail(email);
        Project project = new Project();
        project.setProjectName(projectName);
        project.setOwner(owner);
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request, String email){
        String projectName = validateProjectName(request);
        User owner = findUserByEmail(email);
        Project project = findOwnedProject(projectId, owner.getUserId());
        project.setProjectName(projectName);
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long projectId, String email){
        User owner = findUserByEmail(email);
        projectRepository.delete(findOwnedProject(projectId, owner.getUserId()));
    }

    private User findUserByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required.");
        }
        return userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private Project findOwnedProject(Long projectId, Long ownerId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID is required.");
        }
        return projectRepository.findByProjectIdAndOwner_UserId(projectId, ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));
    }

    private String validateProjectName(ProjectRequest request) {
        if (request == null || !StringUtils.hasText(request.getProjectName())) {
            throw new IllegalArgumentException("Project name is required.");
        }
        return request.getProjectName().trim();
    }

    private static ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setProjectId(project.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setOwnerId(project.getOwner().getUserId());
        return response;
    }
}
