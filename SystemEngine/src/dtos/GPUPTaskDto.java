package dtos;

import target.TargetForWorker;
import task.GPUPTask;

public class GPUPTaskDto {
    protected final String taskName;
    protected final TargetForWorker target;
    protected final String taskType;
    protected String workerName;

    public GPUPTaskDto(GPUPTask gpupTask){
        this.taskName = gpupTask.getTaskName();
        this.target = gpupTask.getTarget();
        this.taskType = gpupTask.getTaskType();
        this.workerName = gpupTask.getWorkerName();
    }

    public String getTaskName() {
        return taskName;
    }

    public TargetForWorker getTarget() {
        return target;
    }

    public String getTaskType() {
        return taskType;
    }

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }
}
