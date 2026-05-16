package com.workhub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhub.config.RabbitConfig;
import com.workhub.dto.JobPayload;
import com.workhub.model.OutboxMessage;
import com.workhub.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private static final String REPORT_JOB_AGGREGATE = "REPORT_JOB";

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    public OutboxMessage enqueueReportJob(JobPayload payload) {
        try {
            OutboxMessage message = new OutboxMessage(
                    REPORT_JOB_AGGREGATE,
                    payload.getJobId(),
                    RabbitConfig.EXCHANGE,
                    RabbitConfig.ROUTING_KEY,
                    objectMapper.writeValueAsString(payload));
            return outboxMessageRepository.save(message);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize report job outbox payload", ex);
        }
    }
}
