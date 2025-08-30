package com.scene.mesh.manager.dto;

import com.scene.mesh.manager.task.TaskType;
import lombok.Data;

import java.util.Map;

@Data
public class SubmittedTaskDTO {

    private TaskType taskType;

    private Map<String,Object> taskData;
}
