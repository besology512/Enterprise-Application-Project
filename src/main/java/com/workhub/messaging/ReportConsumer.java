package com.workhub.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ReportConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ReportConsumer.class);

    @RabbitListener(queues = MessagingConfig.REPORT_QUEUE)
    public void processReport(ReportMessage message) {
        org.slf4j.MDC.put("correlationId", message.getCorrelationId());
        try {
            logger.info("Processing report for project {} (Tenant: {}) requested by {}", 
                    message.getProjectId(), message.getTenantId(), message.getUserEmail());
            
            // Simulate report generation
            Thread.sleep(2000);
            logger.info("Report generation completed for project {}", message.getProjectId());
        } catch (InterruptedException e) {
            logger.error("Error processing report", e);
            Thread.currentThread().interrupt();
        } finally {
            org.slf4j.MDC.remove("correlationId");
        }
    }
}
