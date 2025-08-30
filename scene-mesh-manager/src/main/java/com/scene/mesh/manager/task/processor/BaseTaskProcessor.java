package com.scene.mesh.manager.task.processor;

import com.scene.mesh.manager.task.IGeneralTask;
import com.scene.mesh.manager.task.TaskStatus;
import com.scene.mesh.manager.task.TaskType;

public abstract class BaseTaskProcessor implements ITaskProcessor {

    private final TaskType taskType;

    private IGeneralTask task;

    public BaseTaskProcessor(TaskType taskType) {
        this.taskType = taskType;
    }

    @Override
    public TaskType getTaskType() {
        return this.taskType;
    }

    @Override
    public IGeneralTask getGeneralTask() {
        return this.task;
    }

    @Override
    public void process(IGeneralTask task) {
        this.task = task;
        if (task == null || task.getTaskStatus() == null
                || TaskStatus.successful.equals(task.getTaskStatus())) {
            return;
        }
        processTask(task);
    }

    protected abstract void processTask(IGeneralTask task);
}
