package com.workhub.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    public static final String REPORT_QUEUE = "report-queue";
    public static final String REPORT_EXCHANGE = "report-exchange";
    public static final String REPORT_ROUTING_KEY = "report.routing.key";

    @Bean
    public Queue queue() {
        return new Queue(REPORT_QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(REPORT_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(REPORT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
