package com.workhub.dto;

import com.workhub.model.Project;
import com.workhub.model.Task;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ProjectCreationRequest {

    @NotNull(message = "Project details cannot be missing")
    @Valid
    private Project project;

    @NotNull(message = "Tasks list cannot be null")
    @Size(min = 1, message = "You must create at least one task with a new project")
    @Valid
    private List<Task> tasks;
}