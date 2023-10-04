package task.compilation;

import dtos.CompilationTaskDto;
import target.Target;
import target.TargetForWorker;
import target.TargetGraph;
import task.GPUPTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

public class CompilationTask extends GPUPTask {

    private final String sourceFolderPath;
    private String destinationPath;

    public CompilationTask(String taskName, String sourceFilePath, String destinationFilePath,
                           TargetForWorker target) {
        super(taskName, target, "Compilation");
        this.sourceFolderPath = sourceFilePath;
        this.destinationPath = destinationFilePath;
    }

    public CompilationTask(CompilationTaskDto compilationTaskDto) {
        super(compilationTaskDto);
        this.sourceFolderPath = compilationTaskDto.getSourceFolderPath();
        this.destinationPath = compilationTaskDto.getDestinationPath();
    }

    @Override
    public void run() {
        String FQNToPath = "\\" + target.getExtraData().replace(".","\\");
        String javaFilePath = sourceFolderPath + FQNToPath + ".java";
        try {
            Thread.sleep(500);
            Instant targetTaskBegin = Instant.now();
            target.setTargetStatus(Target.Status.IN_PROCESS);
            target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " is starting compilation\n\n"));
            uploadTaskStatusToServer();

            ProcessBuilder processBuilder = new ProcessBuilder("javac", "-d", destinationPath, "-cp", destinationPath, javaFilePath);
            Process process;

            process = processBuilder.start();
            int code = process.waitFor();

            Thread.sleep(200);
            if (code == 0) {
                target.setTargetResult(Target.Result.SUCCESS);
                target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " compiled successfully with compilation time of: " +
                        TargetGraph.getDurationAsString(Duration.between(targetTaskBegin, Instant.now())) + "\n\n"));
                uploadTaskStatusToServer();
            } else {
                target.setTargetResult(Target.Result.FAILURE);
                String errorMsg = "";
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                for (String errorLine:bufferedReader.lines().collect(Collectors.toList())) {
                    errorMsg += errorLine + "\n";
                }
                String finalErrorMsg = errorMsg;
                target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " compilation failed!\n" +
                        "error message:\n" + finalErrorMsg + "\n\n"));
                uploadTaskStatusToServer();
            }
            target.setTargetStatus(Target.Status.FINISHED);
            Thread.sleep(200);
        }catch (Exception exception) {
            target.setRunLog(target.getRunLog().concat("Target " + target.getName() + " was interrupted!\n\n"));
            target.setTargetStatus(Target.Status.SKIPPED);
            target.setTargetResult(Target.Result.SKIPPED);
        }
        finally{
            uploadTaskStatusToServer();
        }
    }

    public String getSourceFolderPath() {
        return sourceFolderPath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }
}
