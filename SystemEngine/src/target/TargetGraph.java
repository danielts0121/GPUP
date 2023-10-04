package target;

import exceptions.*;
import org.jetbrains.annotations.NotNull;
import xmlfiles.generated.GPUPConfiguration;
import xmlfiles.generated.GPUPDescriptor;
import xmlfiles.generated.GPUPTarget;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class TargetGraph implements Serializable {

    public enum TaskType {SIMULATION, COMPILATION};
    public enum pathDirection {DEPENDS_ON, REQUIRED_FOR};

    private final Map<String, Target> allTargets;
    private Map<TaskType, Integer>  taskPricing;
    private Duration totalTaskDuration;
    private Instant taskStartTime, taskEndTime;
    private String graphName;
    public String currentTaskLog;
    private String uploaderName;

    public String getCurrentTaskLog() { return currentTaskLog; }

    public TargetGraph(String name) {
        this.graphName = name;
        this.allTargets = new HashMap<>();
        currentTaskLog = "";
    }

    public static String getDurationAsString(Duration duration) {
        return String.format("%02d:%02d:%02d", duration.toHours(),
                duration.toMinutes() % 60,
                duration.getSeconds() - (duration.toHours() * 3600));
    }

    public Map<TaskType, Integer> getTaskPricing() {
        return taskPricing;
    }

    public void setTaskPricing(Map<TaskType, Integer> taskPricing) {
        this.taskPricing = taskPricing;
    }

    public String getGraphName() {
        return graphName;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public void InitializeTypes() {
        for (Target target : allTargets.values()) {
            target.determineInitialType();
        }
    }

    public Set<Target> getTargetsToRunOnAndResetExtraData(boolean isIncremental) {
        Set<Target> ChosenTargets;
        currentTaskLog = "";
        if (isIncremental) {
            ChosenTargets = setGraphToRunIncrementallyAndGetChosenTargets();
        }
        else {
            allTargets.values().forEach(Target::resetTarget);
            ChosenTargets = allTargets.values().stream().filter(Target::isChosen).collect(Collectors.toSet());
            for (Target target : ChosenTargets)
                target.determineStatusBeforeTask();
        }
        return ChosenTargets;
    }

    @NotNull
    public Set<Target> setGraphToRunIncrementallyAndGetChosenTargets() {
        Set<Target> ChosenTargets;
        ChosenTargets = allTargets.values().stream().filter(Target::isChosen).filter
                (target -> (target.getRunResult().equals(Target.Result.FAILURE) ||
                        target.getRunResult().equals(Target.Result.SKIPPED))).collect(Collectors.toSet());

        Set<Target> SucceededTargets = allTargets.values().stream().filter(Target::isChosen).filter
                (target -> (target.getRunResult().equals(Target.Result.SUCCESS) ||
                        target.getRunResult().equals(Target.Result.WARNING))).collect(Collectors.toSet());

        SucceededTargets.forEach(target -> {target.setIsChosen(false);});

        for (Target target : ChosenTargets)
            target.determineStatusBeforeTask();

        SucceededTargets.forEach(target -> {target.setIsChosen(true);});
        return ChosenTargets;
    }

    public void resetGraph() {
        InitializeTypes();
        resetTargets();
    }

    private void resetTargets() {
        for (Target target : allTargets.values()) {
            target.resetTarget();
        }
    }

    public void refreshWaiting(){
        for (Target target: allTargets.values().stream().filter(Target::isChosen).
                filter(target -> (target.getRunStatus().equals(Target.Status.FROZEN))).collect(Collectors.toSet())) {
            target.setStatusWaitingIfNeeded();
        }
    }

    public void addTargetToGraph(Target target) {
        target.determineInitialType();
        allTargets.put(target.getName().toUpperCase(), target);
    }

    /**
     * Checks if the task finished running on all the target graph, all targets are frozen or finished.
     * @return
     */
    public Boolean isTaskFinished() {
        return getAllTargets().values().stream().filter(Target::isChosen).allMatch(target ->
                (target.getRunStatus() == Target.Status.FINISHED || target.getRunStatus() == Target.Status.SKIPPED));
    }

    public Integer howMuchAreChosen() {
        return getAllTargets().values().stream().filter(Target::isChosen).collect(Collectors.toSet()).size();
    }

    public Integer howMuchAreFinishedOrSkipped() {
        return getAllTargets().values().stream().filter(Target::isChosen)
                .filter(target -> target.getRunStatus().equals(Target.Status.FINISHED) ||
                        target.getRunStatus().equals(Target.Status.SKIPPED)).collect(Collectors.toSet()).size();
    }

    public boolean didAllTargetsSucceed(){
        return allTargets.values().stream().filter(Target::isChosen).allMatch(target ->
                (target.getRunResult() == Target.Result.SUCCESS || target.getRunResult() == Target.Result.WARNING));
    }

    public Set<Target> getWaitingSet() {
        return allTargets.values().stream().filter(target ->
                (target.getRunStatus().equals(Target.Status.WAITING))).collect(Collectors.toSet());
    }

    public Map<String, Target> getAllTargets() {
        return allTargets;
    }

    public Duration getTotalTaskDuration() {
        return totalTaskDuration;
    }

    public void setTotalTaskDuration(Duration totalTaskDuration) {
        this.totalTaskDuration = totalTaskDuration;
    }

    public Instant getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(Instant taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public Instant getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(Instant taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    public Boolean getCanRunIncrementally() {
        return !didAllTargetsSucceed();
    }

    public Target getTarget(String targetName) {
        return allTargets.get(targetName.toUpperCase());
    }

    public Set<List<Target>> getAllPathsFromTwoTargets(Target fromTarget, Target toTarget, pathDirection direction) {
        Set<List<Target>> pathsSet = new HashSet<>();
        List<Target> currentPath = new ArrayList<>();
        getAllPathsFromTwoTargetsRec(fromTarget,toTarget,direction,pathsSet,currentPath);
        return pathsSet;
    }
    private void getAllPathsFromTwoTargetsRec(Target fromTarget, Target toTarget,pathDirection direction, Set<List<Target>> pathsSet,  List<Target> currentPath)
    {
        if(fromTarget.isVisited())
            return;

        currentPath.add(fromTarget);

        if(fromTarget.equals(toTarget)) {
            pathsSet.add(new ArrayList<>(currentPath));
        }
        else {
            fromTarget.setVisited(true);
            if(direction == pathDirection.DEPENDS_ON) {
                for (Target target : fromTarget.getDependsOnSet()) {
                    getAllPathsFromTwoTargetsRec(target, toTarget, direction, pathsSet, currentPath);
                }
            }
            else {
                for (Target target : fromTarget.getRequiredForSet()) {
                    getAllPathsFromTwoTargetsRec(target, toTarget, direction, pathsSet, currentPath);
                }
            }
            fromTarget.setVisited(false);
        }

        currentPath.remove(currentPath.size()-1); // remove last added target from path
    }

    public List<Target> checkIfATargetIsInACircleAndReturnCircle(Target target)
    {
        List<Target> currentPath = new ArrayList<>();

        boolean circleFound = checkIfATargetIsInACircleAndReturnCircleRec(target,target,currentPath);
        if(!circleFound)
            return null;

        return currentPath;
    }

    private boolean checkIfATargetIsInACircleAndReturnCircleRec(Target fromTarget, Target curTarget,List<Target> currentPath)
    {
        if(curTarget.equals(fromTarget)&&curTarget.isVisited()) {
            currentPath.add(curTarget);
            return true;
        }

        if(curTarget.isVisited())
            return false;

        currentPath.add(curTarget);

        curTarget.setVisited(true);

        boolean circleFound = false;
        for (Target target: curTarget.getRequiredForSet() ) {
            if(!circleFound) {
                circleFound = checkIfATargetIsInACircleAndReturnCircleRec(fromTarget,target,currentPath);
            }
        }
        if(!circleFound)
            currentPath.remove(currentPath.size()-1); // remove last added target from path
        curTarget.setVisited(false);
        return circleFound;
    }

    public static TargetGraph createTargetGraphFromXml(Path filePath) throws Exception {
        File file = new File(filePath.toString());
        JAXBContext jaxbContext = JAXBContext.newInstance(GPUPDescriptor.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        GPUPDescriptor gpupDescriptor = (GPUPDescriptor) jaxbUnmarshaller.unmarshal(file);

        GPUPConfiguration gpupConfiguration = gpupDescriptor.getGPUPConfiguration();
        TargetGraph targetGraph = new TargetGraph(gpupConfiguration.getGPUPGraphName());
        Map<TaskType, Integer>  taskPricing = new HashMap<>();
        for (GPUPConfiguration.GPUPPricing.GPUPTask task:gpupConfiguration.getGPUPPricing().getGPUPTask()) {
            TaskType taskType;
            if(task.getName().equalsIgnoreCase("simulation"))
                taskType = TaskType.SIMULATION;
            else
                taskType = TaskType.COMPILATION;
            taskPricing.put(taskType,task.getPricePerTarget());
        }
        targetGraph.setTaskPricing(taskPricing);

        for (GPUPTarget gpupTarget : gpupDescriptor.getGPUPTargets().getGPUPTarget()) {
            Target target = new Target(gpupTarget);
            if (targetGraph.allTargets.containsKey(target.getName().toUpperCase())){
                throw new TargetAppearTwiceException(target);
            }
            targetGraph.addTargetToGraph(target);
        }

        for (GPUPTarget gpupTarget : gpupDescriptor.getGPUPTargets().getGPUPTarget()) {
            if (gpupTarget.getGPUPTargetDependencies() != null) {
                targetGraph.getTarget(gpupTarget.getName()).addDependencies(gpupTarget.getGPUPTargetDependencies().getGPUGDependency(), targetGraph.allTargets);
            }
        }
        targetGraph.resetGraph();
        return targetGraph;
    }

    public int getAmountOfTargets() {
        return allTargets.size();
    }

    public int getAmountOfRoots() {
        return (int) allTargets.values().stream().filter(target -> (target.getNodeType()) == Target.Type.ROOT).count();
    }

    public int getAmountOfMiddles() {
        return (int) allTargets.values().stream().filter(target -> (target.getNodeType()) == Target.Type.MIDDLE).count();
    }

    public int getAmountOfLeaves() {
        return (int) allTargets.values().stream().filter(target -> (target.getNodeType()) == Target.Type.LEAF).count();
    }

    public int getAmountOfIndependent() {
        return (int) allTargets.values().stream().filter(target -> (target.getNodeType()) == Target.Type.INDEPENDENT).count();
    }

    public ArrayList<String> getAllPathsFromTwoTargetsAsStrings (String source, String destination, pathDirection direction) {
        Set<List<Target>> paths = getAllPathsFromTwoTargets(getTarget(source),getTarget(destination),direction);
        ArrayList<String> stringPaths = new ArrayList<>();
        for (List<Target> path: paths) {
            stringPaths.add(returnPathAsString(path));
        }
        return stringPaths;
    }

    public String checkIfTargetIsInACircleAndReturnCircleAsString(String target) {
        List<Target> circle = checkIfATargetIsInACircleAndReturnCircle(getTarget(target));
        if(circle == null)
            return "Target is not in a circle";
        else
            return returnPathAsString(circle);
    }

    private String returnPathAsString(List<Target> path) {
        String StringPath = "";
        char rightArrow = '\u2192';
        int i = 0;
        for (Target target : path) {
            if(i==0)
                StringPath += target.getName().toUpperCase();
            else
                StringPath = StringPath + ' ' + rightArrow + ' ' + target.getName().toUpperCase();
            i++;
        }
        return StringPath;
    }

    public void markTargetsAsChosen(Set<String> chosenTargets) {
        clearChosenTargets();
        for (String curTarget : chosenTargets) {
            getTarget(curTarget).setIsChosen(true);
        }
    }

    public void copyStatusAndResult(TargetGraph oldTargetGraph) {
        allTargets.forEach((key, target) -> {
            target.setStatus(oldTargetGraph.allTargets.get(key).getRunStatus());
            target.setResult(oldTargetGraph.allTargets.get(key).getRunResult());
        } );
    }

    private void clearChosenTargets(){
        for (Target target: allTargets.values()){
            target.setIsChosen(false);
        }
    }

    public void updateTargetsStatusAndResult(TargetForWorker targetForWorker) {
        Target target = getTarget(targetForWorker.getName());
        target.setStatus(targetForWorker.getTargetStatus());
        target.setResult(targetForWorker.getTargetResult());
        target.setRunLog(targetForWorker.getRunLog());
    }

    public Set<Target> getAllChosenTargets() {
        return allTargets.values().stream().filter(Target::isChosen).collect(Collectors.toSet());
    }
}
