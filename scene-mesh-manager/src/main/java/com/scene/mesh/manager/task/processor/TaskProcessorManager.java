package com.scene.mesh.manager.task.processor;

import com.scene.mesh.manager.task.TaskType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskProcessorManager {

    private final Map<TaskType, ITaskProcessor> processorMap;

    public TaskProcessorManager(List<ITaskProcessor> processors) {
        processorMap = new HashMap<>();
        if (processors != null) {
            for (ITaskProcessor processor : processors) {
                registerTaskProcessor(processor);
            }
        }
    }

    public void registerTaskProcessor(ITaskProcessor processor) {
        processorMap.put(processor.getTaskType(), processor);
    }

    public ITaskProcessor getTaskProcessor(TaskType taskType) {
        return processorMap.get(taskType);
    }
}
