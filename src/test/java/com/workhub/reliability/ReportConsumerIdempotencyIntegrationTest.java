package com.workhub.reliability;

import com.workhub.config.RabbitConfig;
import com.workhub.dto.JobPayload;
import com.workhub.model.Job;
import com.workhub.model.JobStatus;
import com.workhub.repository.JobRepository;
import com.workhub.repository.ProcessedMessageRepository;
import com.workhub.service.ReportConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:consumer-idempotency-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "management.health.rabbit.enabled=false"
})
class ReportConsumerIdempotencyIntegrationTest {

    @Autowired
    private ReportConsumer reportConsumer;

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
    void duplicateReportPayloadIsIgnoredAfterFirstSuccessfulProcessing() throws Exception {
        Job job = new Job();
        job.setTenantId("1");
        job.setStatus(JobStatus.PENDING);
        job.setType("SUMMARY");
        Job savedJob = jobRepository.saveAndFlush(job);
        JobPayload payload = new JobPayload(savedJob.getId(), "1", 42L);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> firstProcessing = executor.submit(() -> reportConsumer.handleReportJob(payload));

            await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                Job completedJob = jobRepository.findById(savedJob.getId()).orElseThrow();
                assertThat(completedJob.getStatus()).isEqualTo(JobStatus.COMPLETED);
                assertThat(completedJob.getResultUrl()).isEqualTo("/reports/42/report-" + savedJob.getId() + ".pdf");
                assertThat(processedMessageRepository.existsByMessageId("report-job-" + savedJob.getId())).isTrue();
            });
            firstProcessing.get();

            reportConsumer.handleReportJob(payload);

            assertThat(processedMessageRepository.count()).isEqualTo(1L);
            assertThat(processedMessageRepository.findById("report-job-" + savedJob.getId())
                    .orElseThrow()
                    .getQueueName()).isEqualTo(RabbitConfig.QUEUE);
            assertThat(jobRepository.findById(savedJob.getId()).orElseThrow().getStatus())
                    .isEqualTo(JobStatus.COMPLETED);
        } finally {
            executor.shutdownNow();
        }
    }
}
