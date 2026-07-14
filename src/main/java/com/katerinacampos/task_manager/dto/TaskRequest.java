package com.katerinacampos.task_manager.dto;

import com.katerinacampos.task_manager.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

	@NotBlank
	@Size(min = 3)
	private String title;

	private String description;
	private TaskStatus status;
}
