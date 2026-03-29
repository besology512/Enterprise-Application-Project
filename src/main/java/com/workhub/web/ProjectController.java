package com.workhub.web;

import com.workhub.domain.Project;
import com.workhub.dto.ProjectRequest;
import com.workhub.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private com.workhub.messaging.ReportProducer reportProducer;

    @PostMapping
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Project> createProject(@Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.createProjectWithTasks(request));
    }

    @PostMapping("/{id}/generate-report")
    public ResponseEntity<String> generateReport(@PathVariable Long id, org.springframework.security.core.Authentication auth) {
        Project project = projectService.getProjectById(id);
        com.workhub.messaging.ReportMessage message = com.workhub.messaging.ReportMessage.builder()
                .projectId(project.getId())
                .tenantId(project.getTenantId())
                .userEmail(auth.getName())
                .correlationId(org.slf4j.MDC.get("correlationId"))
                .build();
        
        reportProducer.sendReportRequest(message);
        return ResponseEntity.accepted().body("Report generation triggered");
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }
}
