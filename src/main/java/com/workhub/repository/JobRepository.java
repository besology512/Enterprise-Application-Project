package com.workhub.repository;

import com.workhub.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByIdAndTenantId(Long id, String tenantId);
}
