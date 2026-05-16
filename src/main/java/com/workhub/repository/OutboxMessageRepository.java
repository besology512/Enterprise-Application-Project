package com.workhub.repository;

import com.workhub.model.OutboxMessage;
import com.workhub.model.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
