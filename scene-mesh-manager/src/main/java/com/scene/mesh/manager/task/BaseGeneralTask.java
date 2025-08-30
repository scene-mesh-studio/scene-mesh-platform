package com.scene.mesh.manager.task;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public abstract class BaseGeneralTask implements IGeneralTask{

    private final String taskId;

    private final TaskType taskType;

    private Map<String, Object> payload;

    private TaskStatus taskStatus;

    private String statusMessage;

    private Instant completeTime;

    public BaseGeneralTask(String taskId, TaskType taskType) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.taskStatus = TaskStatus.unactivated;
        this.payload = new ConcurrentHashMap<>();
    }

    @Override
    public Object getPayloadVal(String key) {
        return this.payload.get(key);
    }

    @Override
    public void addPayloadEntry(String key, Object val) {
        this.payload.put(key, val);
    }

}
