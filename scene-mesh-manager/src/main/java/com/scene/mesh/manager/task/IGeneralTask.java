package com.scene.mesh.manager.task;


import java.time.Instant;
import java.util.Map;

public interface IGeneralTask {

    String getTaskId();

    TaskType getTaskType();

    Object getPayloadVal(String key);

    void addPayloadEntry(String key, Object val);

    void setPayload(Map<String, Object> payload);

    TaskStatus getTaskStatus();

    Instant getCompleteTime();
}
