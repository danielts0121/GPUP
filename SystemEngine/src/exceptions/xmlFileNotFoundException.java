package exceptions;

public class xmlFileNotFoundException extends Exception {
    private final String FILE_NAME;
    public xmlFileNotFoundException(String FILE_NAME){
        this.FILE_NAME = FILE_NAME;
    }

    @Override
    public String getMessage(){
        return ("file with the name " + FILE_NAME +" not found\n");
    }
}
