package com.workhub.service;

import com.workhub.domain.Project;
import com.workhub.domain.Task;
import com.workhub.dto.ProjectRequest;
import com.workhub.repository.ProjectRepository;
import com.workhub.repository.TaskRepository;
import com.workhub.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Transactional
    public Project createProjectWithTasks(ProjectRequest request) {
        Long tenantId = TenantContext.getTenantId();
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        Project project = Project.builder()
                .name(request.getName())
                .createdBy(currentUser)
                .tenantId(tenantId)
                .build();

        Project savedProject = projectRepository.save(project);

        if (request.getInitialTasks() != null) {
            for (String taskTitle : request.getInitialTasks()) {
                if (taskTitle.equalsIgnoreCase("fail")) {
                    throw new RuntimeException("Simulated transaction failure for rollback demonstration");
                }
                Task task = Task.builder()
                        .title(taskTitle)
                        .status(Task.Status.TODO)
                        .projectId(savedProject.getId())
                        .tenantId(tenantId)
                        .build();
                taskRepository.save(task);
            }
        }

        return savedProject;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAllByTenantId(TenantContext.getTenantId());
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .filter(p -> p.getTenantId().equals(TenantContext.getTenantId()))
                .orElseThrow(() -> new RuntimeException("Project not found or access denied"));
    }
}
