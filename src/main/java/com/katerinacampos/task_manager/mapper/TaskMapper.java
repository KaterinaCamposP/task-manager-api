package com.katerinacampos.task_manager.mapper;

import com.katerinacampos.task_manager.dto.TaskResponse;
import com.katerinacampos.task_manager.model.Task;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    TaskResponse toResponse(Task task);
}