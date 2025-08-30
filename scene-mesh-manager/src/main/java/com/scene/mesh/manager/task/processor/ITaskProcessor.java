package com.scene.mesh.manager.task.processor;

import com.scene.mesh.manager.task.IGeneralTask;
import com.scene.mesh.manager.task.TaskType;

public interface ITaskProcessor {

    TaskType getTaskType();

    IGeneralTask getGeneralTask();

    void process(IGeneralTask task);
}
