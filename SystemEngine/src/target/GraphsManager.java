package target;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GraphsManager {

    private static final Map<String, File> graphsXMLMap = new HashMap<>();
    private static final Map<String, TargetGraph> targetGraphMap = new HashMap<>();
    private static final Set<String> targetGraphNameSet = new HashSet<>();

    public boolean isGraphExists(String graphName) { return targetGraphMap.containsKey(graphName.toLowerCase(Locale.ROOT)); }

    public synchronized File getGraphFile(String graphName) {
        return graphsXMLMap.get(graphName.toLowerCase(Locale.ROOT));
    }

    public synchronized TargetGraph getGraph(String graphName) {
        return targetGraphMap.get(graphName);
    }

    public synchronized void addGraph(String graphName, File file, TargetGraph graph) {
        try {
            Path filePath = file.toPath();
            String newFileName = graph.getGraphName() + ".xml";
            Files.deleteIfExists(filePath.resolveSibling(newFileName));
            filePath = Files.move(filePath, filePath.resolveSibling(newFileName));

            graphsXMLMap.put(graphName.toLowerCase(Locale.ROOT), filePath.toFile());
            targetGraphMap.put(graphName.toLowerCase(Locale.ROOT), graph);
            targetGraphNameSet.add(graphName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized Set<String> getGraphNames()
    {
        return targetGraphNameSet;
    }
}
