package com.katerinacampos.task_manager.controller;

import com.katerinacampos.task_manager.dto.TaskRequest;
import com.katerinacampos.task_manager.dto.TaskResponse;
import com.katerinacampos.task_manager.model.TaskStatus;
import com.katerinacampos.task_manager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Tareas", description = "CRUD de tareas del usuario autenticado")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Crear tarea")
    @PostMapping
    public ResponseEntity<TaskResponse> create(Authentication auth,
                                               @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.create(auth.getName(), request));
    }

    @Operation(summary = "Listar tareas", description = "Soporta paginación, ordenamiento y filtro por status")
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAll(
            Authentication auth,
            @Parameter(description = "Filtrar por estado: PENDING o COMPLETED")
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(taskService.getAll(auth.getName(), status, pageable));
    }

    @Operation(summary = "Obtener tarea por ID")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(auth.getName(), id));
    }

    @Operation(summary = "Actualizar tarea")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(Authentication auth,
                                               @PathVariable Long id,
                                               @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.update(auth.getName(), id, request));
    }

    @Operation(summary = "Eliminar tarea (soft delete)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long id) {
        taskService.delete(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Cambiar estado de la tarea", description = "Alterna entre PENDING y COMPLETED")
    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> changeStatus(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(taskService.changeStatus(auth.getName(), id));
    }
}