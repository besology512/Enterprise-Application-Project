package com.workhub.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_messages")
@Data
@NoArgsConstructor
public class ProcessedMessage {
    @Id
    @Column(nullable = false, unique = true)
    private String messageId;

    @Column(nullable = false)
    private String queueName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime processedAt;

    public ProcessedMessage(String messageId, String queueName) {
        this.messageId = messageId;
        this.queueName = queueName;
    }
}
