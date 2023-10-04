package task;

import dtos.CompilationTaskDto;
import dtos.GPUPTaskDto;
import dtos.SimulationTaskDto;
import target.Target;
import target.TargetGraph;
import task.compilation.CompilationTask;
import task.simulation.SimulationTask;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class ExecutorThread extends Thread{
    private final LinkedList<GPUPTask> tasksList;
    private final String taskName;
    private final String taskType;
    private final TasksManager tasksManager;

    /*---------------------Simulation parameters-----------------------------*/
    private double warningChance;
    private double successChance;
    private boolean isRandom;
    private int processTimeInMS;
    /*---------------------Compilation parameters----------------------------*/
    private String SourceFolderPath;
    private String DestFolderPath;
    /*-----------------------------------------------------------------------*/
    private Boolean isPaused = false;
    private Boolean isStopped = false;
    public Boolean isPauseDummy = false;
    /*-----------------------------------------------------------------------*/

    public ExecutorThread( String taskName, double warningChance, double successChance,
                           boolean isRandom, int processTimeInMS, boolean isIncremental, TasksManager tasksManager){
        this.taskName = taskName;
        this.warningChance = warningChance;
        this.successChance = successChance;
        this.isRandom = isRandom;
        this.processTimeInMS = processTimeInMS;
        this.tasksList = new LinkedList<GPUPTask>();
        this.tasksManager = tasksManager;
        this.taskType = "simulation";
        initTasksList(isIncremental);
    }

    public ExecutorThread(String taskName, String SourceFolderPath,
                          String DestFolderPath, boolean isIncremental, TasksManager tasksManager) {
        this.isStopped = false;
        this.tasksList = new LinkedList<GPUPTask>();
        this.taskName = taskName;
        this.SourceFolderPath = SourceFolderPath;
        this.DestFolderPath = DestFolderPath;
        this.tasksManager = tasksManager;
        this.taskType = "compilation";
        initTasksList(isIncremental);
    }
    private void initTasksList(boolean isIncremental){
        tasksList.clear();
        for(Target target : tasksManager.getTaskForServerSide(taskName).getTargetGraph().getTargetsToRunOnAndResetExtraData(isIncremental)) {
            if (target.getRunStatus().equals(Target.Status.WAITING)) {
                if(this.taskType.equalsIgnoreCase("simulation"))
                    tasksList.addFirst(new SimulationTask(taskName, processTimeInMS, isRandom, successChance, warningChance,
                            Target.extractTargetForWorkerFromTarget(target, taskName, taskType,
                                    tasksManager.getTaskForServerSide(taskName).getTargetGraph().getTaskPricing().get(TargetGraph.TaskType.SIMULATION))));
                else /*compilation*/
                    tasksList.addFirst(new CompilationTask(taskName, SourceFolderPath, DestFolderPath,
                            Target.extractTargetForWorkerFromTarget(target, taskName, taskType,
                                    tasksManager.getTaskForServerSide(taskName).getTargetGraph().getTaskPricing().get(TargetGraph.TaskType.COMPILATION))));
            }
            else {
                if(this.taskType.equalsIgnoreCase("simulation"))
                    tasksList.addLast(new SimulationTask(taskName, processTimeInMS, isRandom, successChance, warningChance,
                            Target.extractTargetForWorkerFromTarget(target, taskName, taskType,
                                    tasksManager.getTaskForServerSide(taskName).getTargetGraph().getTaskPricing().get(TargetGraph.TaskType.SIMULATION))));
                else /*compilation*/
                    tasksList.addLast(new CompilationTask(taskName, SourceFolderPath, DestFolderPath,
                            Target.extractTargetForWorkerFromTarget(target, taskName, taskType,
                                    tasksManager.getTaskForServerSide(taskName).getTargetGraph().getTaskPricing().get(TargetGraph.TaskType.COMPILATION))));
            }
        }
    }

    @Override
    public void run() {
        tasksManager.getTaskForServerSide(taskName).getTargetGraph().setTaskStartTime(Instant.now());
        tasksManager.getTaskForServerSide(taskName).getTargetGraph().getAllTargets().values().forEach(Target::setStartTimeInCurState);
        while (!tasksList.isEmpty()) {
            if (isStopped) { // break if stopped
                break;
            }

            while(isPaused) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            GPUPTask curTask = tasksList.poll();
            curTask.getTarget().updateData(tasksManager.getTaskForServerSide(taskName).getTargetGraph().getTarget(curTask.getTarget().getName()));
            if (curTask.target.getTargetStatus().equals(Target.Status.FROZEN)) { // target is frozen
                tasksList.addLast(curTask);
            } else {    // target is waiting (but maybe needs to be skipped)
                Target curTarget = tasksManager.getTaskForServerSide(taskName).getTargetGraph().getTarget(curTask.getTarget().getName());
                curTarget.checkIfNeedsToBeSkipped();
                curTask.getTarget().setTargetResult(curTarget.getRunResult());
                curTask.getTarget().setTargetStatus(curTarget.getRunStatus());
                if (curTask.getTarget().getTargetStatus().equals(Target.Status.SKIPPED)) {
                    curTask.getTarget().setRunLog(curTask.getTarget().getRunLog().concat("Target " + curTask.getTarget().getName() +
                            " is skipped because " + curTarget.getResponsibleTargets().toString() + " failed \n\n"));
                } else {
                    if(curTask.getTaskType().equalsIgnoreCase("simulation")) {
                        SimulationTask simulationTask = (SimulationTask)curTask;
                        SimulationTaskDto simulationTaskDto = new SimulationTaskDto(simulationTask);
                        tasksManager.addTaskReadyForWorker(simulationTaskDto);
                    }
                    else //compilation
                    {
                        CompilationTask compilationTask = (CompilationTask)curTask;
                        CompilationTaskDto compilationTaskDto = new CompilationTaskDto(compilationTask);
                        tasksManager.addTaskReadyForWorker(compilationTaskDto);
                    }
                }
            }
            tasksManager.getTaskForServerSide(taskName).getTargetGraph().refreshWaiting();
        }
        tasksManager.getTaskForServerSide(taskName).getTargetGraph().setTaskEndTime(Instant.now());
        tasksManager.getTaskForServerSide(taskName).getTargetGraph().
                setTotalTaskDuration(Duration.between(tasksManager.getTaskForServerSide(taskName).getTargetGraph().getTaskStartTime()
                        , tasksManager.getTaskForServerSide(taskName).getTargetGraph().getTaskEndTime()));
        if(isStopped)
            tasksManager.getTaskForServerSide(taskName).setTaskStatus("Stopped");
        else
            tasksManager.getTaskForServerSide(taskName).setTaskStatus("Finished");
    }


    public Boolean getPaused() {
        return isPaused;
    }

    public void setPaused(Boolean paused) {
        isPaused = paused;
    }

    public Boolean getStopped() {
        return isStopped;
    }

    public void setStopped(Boolean stopped) {
        isStopped = stopped;
    }

    public Boolean getIsPauseDummy() {
        return isPauseDummy;
    }
}