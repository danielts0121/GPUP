package exceptions;

public class SameTargetException extends Exception{
    private final String EXCEPTION_MASSAGE = "Do not enter the same target twice, please enter a different target:";

    @Override
    public String getMessage(){
        return EXCEPTION_MASSAGE;
    }
}
