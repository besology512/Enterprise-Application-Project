package com.workhub.repository;

import com.workhub.model.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {
    boolean existsByMessageId(String messageId);
}
