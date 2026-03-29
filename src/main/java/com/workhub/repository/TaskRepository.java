package com.workhub.repository;

import com.workhub.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByTenantId(Long tenantId);
    List<Task> findAllByProjectIdAndTenantId(Long projectId, Long tenantId);
}
