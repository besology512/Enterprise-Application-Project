package com.workhub.service;

import com.workhub.dto.JobPayload;
import com.workhub.dto.JobRequest;
import com.workhub.model.Job;
import com.workhub.model.JobStatus;
import com.workhub.repository.JobRepository;
import com.workhub.config.RabbitConfig;
import com.workhub.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Job createReportJob(Long projectId, JobRequest request) {
        String tenantId = TenantContext.getTenantId();
        Job job = new Job();
        job.setTenantId(tenantId);
        job.setStatus(JobStatus.PENDING);
        job.setType(request.getType());

        Job savedJob = jobRepository.save(job);
        JobPayload payload = new JobPayload(savedJob.getId(), tenantId, projectId);

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                payload
        );

        return savedJob;
    }

    public Job getJobStatus(Long jobId) {
        String tenantId = TenantContext.getTenantId();
        return jobRepository.findByIdAndTenantId(jobId, tenantId)
                .orElseThrow(() -> new RuntimeException("Job not found or access denied"));
    }
}