package target;

public class TargetForWorker {
    private String name;
    private String targetId;
    private String result;
    private String status;
    private String runLog;
    private String extraData;
    private String taskName;
    private String taskType;
    private Integer pricing;
    private String nodeType;


    public TargetForWorker(String name, String extraData, String taskName,String taskType ,Integer pricing, String nodeType) {
        this.name = name;
        this.targetId = name + "("+ taskName + ")";
        this.extraData = extraData;
        this.runLog = "";
        this.taskName = taskName;
        this.taskType = taskType;
        this.pricing = pricing;
        this.nodeType = nodeType;
    }

    public String getName() {
        return name;
    }

    public Target.Result getTargetResult() {
        return Target.Result.valueOf(result.toUpperCase());
    }

    public void setTargetResult(Target.Result result) {
        this.result = result.name();
    }

    public String getResult(){
        return result;
    }

    public String getStatus() {
        return status;
    }

    public String getRunLog() {
        return runLog;
    }

    public void setRunLog(String runLog) {
        this.runLog = runLog;
    }

    public Target.Status getTargetStatus() {
        return Target.Status.valueOf(status.toUpperCase());
    }

    public void setTargetStatus(Target.Status status) {
        this.status = status.name();
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExtraData() {
        return extraData;
    }

    public String getTaskName() {
        return taskName;
    }

    public Integer getPricing() {
        return pricing;
    }

    public void updateData(Target target) {
        this.setTargetStatus(target.getRunStatus());
        this.setTargetResult(target.getRunResult());
    }

    public String getNodeType() { return nodeType; }

    public String getTargetId() {
        return targetId;
    }

    public String getTaskType() {
        return taskType;
    }
}
