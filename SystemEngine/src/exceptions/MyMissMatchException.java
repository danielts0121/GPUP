package exceptions;

import java.util.InputMismatchException;

public class MyMissMatchException extends InputMismatchException {
    private final String EXCEPTION_MASSAGE = "Invalid input, please enter an INTEGER:";

    @Override
    public String getMessage(){
        return EXCEPTION_MASSAGE;
    }
}
