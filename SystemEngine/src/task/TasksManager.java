package task;

import dtos.GPUPTaskDto;
import dtos.TaskDetailsDto;
import target.TargetForWorker;
import target.TargetGraph;
import task.compilation.CompilationParameters;
import task.compilation.CompilationTaskInformation;
import task.simulation.SimulationParameters;
import task.simulation.SimulationTaskInformation;
import worker.taskmanagment.TaskHistoryDto;

import java.util.*;
import java.util.stream.Collectors;

public class TasksManager {

    private static final Map<String, SimulationTaskInformation> simulationTasksMap = new HashMap<>();
    private static final Map<String, CompilationTaskInformation> compilationTasksMap = new HashMap<>();
    private static final Map<String, Set<String>> usersTasks = new HashMap<>();
    private static final Set<String> listOfAllTasks = new HashSet<>();
    private static final Map<String, TaskDetailsDto> taskDetailsDTOMap = new HashMap<>();
    private static final Map<String, TaskForServerSide> taskForServerSideMap = new HashMap<>();
    private static final Map<String, ExecutorThread> taskExecutorThreadMap = new HashMap<>();
    private static final LinkedList<GPUPTaskDto> tasksThatAreReadyForWorkersList = new LinkedList<>();

    /*----------------------------------------------Worker data members---------------------------------------------------*/

    /* The first key is the worker name, the second key is the task name, the value is the worker history with that task. */
    private static final Map<String, Map<String, TaskHistoryDto>> tasksHistoryMapForWorker = new HashMap<>();

    /* The first key is the worker name, the second key is the target name, the value is the target's dto.*/
    private static final Map<String, Map<String, TargetForWorker>> workersTargetMaps = new HashMap<>();

    public void addWorker (String workerName){
        if (!workersTargetMaps.containsKey(workerName))
            workersTargetMaps.put(workerName,new HashMap<>());
        if (!tasksHistoryMapForWorker.containsKey(workerName))
            tasksHistoryMapForWorker.put(workerName, new HashMap<>());
    }

    public synchronized boolean isTaskExists(String taskName) {
        return simulationTasksMap.containsKey(taskName.toLowerCase()) || compilationTasksMap.containsKey(taskName.toLowerCase());
    }
    public synchronized boolean isSimulationTask(String taskName) { return simulationTasksMap.containsKey(taskName.toLowerCase()); }
    public synchronized boolean isCompilationTask(String taskName) { return compilationTasksMap.containsKey(taskName.toLowerCase()); }

    public synchronized CompilationTaskInformation getCompilationTaskInformation(String taskName) {
        return compilationTasksMap.get(taskName.toLowerCase());
    }
    public synchronized SimulationTaskInformation getSimulationTaskInformation(String taskName) {
        return simulationTasksMap.get(taskName.toLowerCase());
    }

