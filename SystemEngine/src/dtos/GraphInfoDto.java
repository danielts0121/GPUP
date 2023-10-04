package dtos;

import target.TargetGraph;
import target.Target;
import java.util.Map;


public class GraphInfoDto {

    private final String graphName;
    private final String uploader;
    private final Integer targets;
    private final Integer roots;
    private final Integer middles;
    private final Integer leaves;
    private final Integer independents;
    private Integer simulationPrice;
    private Integer compilationPrice;

    public GraphInfoDto(TargetGraph targetGraph) {
        this.graphName = targetGraph.getGraphName();
        this.uploader = targetGraph.getUploaderName();

        Map<String,Target> allTargets = targetGraph.getAllTargets();
        this.roots = (int)allTargets.values().stream().filter(target -> target.getNodeType().equals(Target.Type.ROOT)).count();
        this.middles = (int)allTargets.values().stream().filter(target -> target.getNodeType().equals(Target.Type.MIDDLE)).count();
        this.leaves = (int)allTargets.values().stream().filter(target -> target.getNodeType().equals(Target.Type.LEAF)).count();
        this.independents = (int)allTargets.values().stream().filter(target -> target.getNodeType().equals(Target.Type.INDEPENDENT)).count();
        this.targets = allTargets.size();

        Map<TargetGraph.TaskType, Integer> taskPricing = targetGraph.getTaskPricing();
        this.simulationPrice = taskPricing.get(TargetGraph.TaskType.SIMULATION) != null ? taskPricing.get(TargetGraph.TaskType.SIMULATION) : 0;
        this.compilationPrice = taskPricing.get(TargetGraph.TaskType.COMPILATION) != null ? taskPricing.get(TargetGraph.TaskType.COMPILATION) : 0;
    }

    public String getGraphName() {
        return this.graphName;
    }

    public String getUploader() {
        return this.uploader;
    }

    public Integer getRoots() {
        return this.roots;
    }

    public Integer getMiddles() {
        return this.middles;
    }

    public Integer getLeaves() {
        return this.leaves;
    }

    public Integer getIndependents() {
        return this.independents;
    }

    public Integer getSimulationPrice() {
        return this.simulationPrice;
    }

    public Integer getCompilationPrice() {
        return this.compilationPrice;
    }

    public Integer getTargets() {
        return targets;
    }
}
