package com.katerinacampos.task_manager.repository;

import com.katerinacampos.task_manager.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);
}