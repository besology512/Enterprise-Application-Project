package com.workhub.reliability;

import com.workhub.model.Project;
import com.workhub.model.ProjectStatus;
import com.workhub.model.Task;
import com.workhub.model.TaskStatus;
import com.workhub.repository.ProjectRepository;
import com.workhub.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.OptimisticLockException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:concurrency-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "management.health.rabbit.enabled=false"
})
class TaskConcurrencyIntegrationTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long taskId;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        projectRepository.deleteAll();

        Project project = new Project();
        project.setTenantId("1");
        project.setName("Concurrent Project");
        project.setDescription("Concurrency proof");
        project.setStatus(ProjectStatus.PLANNING);
        project.setCategory("Internal");
        project.setCreatedBy("admin@tenant.com");
        project.setCreatedAt("2026-05-16T00:00:00");
        Project savedProject = projectRepository.save(project);

        Task task = new Task();
        task.setTenantId("1");
        task.setTitle("Original");
        task.setDescription("Concurrency proof task");
        task.setStatus(TaskStatus.TODO);
        task.setProject(savedProject);
        taskId = taskRepository.save(task).getId();
    }

    @Test
    void concurrentUpdatesToSameTaskProduceOneSuccessAndOneOptimisticLockConflict() throws Exception {
        CountDownLatch bothTransactionsLoadedTask = new CountDownLatch(2);
        CountDownLatch releaseBothTransactions = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger conflicts = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> firstUpdate = executor.submit(() -> updateTaskTitleConcurrently(
                    "Updated by worker 1", bothTransactionsLoadedTask, releaseBothTransactions, successes, conflicts));
            Future<?> secondUpdate = executor.submit(() -> updateTaskTitleConcurrently(
                    "Updated by worker 2", bothTransactionsLoadedTask, releaseBothTransactions, successes, conflicts));

            await(bothTransactionsLoadedTask);
            releaseBothTransactions.countDown();

            firstUpdate.get(10, TimeUnit.SECONDS);
            secondUpdate.get(10, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        Task finalTask = taskRepository.findById(taskId).orElseThrow();

        assertThat(successes.get()).isEqualTo(1);
        assertThat(conflicts.get()).isEqualTo(1);
        assertThat(finalTask.getVersion()).isEqualTo(1L);
        assertThat(finalTask.getTitle()).isIn(Set.of("Updated by worker 1", "Updated by worker 2"));
    }

    private void updateTaskTitleConcurrently(
            String title,
            CountDownLatch loadedLatch,
            CountDownLatch releaseLatch,
            AtomicInteger successes,
            AtomicInteger conflicts) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                Task task = taskRepository.findById(taskId).orElseThrow();
                loadedLatch.countDown();
                await(releaseLatch);
                task.setTitle(title);
                taskRepository.saveAndFlush(task);
            });
            successes.incrementAndGet();
        } catch (ObjectOptimisticLockingFailureException | OptimisticLockException | TransactionSystemException ex) {
            conflicts.incrementAndGet();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for concurrent test latch");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for concurrent test latch", ex);
        }
    }
}
