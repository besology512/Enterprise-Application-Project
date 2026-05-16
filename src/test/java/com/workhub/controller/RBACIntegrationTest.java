package com.workhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhub.model.Project;
import com.workhub.model.ProjectStatus;
import com.workhub.model.Task;
import com.workhub.model.TaskStatus;
import com.workhub.model.User;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RBACIntegrationTest {

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

        private String adminToken;
        private String userToken;

        @BeforeEach
        void setUp() {
                taskRepository.deleteAll();
                projectRepository.deleteAll();
                userRepository.deleteAll();

                User admin = User.builder()
                                .email("admin@tenant.com")
                                .password("password")
                                .tenantId(1L)
                                .roles(Set.of(User.Role.TENANT_ADMIN))
                                .build();
                userRepository.save(admin);
                adminToken = generateToken(admin);

                User user = User.builder()
                                .email("user@tenant.com")
                                .password("password")
                                .tenantId(1L)
                                .roles(Set.of(User.Role.TENANT_USER))
                                .build();
                userRepository.save(user);
                userToken = generateToken(user);
        }

        private String generateToken(User user) {
                List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                                .collect(Collectors.toList());

                org.springframework.security.core.userdetails.User principal = new org.springframework.security.core.userdetails.User(
                                user.getEmail(), user.getPassword(), authorities);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null,
                                authorities);
                return "Bearer " + jwtProvider.generateToken(auth, user.getTenantId());
        }

        // ── helpers ──────────────────────────────────────────────────────────────

        private Project savedProject() {
                Project p = new Project();
                p.setName("Test Project");
                p.setDescription("For tests");
                p.setTenantId("1");
                p.setStatus(ProjectStatus.PLANNING);
                p.setCategory("Internal");
                p.setCreatedBy("admin@tenant.com");
                p.setCreatedAt("2024-01-01T10:00:00");
                return projectRepository.save(p);
        }

        private Task savedTask(Project project) {
                Task t = new Task();
                t.setTitle("Test Task");
                t.setDescription("For tests");
                t.setStatus(TaskStatus.TODO);
                t.setProject(project);
                t.setTenantId("1");
                return taskRepository.save(t);
        }

        // ── 401 — no or invalid token ─────────────────────────────────────────

        @Test
        void testMissingTokenReturns401() throws Exception {
                Project project = new Project();
                project.setName("New Project");
                project.setDescription("Valid Description");
                project.setStatus(ProjectStatus.PLANNING);
                project.setCategory("General");
                project.setTenantId("1");

                mockMvc.perform(post("/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(project)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").exists())
                                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        void testInvalidTokenReturns401() throws Exception {
                mockMvc.perform(get("/projects")
                                .header("Authorization", "Bearer this.is.not.a.valid.jwt"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").exists())
                                .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        void testMalformedAuthHeaderReturns401() throws Exception {
                mockMvc.perform(get("/projects")
                                .header("Authorization", "NotBearer sometoken"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").exists())
                                .andExpect(jsonPath("$.status").value(401));
        }

        // ── 403 — authenticated but wrong role ───────────────────────────────

        @Test
        void testWrongRoleReturns403() throws Exception {
                Project project = new Project();
                project.setName("Forbidden Project");
                project.setDescription("Valid Description");
                project.setStatus(ProjectStatus.PLANNING);
                project.setCategory("General");
                project.setTenantId("1");

                mockMvc.perform(post("/projects")
                                .header("Authorization", userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(project)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").exists())
                                .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        void testUserCannotCreateProjectWithTasks() throws Exception {
                mockMvc.perform(post("/projects/with-tasks")
                                .header("Authorization", userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"project\":{\"name\":\"P\",\"description\":\"D\",\"status\":\"PLANNING\",\"category\":\"C\"},\"tasks\":[{\"title\":\"T\"}]}"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").exists())
                                .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        void testUserCannotAddTaskToProject() throws Exception {
                Project project = savedProject();

                mockMvc.perform(post("/projects/" + project.getId() + "/tasks")
                                .header("Authorization", userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"Task\",\"description\":\"Desc\",\"status\":\"TODO\"}"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").exists())
                                .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        void testUserCannotGenerateReport() throws Exception {
                Project project = savedProject();

                mockMvc.perform(post("/projects/" + project.getId() + "/generate-report")
                                .header("Authorization", userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"reportType\":\"SUMMARY\"}"))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.error").exists())
                                .andExpect(jsonPath("$.status").value(403));
        }

        // ── 200 / 201 — correct role, access granted ──────────────────────────

        @Test
        void testAdminUserReturns201() throws Exception {
                Project project = new Project();
                project.setName("Admin Project");
                project.setDescription("Created by admin");
                project.setStatus(ProjectStatus.PLANNING);
                project.setCategory("Internal");
                project.setCreatedBy("admin");
                project.setCreatedAt("2024-05-04T10:00:00");
                project.setTenantId("1");

                mockMvc.perform(post("/projects")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(project)))
                                .andExpect(status().isCreated());
        }

        @Test
        void testAdminCanAddTaskToProject() throws Exception {
                Project project = savedProject();

                mockMvc.perform(post("/projects/" + project.getId() + "/tasks")
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"New Task\",\"description\":\"Desc\",\"status\":\"TODO\"}"))
                                .andExpect(status().isCreated());
        }

        @Test
        void testUserCanListProjects() throws Exception {
                mockMvc.perform(get("/projects")
                                .header("Authorization", userToken))
                                .andExpect(status().isOk());
        }

        @Test
        void testAdminCanListProjects() throws Exception {
                mockMvc.perform(get("/projects")
                                .header("Authorization", adminToken))
                                .andExpect(status().isOk());
        }

        @Test
        void testUserCanListTasks() throws Exception {
                mockMvc.perform(get("/tasks")
                                .header("Authorization", userToken))
                                .andExpect(status().isOk());
        }

        @Test
        void testAdminCanListTasks() throws Exception {
                mockMvc.perform(get("/tasks")
                                .header("Authorization", adminToken))
                                .andExpect(status().isOk());
        }

        @Test
        void testUserCanUpdateTask() throws Exception {
                Task task = savedTask(savedProject());

                mockMvc.perform(patch("/tasks/" + task.getId())
                                .header("Authorization", userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"Updated\"}"))
                                .andExpect(status().isOk());
        }

        @Test
        void testAdminCanUpdateTask() throws Exception {
                Task task = savedTask(savedProject());

                mockMvc.perform(patch("/tasks/" + task.getId())
                                .header("Authorization", adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"Updated by admin\"}"))
                                .andExpect(status().isOk());
        }

        @Test
        void testUserCanGetProjectById() throws Exception {
                Project project = savedProject();

                mockMvc.perform(get("/projects/" + project.getId())
                                .header("Authorization", userToken))
                                .andExpect(status().isOk());
        }

        @Test
        void testAdminCanGetProjectById() throws Exception {
                Project project = savedProject();

                mockMvc.perform(get("/projects/" + project.getId())
                                .header("Authorization", adminToken))
                                .andExpect(status().isOk());
        }
}
