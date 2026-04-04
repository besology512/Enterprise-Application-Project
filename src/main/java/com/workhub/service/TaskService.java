package com.workhub.service;

import com.workhub.exception.ResourceNotFoundException;
import com.workhub.exception.TenantAccessException;
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
        Project project = projectService.getProjectById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        task.setProject(project);
        return taskRepository.save(task);
    }

    public Task updateTask(Long taskId, Task taskUpdates) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!existingTask.getProject().getTenantId().equals(TenantContext.getTenantId())) {
            throw new TenantAccessException("Access Denied: This task belongs to another tenant.");
        }

        if (taskUpdates.getTitle() != null) {
            existingTask.setTitle(taskUpdates.getTitle());
        }
        if (taskUpdates.getDescription() != null) {
            existingTask.setDescription(taskUpdates.getDescription());
        }
        if (taskUpdates.getStatus() != null) {
            existingTask.setStatus(taskUpdates.getStatus());
        }
        return taskRepository.save(existingTask);
    }
}