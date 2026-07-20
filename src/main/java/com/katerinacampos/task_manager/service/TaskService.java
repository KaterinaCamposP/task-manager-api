package com.katerinacampos.task_manager.service;

import com.katerinacampos.task_manager.dto.TaskRequest;
import com.katerinacampos.task_manager.dto.TaskResponse;
import com.katerinacampos.task_manager.mapper.TaskMapper;
import com.katerinacampos.task_manager.model.Task;
import com.katerinacampos.task_manager.model.TaskStatus;
import com.katerinacampos.task_manager.model.User;
import com.katerinacampos.task_manager.repository.TaskRepository;
import com.katerinacampos.task_manager.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public TaskResponse create(String email, TaskRequest request) {
        User user = getUser(email);
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.PENDING)
                .user(user)
                .build();
        return taskMapper.toResponse(taskRepository.save(task));
    }

    public Page<TaskResponse> getAll(String email, TaskStatus status, Pageable pageable) {
        User user = getUser(email);
        Page<Task> page = (status != null)
                ? taskRepository.findByUserIdAndStatus(user.getId(), status, pageable)
                : taskRepository.findByUserId(user.getId(), pageable);
        return page.map(taskMapper::toResponse);
    }

    public TaskResponse getById(String email, Long id) {
        User user = getUser(email);
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return taskMapper.toResponse(task);
    }

    public TaskResponse update(String email, Long id, TaskRequest request) {
        User user = getUser(email);
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        return taskMapper.toResponse(taskRepository.save(task));
    }

    public void delete(String email, Long id) {
        User user = getUser(email);
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepository.delete(task);
    }

    public TaskResponse changeStatus(String email, Long id) {
        User user = getUser(email);
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(task.getStatus() == TaskStatus.PENDING ? TaskStatus.COMPLETED : TaskStatus.PENDING);
        return taskMapper.toResponse(taskRepository.save(task));
    }
}