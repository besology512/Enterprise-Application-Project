package com.workhub.web;

import com.workhub.domain.Task;
import com.workhub.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @PatchMapping("/{id}")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long id, @RequestParam Task.Status status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(id, status));
    }
}
