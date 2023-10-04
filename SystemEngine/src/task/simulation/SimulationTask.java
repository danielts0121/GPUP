package task.simulation;

import dtos.SimulationTaskDto;
import target.Target;
import target.TargetForWorker;
import task.GPUPTask;

import java.util.Random;

public class SimulationTask extends GPUPTask {

    private final int processTimeInMS;
    private final boolean isRandom;
    private final double successChance;
    private final double warningChance;
    private final Random random;

    public SimulationTask(String taskName, int processTimeInMS, boolean isRandom, double successChance,
                            double warningChance, TargetForWorker target) {
        super(taskName, target, "Simulation");
        this.processTimeInMS = processTimeInMS;
        this.isRandom = isRandom;
        this.successChance = successChance;
        this.warningChance = warningChance;
        this.random = new Random();
    }

    public SimulationTask(SimulationTaskDto simulationTaskDto) {
        super(simulationTaskDto);
        this.processTimeInMS = simulationTaskDto.getProcessTimeInMS();
        this.isRandom = simulationTaskDto.isRandom();
        this.successChance = simulationTaskDto.getSuccessChance();
        this.warningChance = simulationTaskDto.getWarningChance();
        this.random = simulationTaskDto.getRandom();
    }

    @Override
    public void run() {
        try {
            target.setTargetStatus(Target.Status.IN_PROCESS);
            uploadTaskStatusToServer();
            int runTime;
            double randSuccess = random.nextDouble();
            double randWarning = random.nextDouble();
            if (isRandom)
                runTime = random.nextInt(processTimeInMS);
            else
                runTime = processTimeInMS;

            target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " is going to sleep for " + runTime + " milliseconds\n\n"));
            uploadTaskStatusToServer();

            Thread.sleep(runTime);

            if (randSuccess > successChance) {
                target.setTargetResult(Target.Result.FAILURE);
            } else if (randWarning < warningChance)
                target.setTargetResult(Target.Result.WARNING);
            else
                target.setTargetResult(Target.Result.SUCCESS);


            target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " woke up with result: " + target.getTargetResult().toString() + "\n\n"));
            target.setTargetStatus(Target.Status.FINISHED);
            uploadTaskStatusToServer();

        } catch (InterruptedException exception) {
            target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " was interrupted! \n\n"));
            target.setTargetStatus(Target.Status.SKIPPED);
            target.setTargetResult(Target.Result.SKIPPED);
        }
        finally{
            //uploadTaskStatusToServer();
        }
    }

    public int getProcessTimeInMS() {
        return processTimeInMS;
    }

    public boolean isRandom() {
        return isRandom;
    }

    public double getSuccessChance() {
        return successChance;
    }

    public double getWarningChance() {
        return warningChance;
    }

    public Random getRandom() {
        return random;
    }

}
