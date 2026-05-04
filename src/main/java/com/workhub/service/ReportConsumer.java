package com.workhub.service;

import com.workhub.config.RabbitConfig;
import com.workhub.dto.JobPayload;
import com.workhub.model.Job;
import com.workhub.model.JobStatus;
import com.workhub.model.ProcessedMessage;
import com.workhub.repository.JobRepository;
import com.workhub.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportConsumer {

    private final JobRepository jobRepository;
    private final ProcessedMessageRepository processedMessageRepository;


    @RabbitListener(queues = RabbitConfig.QUEUE)
    @Transactional
    public void handleReportJob(JobPayload payload) {
        String idempotencyKey = "report-job-" + payload.getJobId();

        if (processedMessageRepository.existsByMessageId(idempotencyKey)) {
            return;
        }

        Job job = jobRepository.findByIdAndTenantId(payload.getJobId(), payload.getTenantId())
                .orElseThrow(() -> new RuntimeException(
                        "Job not found: id=" + payload.getJobId()
                                + " tenantId=" + payload.getTenantId()));

        job.setStatus(JobStatus.PROCESSING);
        jobRepository.save(job);

        try {
            Thread.sleep(2000);

            job.setStatus(JobStatus.COMPLETED);
            job.setResultUrl("/reports/" + payload.getProjectId()
                    + "/report-" + payload.getJobId() + ".pdf");
            jobRepository.save(job);
            processedMessageRepository.save(
                    new ProcessedMessage(idempotencyKey, RabbitConfig.QUEUE));

            log.info("Report job completed: jobId={}", payload.getJobId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            markFailed(job, "Report generation was interrupted");
        } catch (Exception e) {
            markFailed(job, e.getMessage());
            throw new RuntimeException("Report generation failed for jobId=" + payload.getJobId(), e);
        }
    }

    private void markFailed(Job job, String errorMessage) {
        job.setStatus(JobStatus.FAILED);
        job.setErrorMessage(errorMessage);
        jobRepository.save(job);
        log.error("Report job failed: jobId={}, reason={}", job.getId(), errorMessage);
    }
}
