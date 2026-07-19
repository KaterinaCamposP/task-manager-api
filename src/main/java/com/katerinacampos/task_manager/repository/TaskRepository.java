package com.katerinacampos.task_manager.repository;

import com.katerinacampos.task_manager.model.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;


public interface TaskRepository extends JpaRepository<Task, Long> {

	List<Task> findByUserId(Long userId);

	Optional<Task> findByIdAndUserId(Long id, Long userId);

	@Modifying
    @Transactional
    @Query(value = "DELETE FROM tasks WHERE user_id = :userId", nativeQuery = true)
    void deleteAllByUserIdNative(Long userId);
}
