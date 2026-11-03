package com.scheduler.distributed_job_scheduler_backend.integration;

import com.scheduler.distributed_job_scheduler_backend.dto.auth.AuthRequest;
import com.scheduler.distributed_job_scheduler_backend.dto.auth.AuthResponse;
import com.scheduler.distributed_job_scheduler_backend.dto.projects.ProjectRequest;
import com.scheduler.distributed_job_scheduler_backend.dto.projects.ProjectResponse;
import com.scheduler.distributed_job_scheduler_backend.entities.User;
import com.scheduler.distributed_job_scheduler_backend.repository.ProjectRepository;
import com.scheduler.distributed_job_scheduler_backend.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:api-integration-test;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "app.jwt.secret=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class AuthAndProjectApiIntegrationTest {
    private static final String API = "/api";

    @Value("${local.server.port}")
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void signupLoginAndProjectApisEnforceAuthenticationAndOwnership() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String emailA = "owner-a-" + suffix + "@example.com";
        String emailB = "owner-b-" + suffix + "@example.com";
        String password = "integration-password";

        HttpResponse<String> unauthenticatedResponse = send("GET", "/projects", null, null);
        assertEquals(403, unauthenticatedResponse.statusCode());

        signup(emailA, password);
        User savedUserA = userRepository.findByEmailIgnoreCase(emailA).orElseThrow();
        assertNotEquals(password, savedUserA.getPassword());
        assertTrue(passwordEncoder.matches(password, savedUserA.getPassword()));
        AuthResponse loginA = login(emailA, password);
        assertEquals(emailA, loginA.getEmail());
        assertEquals(savedUserA.getUserId(), loginA.getUserId());

        ProjectResponse projectA = createProject(loginA.getToken(), "Project A");
        assertEquals(savedUserA.getUserId(), projectA.getOwnerId());
        assertTrue(projectRepository.existsById(projectA.getProjectId()));

        signup(emailB, password);
        AuthResponse loginB = login(emailB, password);
        assertEquals(0, getProjectList(loginB.getToken()).length);

        HttpResponse<String> foreignReadResponse = send(
                "GET", "/projects/" + projectA.getProjectId(), loginB.getToken(), null);
        assertEquals(400, foreignReadResponse.statusCode());

        HttpResponse<String> foreignUpdateResponse = send(
                "PUT", "/projects/" + projectA.getProjectId(), loginB.getToken(),
                projectRequest("Hijacked project"));
        assertEquals(400, foreignUpdateResponse.statusCode());

        HttpResponse<String> foreignDeleteResponse = send(
                "DELETE", "/projects/" + projectA.getProjectId(), loginB.getToken(), null);
        assertEquals(400, foreignDeleteResponse.statusCode());

        HttpResponse<String> ownedReadResponse = send(
                "GET", "/projects/" + projectA.getProjectId(), loginA.getToken(), null);
        assertEquals(200, ownedReadResponse.statusCode());
        assertEquals("Project A", objectMapper.readValue(
                ownedReadResponse.body(), ProjectResponse.class).getProjectName());

        HttpResponse<String> updateResponse = send(
                "PUT", "/projects/" + projectA.getProjectId(), loginA.getToken(),
                projectRequest("Updated Project A"));
        assertEquals(200, updateResponse.statusCode());
        assertEquals("Updated Project A", objectMapper.readValue(
                updateResponse.body(), ProjectResponse.class).getProjectName());
        assertEquals("Updated Project A", projectRepository.findById(projectA.getProjectId())
                .orElseThrow().getProjectName());

        HttpResponse<String> deleteResponse = send(
                "DELETE", "/projects/" + projectA.getProjectId(), loginA.getToken(), null);
        assertEquals(204, deleteResponse.statusCode());
        assertTrue(projectRepository.findById(projectA.getProjectId()).isEmpty());
    }

    private void signup(String email, String password) throws Exception {
        HttpResponse<String> response = send("POST", "/auth/signup", null,
                authRequest(email, password));
        assertEquals(201, response.statusCode());
    }

    private AuthResponse login(String email, String password) throws Exception {
        HttpResponse<String> response = send("POST", "/auth/login", null,
                authRequest(email, password));
        assertEquals(200, response.statusCode());
        return objectMapper.readValue(response.body(), AuthResponse.class);
    }

    private ProjectResponse createProject(String token, String name) throws Exception {
        HttpResponse<String> response = send(
                "POST", "/projects", token, projectRequest(name));
        assertEquals(201, response.statusCode());
        return objectMapper.readValue(response.body(), ProjectResponse.class);
    }

    private ProjectResponse[] getProjectList(String token) throws Exception {
        HttpResponse<String> response = send("GET", "/projects", token, null);
        assertEquals(200, response.statusCode());
        return objectMapper.readValue(response.body(), ProjectResponse[].class);
    }

    private HttpResponse<String> send(String method, String path, String token, Object body)
            throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url(path)))
                .header("Accept", "application/json");
        if (body != null) {
            request.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(
                            objectMapper.writeValueAsString(body)));
        } else {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        }
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String url(String path) {
        return "http://localhost:" + port + API + path;
    }

    private static AuthRequest authRequest(String email, String password) {
        AuthRequest request = new AuthRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    private static ProjectRequest projectRequest(String name) {
        ProjectRequest request = new ProjectRequest();
        request.setProjectName(name);
        return request;
    }
}
