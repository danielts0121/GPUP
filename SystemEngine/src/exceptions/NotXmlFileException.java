package exceptions;

public class NotXmlFileException extends Exception {
    private final String FILE_NAME;
    public NotXmlFileException(String FILE_NAME){
        this.FILE_NAME = FILE_NAME;
    }

    @Override
    public String getMessage(){
        return (FILE_NAME +" is not an xml file\n");
    }
}
