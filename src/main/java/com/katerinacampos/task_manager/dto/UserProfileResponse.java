package com.katerinacampos.task_manager.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

	private Long id;
	private String username;
	private String email;
	private LocalDateTime createdAt;
}
