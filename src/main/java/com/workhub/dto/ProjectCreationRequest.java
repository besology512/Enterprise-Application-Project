package com.workhub.dto;

import com.workhub.model.Project;
import com.workhub.model.Task;
import lombok.Data;
import java.util.List;

@Data
public class ProjectCreationRequest {
    private Project project;
    private List<Task> tasks;
}
