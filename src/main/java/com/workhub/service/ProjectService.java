package com.workhub.service;

import com.workhub.exception.TenantAccessException;
import com.workhub.model.Project;
import com.workhub.model.Task;
import com.workhub.repository.ProjectRepository;
import com.workhub.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import com.workhub.tenant.TenantContext;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public Project createProject(Project p) {
        p.setTenantId(TenantContext.getTenantId());
        return projectRepository.save(p);
    }

    public List<Project> getAllProjects() {
        String currentTenant = TenantContext.getTenantId();
        return projectRepository.findByTenantId(currentTenant);
    }

    public Optional<Project> getProjectById(Long id) {
        Optional<Project> project = projectRepository.findById(id);

        // STRICT TENANT ISOLATION CHECK
        if (project.isPresent() && !project.get().getTenantId().equals(TenantContext.getTenantId())) {
            throw new TenantAccessException("Access Denied: This project belongs to another tenant.");
        }

        return project;
    }

    @Transactional
    public Project createProjectWithTasks(Project project, List<Task> tasks) {
        project.setTenantId(TenantContext.getTenantId());
        Project savedProject = projectRepository.save(project);

        if (tasks != null && !tasks.isEmpty()) {
            for (Task t : tasks) {
                if ("FAIL".equals(t.getTitle())) {
                    // Triggers the 400 Bad Request in the global handler
                    throw new IllegalArgumentException("Task title cannot be FAIL");
                }
                t.setProject(savedProject);
                taskRepository.save(t);
            }
        }

        return savedProject;
    }
}