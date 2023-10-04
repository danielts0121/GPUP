package task;

import target.TargetGraph;

public class TaskForServerSide {
    private final String taskName;
    private final TargetGraph.TaskType taskType;
    private String taskStatus;
    private TargetGraph targetGraph;
    private Boolean canRunIncrementally;
    private Integer amountOfWorkers = 0;


    public TaskForServerSide(String taskName, TargetGraph.TaskType taskType, String taskStatus,TargetGraph targetGraph) {
        this.taskName = taskName;
        this.taskType = taskType;
        this.taskStatus = taskStatus;
        this.targetGraph = targetGraph;
        this.canRunIncrementally = false;
    }

    public String getTaskName() {
        return taskName;
    }

    public TargetGraph.TaskType getTaskType() {
        return taskType;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public TargetGraph getTargetGraph() {
        return targetGraph;
    }

    public Boolean getCanRunIncrementally() {
        return canRunIncrementally;
    }

    public void setCanRunIncrementally(Boolean canRunIncrementally) {
        this.canRunIncrementally = canRunIncrementally;
    }

    public void addWorker() { amountOfWorkers++; }

    public void removeWorker() { amountOfWorkers--; }

    public Integer getAmountOfWorkers() {
        return amountOfWorkers;
    }
}
