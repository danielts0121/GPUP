package exceptions;

import target.Target;

public class TargetNotExistException extends Exception {
    private String notExist;
    private Target referToNotExist;
    public TargetNotExistException(String notExist, Target referToNotExist){
        this.notExist = notExist;
        this.referToNotExist = referToNotExist;
    }

    @Override
    public String getMessage(){
        return ("The target " + referToNotExist.getName() + " refers to the target " + notExist + " that does not exist\n");
    }
}
