package task.compilation;

import java.util.Set;

public class CompilationTaskInformation {
    private final String taskName;
    private final String taskCreator;
    private final String graphName;
    private final Set<String> targetsToExecute;
    private final Integer pricingForTarget;
    private final CompilationParameters compilationParameters;
    private final boolean isIncremental;

    public CompilationTaskInformation(String taskName, String taskCreator, String graphName,
                                            Set<String> targetsToExecute, Integer pricingForTarget,
                                                 CompilationParameters compilationParameters, boolean isIncremental) {
        this.taskName = taskName;
        this.taskCreator = taskCreator;
        this.graphName = graphName;
        this.targetsToExecute = targetsToExecute;
        this.pricingForTarget = pricingForTarget;
        this.compilationParameters = compilationParameters;
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

    public CompilationParameters getCompilationParameters() {
        return compilationParameters;
    }

    public Set<String> getTargetsToExecute() {
        return this.targetsToExecute;
    }

    public Integer getPricingForTarget() {
        return this.pricingForTarget;
    }

    public boolean isIncremental() {
        return isIncremental;
    }
}
