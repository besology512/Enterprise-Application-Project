package com.workhub.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportMessage {
    private Long projectId;
    private Long tenantId;
    private String userEmail;
    private String correlationId;
}
