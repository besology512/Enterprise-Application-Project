package com.workhub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhub.dto.JobPayload;
import com.workhub.model.OutboxMessage;
import com.workhub.model.OutboxStatus;
import com.workhub.repository.OutboxMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "workhub.outbox.publisher.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {
    private final OutboxMessageRepository outboxMessageRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(
            fixedDelayString = "${workhub.outbox.publish-delay-ms:5000}",
            initialDelayString = "${workhub.outbox.initial-delay-ms:5000}")
    @Transactional
    public void publishPendingMessages() {
        List<OutboxMessage> pendingMessages =
                outboxMessageRepository.findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxMessage message : pendingMessages) {
            publishOne(message);
        }
    }

    private void publishOne(OutboxMessage message) {
        try {
            JobPayload payload = objectMapper.readValue(message.getPayload(), JobPayload.class);
            rabbitTemplate.convertAndSend(message.getExchangeName(), message.getRoutingKey(), payload);

            message.setStatus(OutboxStatus.PUBLISHED);
            message.setPublishedAt(LocalDateTime.now());
            message.setLastError(null);
            outboxMessageRepository.save(message);

            log.info("Published outbox message id={} aggregateType={} aggregateId={}",
                    message.getId(), message.getAggregateType(), message.getAggregateId());
        } catch (Exception ex) {
            message.setAttempts(message.getAttempts() + 1);
            message.setStatus(OutboxStatus.PENDING);
            message.setLastError(ex.getMessage());
            outboxMessageRepository.save(message);

            log.warn("Outbox publish failed for id={}, will retry", message.getId(), ex);
        }
    }
}
