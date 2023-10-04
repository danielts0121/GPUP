package target;

import exceptions.TargetAppearTwiceException;
import exceptions.TargetThatAppearsInTheSerialSetDoNotExist;
import exceptions.TwoSerialSetsWithSameName;
import exceptions.pathIsNotDirException;
import xmlfiles.generated.GPUPConfiguration;
import xmlfiles.generated.GPUPDescriptor;
import xmlfiles.generated.GPUPTarget;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileChecker {
    public TargetGraph createTargetGraphFromXml(Path filePath) throws Exception {

        File file = new File(filePath.toString());
        JAXBContext jaxbContext = JAXBContext.newInstance(GPUPDescriptor.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        GPUPDescriptor gpupDescriptor = (GPUPDescriptor) jaxbUnmarshaller.unmarshal(file);

        GPUPConfiguration gpupConfiguration = gpupDescriptor.getGPUPConfiguration();
        TargetGraph targetGraph = new TargetGraph(gpupConfiguration.getGPUPGraphName());
        Map<TargetGraph.TaskType, Integer>  taskPricing = new HashMap<>();
        for (GPUPConfiguration.GPUPPricing.GPUPTask task:gpupConfiguration.getGPUPPricing().getGPUPTask()) {
            TargetGraph.TaskType taskType;
            if(task.getName().equalsIgnoreCase("simulation"))
                taskType = TargetGraph.TaskType.SIMULATION;
            else
                taskType = TargetGraph.TaskType.COMPILATION;
            taskPricing.put(taskType,task.getPricePerTarget());
        }
        targetGraph.setTaskPricing(taskPricing);

        for (GPUPTarget gpupTarget : gpupDescriptor.getGPUPTargets().getGPUPTarget()) {
            Target target = new Target(gpupTarget);
            if (targetGraph.getAllTargets().containsKey(target.getName().toUpperCase())){
                throw new TargetAppearTwiceException(target);
            }
            targetGraph.addTargetToGraph(target);
        }

        for (GPUPTarget gpupTarget : gpupDescriptor.getGPUPTargets().getGPUPTarget()) {
            if (gpupTarget.getGPUPTargetDependencies() != null) {
                targetGraph.getTarget(gpupTarget.getName()).addDependencies(gpupTarget.getGPUPTargetDependencies().getGPUGDependency(), targetGraph.getAllTargets());
            }
        }
        targetGraph.resetGraph();
        return targetGraph;
    }

    public void checkIfPathIsValidDirectory(String directoryPath) throws Exception {
        if (directoryPath == null)
            return;
        Path workingDirPath = new File(directoryPath).toPath();
        if(Files.exists(workingDirPath)) {
            if(!Files.isDirectory(workingDirPath))
                throw new pathIsNotDirException(directoryPath);
        }
        else {
            Files.createDirectories(workingDirPath);
        }
    }
}
