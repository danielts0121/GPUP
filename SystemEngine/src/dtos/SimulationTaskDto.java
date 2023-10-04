package dtos;

import task.simulation.SimulationTask;
import java.util.Random;

public class SimulationTaskDto extends GPUPTaskDto {
    private final int processTimeInMS;
    private final boolean isRandom;
    private final double successChance;
    private final double warningChance;
    private final Random random;

    public SimulationTaskDto(SimulationTask simulationTask) {
        super(simulationTask);
        this.processTimeInMS = simulationTask.getProcessTimeInMS();
        this.isRandom = simulationTask.isRandom();
        this.successChance = simulationTask.getSuccessChance();
        this.warningChance = simulationTask.getWarningChance();
        this.random = simulationTask.getRandom();
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
