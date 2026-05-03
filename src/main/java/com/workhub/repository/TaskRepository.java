package com.workhub.repository;

import com.workhub.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByTenantId(String tenantId);
    Optional<Task> findByIdAndTenantId(Long id, String tenantId);
}
