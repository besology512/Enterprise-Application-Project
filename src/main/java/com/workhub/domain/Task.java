package com.workhub.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Version
    private Long version;

    public enum Status {
        TODO, IN_PROGRESS, DONE
    }
}
