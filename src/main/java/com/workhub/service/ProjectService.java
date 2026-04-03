package com.workhub.service;

import com.workhub.model.Project;
import com.workhub.model.Task;
import com.workhub.repository.ProjectRepository;
import com.workhub.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public Project createProject(Project p) {
        return projectRepository.save(p);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }

    @Transactional
    public Project createProjectWithTasks(Project project, List<Task> tasks) {
        Project savedProject = projectRepository.save(project);

        if (tasks != null && !tasks.isEmpty()) {
            for (Task t : tasks) {
                if ("FAIL".equals(t.getTitle())) {
                    throw new RuntimeException("Task title cannot be FAIL");
                }
                t.setProject(savedProject);
                taskRepository.save(t);
            }
        }

        return savedProject;
    }
}
