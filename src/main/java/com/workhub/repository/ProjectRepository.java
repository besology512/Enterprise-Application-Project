package com.workhub.repository;

import com.workhub.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // equals SELECT * FROM projects WHERE tenant_id = ?
    List<Project> findByTenantId(String tenantId);
}