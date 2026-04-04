package com.workhub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobPayload {
    private Long jobId;
    private String tenantId;
    private Long projectId;
}
