package dtos;

import task.compilation.CompilationTask;

public class CompilationTaskDto extends GPUPTaskDto{
    private final String sourceFolderPath;
    private String destinationPath;

    public CompilationTaskDto(CompilationTask compilationTask) {
        super(compilationTask);
        this.sourceFolderPath = compilationTask.getSourceFolderPath();
        this.destinationPath = compilationTask.getDestinationPath();
    }

    public String getSourceFolderPath() {
        return sourceFolderPath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }
}
