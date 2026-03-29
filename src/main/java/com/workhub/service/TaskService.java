package com.workhub.service;

import com.workhub.domain.Task;
import com.workhub.repository.TaskRepository;
import com.workhub.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public Task createTask(Long projectId, String title) {
        Task task = Task.builder()
                .title(title)
                .status(Task.Status.TODO)
                .projectId(projectId)
                .tenantId(TenantContext.getTenantId())
                .build();
        return taskRepository.save(task);
    }

    public Task updateTaskStatus(Long id, Task.Status status) {
        Task task = taskRepository.findById(id)
                .filter(t -> t.getTenantId().equals(TenantContext.getTenantId()))
                .orElseThrow(() -> new RuntimeException("Task not found or access denied"));
        
        task.setStatus(status);
        return taskRepository.save(task);
    }

    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findAllByProjectIdAndTenantId(projectId, TenantContext.getTenantId());
    }
}
