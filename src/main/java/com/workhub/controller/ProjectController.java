package com.workhub.controller;

import com.workhub.dto.JobRequest;
import com.workhub.model.Job;
import com.workhub.model.Project;
import com.workhub.dto.ProjectCreationRequest;
import com.workhub.service.JobService;
import com.workhub.service.ProjectService;
import com.workhub.service.TaskService;
import com.workhub.model.Task;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project and task management — write operations require TENANT_ADMIN role")
public class ProjectController {
    private final ProjectService projectService;
    private final TaskService taskService;
    private final JobService jobService;

    @Operation(summary = "Create a project", description = "TENANT_ADMIN only")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Project created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role")
    })
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project p) {
        Project createdProj = projectService.createProject(p);
        return new ResponseEntity<>(createdProj, HttpStatus.CREATED);
    }

    @Operation(summary = "List all projects for the current tenant")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @Operation(summary = "Get a project by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new com.workhub.exception.ResourceNotFoundException("Project not found"));
        return ResponseEntity.ok(project);
    }

    @Operation(summary = "Add a task to a project", description = "TENANT_ADMIN only")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Task created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/{id}/tasks")
    public ResponseEntity<Task> createTaskforProject(@PathVariable Long id, @Valid @RequestBody Task task) {
        Task createdTask = taskService.createTask(id, task);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @Operation(summary = "Create a project with its initial tasks", description = "TENANT_ADMIN only — requires at least one task")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Project and tasks created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role")
    })
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/with-tasks")
    public ResponseEntity<Project> createProjectWithTasks(@Valid @RequestBody ProjectCreationRequest request) {
        Project createdProject = projectService.createProjectWithTasks(request.getProject(), request.getTasks());
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @Operation(summary = "Trigger an async report generation job", description = "TENANT_ADMIN only — returns 202 Accepted with job ID")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Job accepted"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role"),
        @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    @PostMapping("/{id}/generate-report")
    public ResponseEntity<Job> generateReport(
            @PathVariable Long id,
            @RequestBody JobRequest request) {

        projectService.getProjectById(id)
                .orElseThrow(() -> new com.workhub.exception.ResourceNotFoundException("Project not found"));

        Job job = jobService.createReportJob(id, request);
        return new ResponseEntity<>(job, HttpStatus.ACCEPTED);
    }

    @Operation(summary = "Poll the status of an async job")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid token"),
        @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Job> getJobStatus(@PathVariable Long jobId) {
        Job job = jobService.getJobStatus(jobId);
        return ResponseEntity.ok(job);
    }
}
