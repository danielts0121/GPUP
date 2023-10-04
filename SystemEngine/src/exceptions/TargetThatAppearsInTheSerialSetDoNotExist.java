package exceptions;

public class TargetThatAppearsInTheSerialSetDoNotExist extends Exception{
    String targetName;
    String serialSetName;

    public TargetThatAppearsInTheSerialSetDoNotExist(String targetName, String serialSetName) {
        this.targetName = targetName;
        this.serialSetName = serialSetName;
    }

    @Override
    public String getMessage(){
        return ("The serial set " + serialSetName + " contains the target " + targetName + " but it does not exist in the graph\n");
    }
}
