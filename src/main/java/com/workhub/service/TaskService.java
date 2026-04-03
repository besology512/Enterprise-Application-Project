package com.workhub.service;

import com.workhub.model.Project;
import com.workhub.model.Task;
import com.workhub.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.workhub.tenant.TenantContext;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    public Task createTask(Long projectId, Task task) {
        // This will naturally use the patched getProjectById, so the tenant check happens automatically here too!
        Project project = projectService.getProjectById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        task.setProject(project);
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, Task taskUpdates) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // STRICT TENANT ISOLATION CHECK
        // Validate that the task's parent project belongs to the currently authenticated tenant
        if (!existingTask.getProject().getTenantId().equals(TenantContext.getTenantId())) {
            throw new RuntimeException("Access Denied: This task belongs to another tenant.");
        }

        existingTask.setTitle(taskUpdates.getTitle());
        existingTask.setDescription(taskUpdates.getDescription());
        existingTask.setStatus(taskUpdates.getStatus());
        return taskRepository.save(existingTask);
    }
}