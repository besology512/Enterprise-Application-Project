package com.workhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhub.model.*;
import com.workhub.repository.ProjectRepository;
import com.workhub.repository.TaskRepository;
import com.workhub.repository.UserRepository;
import com.workhub.security.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class TenantIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String tenantAToken;
    private String tenantBToken;
    private Long tenantBProjectId;
    private Long tenantBTaskId;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        com.workhub.model.User userA = com.workhub.model.User.builder()
                .email("userA@tenantA.com")
                .password("password")
                .tenantId(1L)
                .roles(Set.of(com.workhub.model.User.Role.TENANT_ADMIN))
                .build();
        userRepository.save(userA);

        com.workhub.model.User userB = com.workhub.model.User.builder()
                .email("userB@tenantB.com")
                .password("password")
                .tenantId(2L)
                .roles(Set.of(com.workhub.model.User.Role.TENANT_ADMIN))
                .build();
        userRepository.save(userB);

        String tenantAId = "1";
        tenantAToken = generateToken("userA@tenantA.com", 1L);

        Project projectA = new Project();
        projectA.setName("Tenant A Project");
        projectA.setDescription("A's Project");
        projectA.setTenantId(tenantAId);
        projectA.setStatus(ProjectStatus.PLANNING);
        projectA.setCategory("Internal");
        projectA.setCreatedBy("userA");
        projectA.setCreatedAt("2024-01-01T10:00:00");
        projectRepository.save(projectA);

        String tenantBId = "2";
        tenantBToken = generateToken("userB@tenantB.com", 2L);

        Project projectB = new Project();
        projectB.setName("Tenant B Project");
        projectB.setDescription("B's Project");
        projectB.setTenantId(tenantBId);
        projectB.setStatus(ProjectStatus.IN_PROGRESS);
        projectB.setCategory("Client");
        projectB.setCreatedBy("userB");
        projectB.setCreatedAt("2024-01-01T11:00:00");
        Project savedProjectB = projectRepository.save(projectB);
        tenantBProjectId = savedProjectB.getId();

        Task taskB = new Task();
        taskB.setTitle("Tenant B Task");
        taskB.setDescription("B's Task");
        taskB.setStatus(TaskStatus.TODO);
        taskB.setProject(savedProjectB);
        taskB.setTenantId(tenantBId);
        Task savedTaskB = taskRepository.save(taskB);
        tenantBTaskId = savedTaskB.getId();
    }

    private String generateToken(String email, Long tenantId) {
        User user = new User(email, "password", Collections.emptyList());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
                Collections.emptyList());
        return "Bearer " + jwtProvider.generateToken(auth, tenantId);
    }

    @Test
    void testCrossTenantReadProjectFails() throws Exception {
        mockMvc.perform(get("/projects/" + tenantBProjectId)
                .header("Authorization", tenantAToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Project not found"));
    }

    @Test
    void testCrossTenantUpdateTaskFails() throws Exception {
        Task taskUpdates = new Task();
        taskUpdates.setTitle("Hacked Title");

        mockMvc.perform(patch("/tasks/" + tenantBTaskId)
                .header("Authorization", tenantAToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskUpdates)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task not found"));
    }

    @Test
    void testCrossTenantListingFiltered() throws Exception {
        mockMvc.perform(get("/projects")
                .header("Authorization", tenantAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Tenant A Project"))
                .andExpect(jsonPath("$[*].name", not(hasItem("Tenant B Project"))));

        mockMvc.perform(get("/tasks")
                .header("Authorization", tenantAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(jsonPath("$[*].title", not(hasItem("Tenant B Task"))));
    }
}
