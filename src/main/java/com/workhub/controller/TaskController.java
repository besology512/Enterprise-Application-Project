package com.workhub.controller;

import com.workhub.model.Task;
import com.workhub.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PatchMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskUpdates) {

        Task updatedTask = taskService.updateTask(id, taskUpdates);
        return ResponseEntity.ok(updatedTask);
    }
}