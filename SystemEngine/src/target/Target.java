package target;

import exceptions.TargetNotExistException;
import exceptions.TargetsDependsOnEachOtherException;
import xmlfiles.generated.GPUPTarget;
import xmlfiles.generated.GPUPTargetDependencies;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Target implements Serializable, Cloneable {

    public enum Status {FROZEN, SKIPPED, WAITING, IN_PROCESS, FINISHED}
    public enum Result {SUCCESS, WARNING, FAILURE, SKIPPED}
    public enum Type   {LEAF, MIDDLE, ROOT, INDEPENDENT}

    private final String name;
    private final String ExtraData;
    private final Set<Target> requiredForSet;
    private final Set<Target> dependsOnSet;
    private Status runStatus;
    private Result runResult;
    private Type nodeType;
    private Duration targetTaskTime;
    private Instant targetTaskBegin,targetTaskEnd;
    private Instant startTimeInCurState;
    private boolean isVisited;
    private boolean isChosen;
    private final Set<Target> responsibleTargets;
    private String runLog;

    public Target(String name, String extraData)
    {
        this.name = name;
        if(extraData != null)
            this.ExtraData = extraData;
        else
            this.ExtraData = "";
        this.dependsOnSet = new HashSet<>();
        this.requiredForSet = new HashSet<>();
        this.responsibleTargets = new HashSet<>();
        determineInitialType();
        this.resetTarget();
    }

    public Target(String name)
    {
        this(name,"");
    }

    public void resetTarget(){
        this.isVisited = false;
        this.runResult = Result.SKIPPED;
        this.runStatus = Status.WAITING;
        this.responsibleTargets.clear();
    }

    public Target(GPUPTarget gpupTarget){
        this(gpupTarget.getName(),gpupTarget.getGPUPUserData());
    }

    public void setStartTimeInCurState() {
        this.startTimeInCurState = Instant.now();
    }

    public long getTimeInState(){
        return Duration.between(startTimeInCurState, Instant.now()).toMillis();
    }

    public boolean isChosen() { return isChosen; }

    public void setIsChosen(boolean isChosen) { this.isChosen = isChosen; }

    public Set<Target> getResponsibleTargets() {
        return responsibleTargets;
    }

    public Duration getTargetTaskTime() {
        return targetTaskTime;
    }

    public void setTargetTaskTime(Duration targetTaskTime) {
        this.targetTaskTime = targetTaskTime;
    }

    public Instant getTargetTaskBegin() {
        return targetTaskBegin;
    }

    public void setTargetTaskBegin(Instant targetTaskBegin) {
        this.targetTaskBegin = targetTaskBegin;
    }

    public Instant getTargetTaskEnd() {
        return targetTaskEnd;
    }

    public void setTargetTaskEnd(Instant targetTaskEnd) {
        this.targetTaskEnd = targetTaskEnd;
    }

    public Type getNodeType() {
        return nodeType;
    }

    public String getNodeTypeAsString() {
        switch (nodeType)
        {
            case LEAF:
                return "Leaf";
            case ROOT:
                return "Root";
            case MIDDLE:
                return "Middle";
            case INDEPENDENT:
                return "Independent";
        }
        return "";
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public void addDependencies(List<GPUPTargetDependencies.GPUGDependency> dependencyList, Map<String, Target> allTargets) throws TargetNotExistException, TargetsDependsOnEachOtherException {

        for (GPUPTargetDependencies.GPUGDependency dependency: dependencyList){
            if (!allTargets.containsKey(dependency.getValue()))
                throw new TargetNotExistException(dependency.getValue(),this);
            Target otherTarget = allTargets.get(dependency.getValue());

            if (dependency.getType().equals("dependsOn")) {
                if (otherTarget.dependsOnSet.contains(this)){
                    throw new TargetsDependsOnEachOtherException(this,otherTarget);
                }
                this.dependsOnSet.add(otherTarget);
                otherTarget.requiredForSet.add(this);
            }
            else if (dependency.getType().equals("requiredFor")) {
                if (otherTarget.requiredForSet.contains(this)){
                    throw new TargetsDependsOnEachOtherException(this,otherTarget);
                }
                this.requiredForSet.add(otherTarget);
                otherTarget.dependsOnSet.add(this);
            }
        }
    }

    public void determineInitialType() {
        if (dependsOnSet.isEmpty() && requiredForSet.isEmpty()) {
            nodeType = Type.INDEPENDENT;
        }
        else if (dependsOnSet.isEmpty() && !requiredForSet.isEmpty()) {
            nodeType = Type.LEAF;
        }
        else if (!dependsOnSet.isEmpty() && requiredForSet.isEmpty()) {
            nodeType = Type.ROOT;
        }
        else {
            nodeType = Type.MIDDLE;
        }
    }

    public void setStatusWaitingIfNeeded() {
        if (getAllDependsOnTargets().stream().filter(Target::isChosen).allMatch(target ->
                (target.getRunStatus().equals(Status.FINISHED) || target.getRunStatus().equals(Status.SKIPPED)))) {
            this.setStatus(Status.WAITING);
            this.setStartTimeInCurState();
        }
    }
    public void checkIfNeedsToBeSkipped() {
        this.responsibleTargets.clear();
        for (Target target : this.getAllDependsOnTargets()) {
            if (target.isChosen && target.getRunResult() == Result.FAILURE)
                responsibleTargets.add(target);
        }
        if(!responsibleTargets.isEmpty()) {
            this.setStatus(Status.SKIPPED);
            this.setResult(Result.SKIPPED);
        }
    }


    public void determineStatusBeforeTask() {
        if (getAllDependsOnTargets().stream().noneMatch(Target::isChosen))
            this.setStatus(Status.WAITING);
        else
            this.setStatus(Status.FROZEN);
    }

    /**
     *
     * @return set of targets that depends on this target.
     */
    public Set<Target> getAllRequiredForTargets() {
        Set<Target> aboveTargets = new HashSet<>();
        getAllRequiredForTargetsRec(this,aboveTargets);
        return aboveTargets;
    }

    private void getAllRequiredForTargetsRec(Target target , Set<Target> aboveTargets)
    {
        for (Target curTarget: target.getRequiredForSet()) {
            if(!aboveTargets.contains(curTarget)) {
                aboveTargets.add(curTarget);
                getAllRequiredForTargetsRec(curTarget, aboveTargets);
            }
        }
    }

    public Set<String> getAllRequiredForTargetsAsStrings() {
        Set<String> requiredForTargets = new HashSet<>();
        for(Target target: getAllRequiredForTargets()) {
            requiredForTargets.add(target.getName().toUpperCase());
        }
        return requiredForTargets;
    }

    public Set<Target> getAllDependsOnTargets() {
        Set<Target> belowTargets = new HashSet<>();
        getAllDependsOnTargetsRec(this,belowTargets);
        return belowTargets;
    }

    private void getAllDependsOnTargetsRec(Target target , Set<Target> belowTargets)
    {
        for (Target curTarget: target.getDependsOnSet()) {
            if(!belowTargets.contains(curTarget)) {
                belowTargets.add(curTarget);
                getAllDependsOnTargetsRec(curTarget, belowTargets);
            }
        }
    }

    public Set<String> getAllDependsOnTargetsAsStrings() {
        Set<String> dependsOnTargets = new HashSet<>();
        for(Target target: getAllDependsOnTargets()) {
            dependsOnTargets.add(target.getName().toUpperCase());
        }
        return dependsOnTargets;
    }

    public void setResult(Result res){
        this.runResult = res;
    }

    public void setStatus(Status st){
        this.runStatus = st;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Target target = (Target) o;
        if(name != null)
            return name.equalsIgnoreCase(target.name);
        else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public Status getRunStatus() {
        return runStatus;
    }

    public String getName() {
        return name;
    }

    public String getExtraData() {
        return ExtraData;
    }

    public Set<Target> getRequiredForSet() {
        return requiredForSet;
    }

    public Set<Target> getDependsOnSet() {
        return dependsOnSet;
    }

    public Result getRunResult() {
        return runResult;
    }

    public int getAmountOfDirectlyDependsOn() {
        return getDependsOnSet().size();
    }

    public int getAmountOfTotalDependsOn() {
        return getAllDependsOnTargets().size();
    }

    public int getAmountOfDirectlyRequiredFor() {
        return getRequiredForSet().size();
    }

    public int getAmountOfTotalRequiredFor() {
        return getAllRequiredForTargets().size();
    }

    public String getRunStatusAsString() {
        switch(runStatus) {
            case WAITING:
                return "Waiting";
            case FROZEN:
                return "Frozen";
            case SKIPPED:
                return "Skipped";
            case FINISHED:
                return "Finished";
            case IN_PROCESS:
                return "In process";
        }
        return "";
    }
    @Override
    public String toString(){
        return getName();
    }

    public String getRunResultAsString() {
        switch (runResult){
            case SUCCESS:
                return "Success";
            case WARNING:
                return "Warning";
            case FAILURE:
                return "Failure";
            default:
                return "";
        }
    }

    public static TargetForWorker extractTargetForWorkerFromTarget(Target target,String taskName,String taskType, Integer pricing) {
        TargetForWorker targetForWorker = new TargetForWorker(target.getName(),target.getExtraData(),taskName,taskType,pricing,target.getNodeType().name());
        targetForWorker.setTargetStatus(target.getRunStatus());
        targetForWorker.setTargetResult(target.getRunResult());
        return targetForWorker;
    }

    public String getRunLog() {
        return runLog;
    }

    public void setRunLog(String runLog) {
        this.runLog = runLog;
    }
}
