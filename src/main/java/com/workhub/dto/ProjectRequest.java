package com.workhub.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class ProjectRequest {
    @NotBlank
    private String name;
    private List<String> initialTasks;
}
