package com.workhub.reliability;

import com.workhub.config.RabbitConfig;
import com.workhub.dto.JobPayload;
import com.workhub.model.Job;
import com.workhub.model.JobStatus;
import com.workhub.repository.JobRepository;
import com.workhub.repository.ProcessedMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:messaging-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.rabbitmq.listener.simple.auto-startup=true",
        "management.health.rabbit.enabled=false"
})
class MessagingReliabilityIntegrationTest {

    @Container
    static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.12-management-alpine");

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", rabbit::getHost);
        registry.add("spring.rabbitmq.port", rabbit::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbit::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbit::getAdminPassword);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ProcessedMessageRepository processedMessageRepository;

    @BeforeEach
    void setUp() {
        processedMessageRepository.deleteAll();
        jobRepository.deleteAll();
    }

    @Test
    void reportJobMessageCompletesJobAndDuplicateMessageIsIgnoredByProcessedMessageTable() {
        Job job = new Job();
        job.setTenantId("1");
        job.setStatus(JobStatus.PENDING);
        job.setType("SUMMARY");
        Job savedJob = jobRepository.saveAndFlush(job);
        JobPayload payload = new JobPayload(savedJob.getId(), "1", 42L);

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, payload);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            Job completedJob = jobRepository.findById(savedJob.getId()).orElseThrow();
            assertThat(completedJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
            assertThat(completedJob.getResultUrl()).isEqualTo("/reports/42/report-" + savedJob.getId() + ".pdf");
            assertThat(processedMessageRepository.count()).isEqualTo(1L);
        });

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, payload);

        await().during(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Job completedJob = jobRepository.findById(savedJob.getId()).orElseThrow();
            assertThat(completedJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
            assertThat(processedMessageRepository.count()).isEqualTo(1L);
        });
    }
}
