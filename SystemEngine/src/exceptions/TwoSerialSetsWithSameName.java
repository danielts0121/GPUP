package exceptions;

public class TwoSerialSetsWithSameName extends Exception {
    private String SerialSetName;

    public TwoSerialSetsWithSameName(String SerialSetName) {
        this.SerialSetName = SerialSetName;
    }

    @Override
    public String getMessage(){
        return ("The serial set " + SerialSetName + " appears more then one time\n");
    }
}
