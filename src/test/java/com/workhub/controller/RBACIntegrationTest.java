package com.workhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhub.model.Project;
import com.workhub.model.ProjectStatus;
import com.workhub.model.User;
import com.workhub.repository.ProjectRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RBACIntegrationTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ProjectRepository projectRepository;

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
                                .andExpect(status().isUnauthorized());
        }

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
                                .andExpect(status().isForbidden());
        }

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
}
