package com.workhub.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendReportRequest(ReportMessage message) {
        rabbitTemplate.convertAndSend(MessagingConfig.REPORT_EXCHANGE, MessagingConfig.REPORT_ROUTING_KEY, message);
    }
}
