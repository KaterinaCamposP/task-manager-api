package com.katerinacampos.task_manager.controller;

import com.katerinacampos.task_manager.dto.TaskRequest;
import com.katerinacampos.task_manager.dto.TaskResponse;
import com.katerinacampos.task_manager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(Authentication auth,
                                               @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.create(auth.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAll(Authentication auth) {
        return ResponseEntity.ok(taskService.getAll(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(auth.getName(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(Authentication auth,
                                               @PathVariable Long id,
                                               @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        taskService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> changeStatus(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(taskService.changeStatus(auth.getName(), id));
    }
}