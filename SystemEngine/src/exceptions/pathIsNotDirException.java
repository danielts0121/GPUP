package exceptions;

public class pathIsNotDirException extends Exception {
    private final String FILE_NAME;

    public pathIsNotDirException(String FILE_NAME) {
        this.FILE_NAME = FILE_NAME;
    }

    @Override
    public String getMessage() {
        return (FILE_NAME + " is not a directory\n");
    }
}

