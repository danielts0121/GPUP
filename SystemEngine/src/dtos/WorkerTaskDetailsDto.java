package dtos;

public class WorkerTaskDetailsDto {
    private String taskName;
    private String taskStatus;
    private Integer amountOfWorkers;
    private Integer targetsDone;
    private Integer creditsEarned;


    public WorkerTaskDetailsDto(String taskName, String taskStatus, Integer amountOfWorkers, Integer targetsDone, Integer creditsEarned) {
        this.taskName = taskName;
        this.taskStatus = taskStatus;
        this.amountOfWorkers = amountOfWorkers;
        this.targetsDone = targetsDone;
        this.creditsEarned = creditsEarned;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public Integer getAmountOfWorkers() {
        return amountOfWorkers;
    }

    public Integer getTargetsDone() {
        return targetsDone;
    }

    public Integer getCreditsEarned() {
        return creditsEarned;
    }
}