    public synchronized void addSimulationTask(SimulationTaskInformation newTask) {
        simulationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator().toLowerCase(), newTask.getTaskName());
    }
    public synchronized void addCompilationTask(CompilationTaskInformation newTask) {
        compilationTasksMap.put(newTask.getTaskName().toLowerCase(), newTask);
        listOfAllTasks.add(newTask.getTaskName());

        addUserTask(newTask.getTaskCreator().toLowerCase(), newTask.getTaskName());
    }
    public synchronized void addUserTask(String taskCreator, String taskName) {
        if(!usersTasks.containsKey(taskCreator))
            usersTasks.put(taskCreator, new HashSet<>());

        usersTasks.get(taskCreator).add(taskName);
    }

    public synchronized Set<String> getAllTaskList()
    {
        return listOfAllTasks;
    }
    public synchronized Set<String> getUserTaskList(String userName)
    {
        return usersTasks.get(userName.toLowerCase());
    }
    public synchronized Set<String> getOnlineTaskList() {
        return listOfAllTasks.stream()
                .filter(task -> (!taskForServerSideMap.get(task).getTaskStatus()
                        .equalsIgnoreCase("finished"))&& (!taskForServerSideMap.get(task).getTaskStatus()
                            .equalsIgnoreCase("new"))).collect(Collectors.toSet());
    }

    public synchronized void addTaskDetailsDTO(String taskName, String creatorName,
                                               TargetGraph.TaskType taskType,Set<String> targetsToExecute, TargetGraph targetGraph)
    {
        taskDetailsDTOMap.put(taskName.toLowerCase(), new TaskDetailsDto(taskName, creatorName, taskType, targetsToExecute, targetGraph));
    }
    public synchronized TaskDetailsDto getTaskDetailsDTO(String taskName)
    {
        return taskDetailsDTOMap.get(taskName.toLowerCase());
    }

    public synchronized void addTaskForServerSide(String taskName, TargetGraph.TaskType taskType, String taskStatus, TargetGraph targetGraph){
        taskForServerSideMap.put(taskName.toLowerCase(),(new TaskForServerSide(taskName, taskType, taskStatus, targetGraph)));
    }
    public synchronized  TaskForServerSide getTaskForServerSide(String taskName) {
        return taskForServerSideMap.get(taskName.toLowerCase());
    }

    public synchronized void addTaskExecutorThread(String taskName) {
        if(isSimulationTask(taskName)) {
            SimulationParameters parameters = getSimulationTaskInformation(taskName).getSimulationParameters();
            taskExecutorThreadMap.put(taskName.toLowerCase(),new ExecutorThread(taskName,
                                         parameters.getSuccessWithWarnings(), parameters.getSuccessRate(),
                                                parameters.isRandom(),parameters.getProcessingTime(),
                    getSimulationTaskInformation(taskName).isIncremental(), this));
        }
        else if(isCompilationTask(taskName)) {
            CompilationParameters parameters = getCompilationTaskInformation(taskName).getCompilationParameters();
            taskExecutorThreadMap.put(taskName.toLowerCase(),new ExecutorThread(taskName,
                                parameters.getSourcePath(),parameters.getDestinationPath(),
                    getCompilationTaskInformation(taskName).isIncremental(),this));
        }

        taskExecutorThreadMap.get(taskName.toLowerCase()).start();
    }

    public synchronized void addTaskReadyForWorker(GPUPTaskDto task) {
        tasksThatAreReadyForWorkersList.addLast(task);
    }

    public synchronized GPUPTaskDto pollTaskReadyForWorker(Set<String> tasksThatWorkerIsSignedTo) {
        List<GPUPTaskDto> filteredList = tasksThatAreReadyForWorkersList.stream().filter(gpupTaskDto -> tasksThatWorkerIsSignedTo.contains(gpupTaskDto.getTaskName())).collect(Collectors.toList());
        if(!filteredList.isEmpty()) {
            tasksThatAreReadyForWorkersList.remove(filteredList.get(0));
            return filteredList.get(0);
        }
        else //filtered list is empty
            return null;
    }

    public synchronized void updateTargetsStatusAndResult(TargetForWorker target) {
        taskForServerSideMap.get(target.getTaskName()).getTargetGraph().updateTargetsStatusAndResult(target);
    }

    public ExecutorThread getTaskExecutorThread(String taskName) {
        return taskExecutorThreadMap.get(taskName);
    }

    public void createTaskHistoryForWorker(String workerName,String taskName) {
        if(!tasksHistoryMapForWorker.containsKey(workerName))
            tasksHistoryMapForWorker.put(workerName, new HashMap<>());
        if(!tasksHistoryMapForWorker.get(workerName).containsKey(taskName))
            tasksHistoryMapForWorker.get(workerName).put(taskName,new TaskHistoryDto(taskName));
    }
    public TaskHistoryDto getTaskHistoryForWorker(String workerName, String taskName) {
        return tasksHistoryMapForWorker.get(workerName).get(taskName);
    }

    public void addTargetToWorkerMap(String workerName, TargetForWorker targetForWorker) {
        if(!workersTargetMaps.containsKey(workerName))
            workersTargetMaps.put(workerName,new HashMap<>());
        workersTargetMaps.get(workerName).put(targetForWorker.getTargetId(),targetForWorker);
    }
    public Map<String,TargetForWorker> getWorkerTargetMap(String workerName) {
        return workersTargetMaps.get(workerName);
    }
    public Set<String> getWorkerTargetSet(String workerName){
        Set<String> targetsSet = workersTargetMaps.get(workerName).keySet();
        return targetsSet;
    }
    public TargetForWorker getTargetFromWorkerMap(String workerName, String targetId) {
        return workersTargetMaps.get(workerName).get(targetId);
    }
}

