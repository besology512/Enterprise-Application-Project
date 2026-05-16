package com.workhub.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_messages")
@Data
@NoArgsConstructor
public class OutboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private Long aggregateId;

    @Column(nullable = false)
    private String exchangeName;

    @Column(nullable = false)
    private String routingKey;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(nullable = false)
    private int attempts;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    public OutboxMessage(String aggregateType, Long aggregateId, String exchangeName, String routingKey, String payload) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
        this.payload = payload;
    }
}
