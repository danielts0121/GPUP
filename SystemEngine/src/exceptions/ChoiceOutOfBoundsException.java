package exceptions;

public class ChoiceOutOfBoundsException extends Exception {
    private final String EXCEPTION_MASSAGE = "Invalid choice, please enter a number between 1 to 9:";

    @Override
    public String getMessage(){
        return EXCEPTION_MASSAGE;
    }
}
