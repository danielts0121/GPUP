package exceptions;

import target.Target;

public class TargetsDependsOnEachOtherException extends Exception {
    private Target target1;
    private Target target2;
    public TargetsDependsOnEachOtherException(Target target1, Target target2){
        this.target1 = target1;
        this.target2 = target2;
    }

    @Override
    public String getMessage(){
        return ("The targets " + target1.getName() + " and " +target2.getName() + " depend on each other\n");
    }
}
