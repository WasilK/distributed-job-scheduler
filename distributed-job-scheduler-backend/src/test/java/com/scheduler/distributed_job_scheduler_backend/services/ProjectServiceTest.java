package com.scheduler.distributed_job_scheduler_backend.services;

import com.scheduler.distributed_job_scheduler_backend.dto.projects.ProjectRequest;
import com.scheduler.distributed_job_scheduler_backend.dto.projects.ProjectResponse;
import com.scheduler.distributed_job_scheduler_backend.entities.Project;
import com.scheduler.distributed_job_scheduler_backend.entities.User;
import com.scheduler.distributed_job_scheduler_backend.repository.ProjectRepository;
import com.scheduler.distributed_job_scheduler_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    private static final String USER_A_EMAIL = "user.a@example.com";
    private static final long USER_A_ID = 1L;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    private ProjectService projectService;
    private User userA;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, userRepository);
        userA = user(USER_A_ID, USER_A_EMAIL);
        when(userRepository.findByEmailIgnoreCase(USER_A_EMAIL)).thenReturn(Optional.of(userA));
    }

    @Test
    void userCanListOwnProjects() {
        Project ownProject = project(10L, "User A project", userA);
        when(projectRepository.findAllByOwner_UserIdOrderByCreatedAtDesc(USER_A_ID))
                .thenReturn(List.of(ownProject));

        List<ProjectResponse> projects = projectService.getAllProjects(USER_A_EMAIL);

        assertEquals(1, projects.size());
        assertEquals(10L, projects.get(0).getProjectId());
        assertEquals("User A project", projects.get(0).getProjectName());
        assertEquals(USER_A_ID, projects.get(0).getOwnerId());
        verify(projectRepository).findAllByOwner_UserIdOrderByCreatedAtDesc(USER_A_ID);
    }

    @Test
    void userCannotAccessAnotherUsersProject() {
        when(projectRepository.findByProjectIdAndOwner_UserId(20L, USER_A_ID))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> projectService.getProjectById(20L, USER_A_EMAIL));
    }

    @Test
    void userCannotUpdateAnotherUsersProject() {
        when(projectRepository.findByProjectIdAndOwner_UserId(20L, USER_A_ID))
                .thenReturn(Optional.empty());
        ProjectRequest request = request("Changed project name");

        assertThrows(IllegalArgumentException.class,
                () -> projectService.updateProject(20L, request, USER_A_EMAIL));
        verify(projectRepository, never()).save(org.mockito.ArgumentMatchers.any(Project.class));
    }

    @Test
    void userCannotDeleteAnotherUsersProject() {
        when(projectRepository.findByProjectIdAndOwner_UserId(20L, USER_A_ID))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> projectService.deleteProject(20L, USER_A_EMAIL));
        verify(projectRepository, never()).delete(org.mockito.ArgumentMatchers.any(Project.class));
    }

    private static User user(Long id, String email) {
        User user = new User();
        user.setUserId(id);
        user.setEmail(email);
        user.setPassword("encoded-password");
        return user;
    }

    private static Project project(Long id, String name, User owner) {
        Project project = new Project();
        project.setProjectId(id);
        project.setProjectName(name);
        project.setOwner(owner);
        return project;
    }

    private static ProjectRequest request(String name) {
        ProjectRequest request = new ProjectRequest();
        request.setProjectName(name);
        return request;
    }
}
