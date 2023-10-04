package dtos;

import target.Target;
import target.TargetGraph;
import java.util.Map;
import java.util.Set;

public class TaskDetailsDto {
    private final String taskName;
    private final TargetGraph.TaskType taskType;
    private final String graphName;
    private final String uploader;

    private Integer targets = 0;
    private Integer roots = 0;
    private Integer middles = 0;
    private Integer leaves = 0;
    private Integer independents = 0;
    private Integer totalPayment;
    private Integer totalWorkers;
    private String taskStatus;
    private Set<String> targetsToExecute;
    private Boolean canRunIncrementally;

    public TaskDetailsDto(String taskName, String creatorName, TargetGraph.TaskType taskType,Set<String> targetsToExecute ,TargetGraph targetGraph) {
        this.taskName = taskName;
        this.graphName = targetGraph.getGraphName();
        this.uploader = creatorName;
        this.taskType = taskType;
        this.totalWorkers = 0;
        this.taskStatus = "New";
        this.targetsToExecute = targetsToExecute;

        Map<String,Target> allTargets = targetGraph.getAllTargets();
        for (String target : targetsToExecute ) {
            switch(allTargets.get(target).getNodeType())
            {
                case ROOT:
                    this.roots++;
                    break;
                case MIDDLE:
                    this.middles++;
                    break;
                case LEAF:
                    this.leaves++;
                    break;
                case INDEPENDENT:
                    this.independents++;
                    break;
            }
            this.targets++;
        }

        Map<TargetGraph.TaskType, Integer> taskPrices = targetGraph.getTaskPricing();
        totalPayment = taskPrices.get(taskType) * this.targets;
    }

    public void addWorker() { this.totalWorkers++; }

    public void removeWorker() { this.totalWorkers--; }

    public void setTaskStatus(String status) { this.taskStatus = status; }

    public String getGraphName() {
        return this.graphName;
    }

    public String getUploader() {
        return this.uploader;
    }

    public Integer getRoots() {
        return this.roots;
    }

    public Integer getMiddles() {
        return this.middles;
    }

    public Integer getLeaves() {
        return this.leaves;
    }

    public Integer getIndependents() {
        return this.independents;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public Integer getTargets() {
        return this.targets;
    }

    public Integer getTotalPayment() {
        return this.totalPayment;
    }

    public Integer getTotalWorkers() {
        return this.totalWorkers;
    }

    public String getTaskStatus() {
        return this.taskStatus;
    }

    public TargetGraph.TaskType getTaskType() { return this.taskType; }

    public String getTaskTypeAsString() {
        if(taskType == TargetGraph.TaskType.SIMULATION)
            return "Simulation";
        else if(taskType == TargetGraph.TaskType.COMPILATION)
            return "Compilation";
        else
            return "Unknown";
    }

    public Boolean getCanRunIncrementally() {
        return canRunIncrementally;
    }

    public void setCanRunIncrementally(Boolean canRunIncrementally) {
        this.canRunIncrementally = canRunIncrementally;
    }
}
