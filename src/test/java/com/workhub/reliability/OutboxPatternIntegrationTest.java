package com.workhub.reliability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workhub.config.RabbitConfig;
import com.workhub.dto.JobPayload;
import com.workhub.dto.JobRequest;
import com.workhub.model.Job;
import com.workhub.model.OutboxMessage;
import com.workhub.model.OutboxStatus;
import com.workhub.repository.JobRepository;
import com.workhub.repository.OutboxMessageRepository;
import com.workhub.repository.ProcessedMessageRepository;
import com.workhub.service.JobService;
import com.workhub.service.OutboxPublisher;
import com.workhub.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:outbox-pattern-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "management.health.rabbit.enabled=false",
        "workhub.outbox.initial-delay-ms=600000"
})
class OutboxPatternIntegrationTest {

    @Autowired
    private JobService jobService;

    @Autowired
    private OutboxPublisher outboxPublisher;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setUp() {
        processedMessageRepository.deleteAll();
        outboxMessageRepository.deleteAll();
        jobRepository.deleteAll();
        TenantContext.setTenantId("1");
        reset(rabbitTemplate);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createReportJobStoresPendingOutboxMessageInSameDatabaseTransaction() throws Exception {
        JobRequest request = new JobRequest();
        request.setType("SUMMARY");
        request.setFormat("PDF");

        Job job = jobService.createReportJob(42L, request);

        assertThat(jobRepository.findById(job.getId())).isPresent();
        assertThat(outboxMessageRepository.findAll()).singleElement().satisfies(message -> {
            assertThat(message.getAggregateType()).isEqualTo("REPORT_JOB");
            assertThat(message.getAggregateId()).isEqualTo(job.getId());
            assertThat(message.getExchangeName()).isEqualTo(RabbitConfig.EXCHANGE);
            assertThat(message.getRoutingKey()).isEqualTo(RabbitConfig.ROUTING_KEY);
            assertThat(message.getStatus()).isEqualTo(OutboxStatus.PENDING);
            assertThat(message.getAttempts()).isZero();
        });

        OutboxMessage message = outboxMessageRepository.findAll().get(0);
        JobPayload payload = objectMapper.readValue(message.getPayload(), JobPayload.class);

        assertThat(payload).isEqualTo(new JobPayload(job.getId(), "1", 42L));
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void publisherSendsPendingOutboxMessageAndMarksItPublished() throws Exception {
        OutboxMessage message = pendingReportOutboxMessage(new JobPayload(7L, "1", 42L));
        OutboxMessage savedMessage = outboxMessageRepository.saveAndFlush(message);

        outboxPublisher.publishPendingMessages();

        ArgumentCaptor<JobPayload> payloadCaptor = ArgumentCaptor.forClass(JobPayload.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitConfig.EXCHANGE),
                eq(RabbitConfig.ROUTING_KEY),
                payloadCaptor.capture());

        assertThat(payloadCaptor.getValue()).isEqualTo(new JobPayload(7L, "1", 42L));
        OutboxMessage published = outboxMessageRepository.findById(savedMessage.getId()).orElseThrow();
        assertThat(published.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(published.getPublishedAt()).isNotNull();
        assertThat(published.getAttempts()).isZero();
        assertThat(published.getLastError()).isNull();
    }

    @Test
    void publisherKeepsMessagePendingForRetryWhenRabbitPublishFails() throws Exception {
        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), any(JobPayload.class));

        OutboxMessage savedMessage = outboxMessageRepository.saveAndFlush(
                pendingReportOutboxMessage(new JobPayload(8L, "1", 42L)));

        outboxPublisher.publishPendingMessages();

        OutboxMessage retryable = outboxMessageRepository.findById(savedMessage.getId()).orElseThrow();
        assertThat(retryable.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(retryable.getAttempts()).isEqualTo(1);
        assertThat(retryable.getLastError()).contains("RabbitMQ unavailable");
        assertThat(retryable.getPublishedAt()).isNull();
    }

    private OutboxMessage pendingReportOutboxMessage(JobPayload payload) throws Exception {
        return new OutboxMessage(
                "REPORT_JOB",
                payload.getJobId(),
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                objectMapper.writeValueAsString(payload));
    }
}
