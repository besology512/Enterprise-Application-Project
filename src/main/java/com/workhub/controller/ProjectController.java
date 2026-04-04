package com.workhub.controller;

import com.workhub.model.Project;
import com.workhub.dto.ProjectCreationRequest;
import com.workhub.service.ProjectService;
import com.workhub.service.TaskService;
import com.workhub.model.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project p) {
        Project createdProj = projectService.createProject(p);
        return new ResponseEntity<>(createdProj, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new com.workhub.exception.ResourceNotFoundException("Project not found"));
        return ResponseEntity.ok(project);
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<Task> createTaskforProject(@PathVariable Long id, @Valid @RequestBody Task task) {
        Task createdTask = taskService.createTask(id, task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @PostMapping("/with-tasks")
    public ResponseEntity<Project> createProjectWithTasks(@Valid @RequestBody ProjectCreationRequest request) {

        Project createdProject = projectService.createProjectWithTasks(request.getProject(), request.getTasks());
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }
}