package task.simulation;

import java.util.HashSet;
import java.util.Set;

public class SimulationTaskInformation {
    private final String taskName;
    private final String taskCreator;
    private final String graphName;
    private final Set<String> targetsToExecute;
    private final Integer pricingForTarget;
    private final SimulationParameters simulationParameters;
    private final boolean isIncremental;

    public SimulationTaskInformation(String taskName, String taskCreator, String graphName,
                                         Set<String> targetsToExecute, Integer pricingForTarget,
                                            SimulationParameters simulationParameters, boolean isIncremental) {
        this.taskName = taskName;
        this.taskCreator = taskCreator;
        this.graphName = graphName;
        this.targetsToExecute = new HashSet<>();
        this.targetsToExecute.addAll(targetsToExecute);
        this.pricingForTarget = pricingForTarget;
        this.simulationParameters = simulationParameters;
        this.isIncremental = isIncremental;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public String getTaskCreator() {
        return this.taskCreator;
    }

    public String getGraphName() {
        return this.graphName;
    }

    public Set<String> getTargetsToExecute() {
        return this.targetsToExecute;
    }

    public Integer getPricingForTarget() {
        return this.pricingForTarget;
    }

    public SimulationParameters getSimulationParameters() {
        return this.simulationParameters;
    }

    public boolean isIncremental() {
        return isIncremental;
    }
}
