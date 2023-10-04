package exceptions;

public class ChoiceNotValidException extends Exception{
    private final String EXCEPTION_MASSAGE = "This option isn't valid right now, please load a file first";

    @Override
    public String getMessage(){
        return EXCEPTION_MASSAGE;
    }
}
