package com.katerinacampos.task_manager.repository;

import com.katerinacampos.task_manager.model.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByUserId(Long userId);

	Optional<Task> findByIdAndUserId(Long id, Long userId);
}
