package com.workhub.reliability;

import com.workhub.model.Project;
import com.workhub.model.ProjectStatus;
import com.workhub.model.Task;
import com.workhub.model.TaskStatus;
import com.workhub.repository.ProjectRepository;
import com.workhub.repository.TaskRepository;
import com.workhub.service.ProjectService;
import com.workhub.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:rollback-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "management.health.rabbit.enabled=false"
})
class TransactionRollbackIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();
        TenantContext.setTenantId("1");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@tenant.com", null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }

    @Test
    void createProjectWithTasksRollsBackAllWritesWhenOneTaskFails() {
        Project project = project("Rollback Project");

        Task firstTask = task("First task");
        Task failingTask = task("FAIL");

        assertThatThrownBy(() -> projectService.createProjectWithTasks(project, List.of(firstTask, failingTask)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Task title cannot be FAIL");

        assertThat(projectRepository.findAll())
                .extracting(Project::getName)
                .doesNotContain("Rollback Project");
        assertThat(taskRepository.findAll())
                .extracting(Task::getTitle)
                .doesNotContain("First task", "FAIL");
    }

    private Project project(String name) {
        Project project = new Project();
        project.setName(name);
        project.setDescription("Rollback proof");
        project.setStatus(ProjectStatus.PLANNING);
        project.setCategory("Internal");
        return project;
    }

    private Task task(String title) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Rollback proof task");
        task.setStatus(TaskStatus.TODO);
        return task;
    }
}
