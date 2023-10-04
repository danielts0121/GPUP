package task.compilation;

public class CompilationParameters {
    private final String sourcePath;
    private final String destinationPath;
    private String ExtraData;

    public CompilationParameters(String sourcePath, String destinationPath) {
        this.sourcePath = sourcePath;
        this.destinationPath = destinationPath;
    }

    public String getSourcePath() {
        return this.sourcePath;
    }

    public String getDestinationPath() {
        return this.destinationPath;
    }

    public String getExtraData() {
        return ExtraData;
    }

    public void setExtraData(String extraData) {
        ExtraData = extraData;
    }
}
