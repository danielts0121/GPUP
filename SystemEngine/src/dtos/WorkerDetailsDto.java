package dtos;

import java.util.HashSet;
import java.util.Set;

public class WorkerDetailsDto {
    private String userName;
    private Set<String> registeredTasks;
    private Integer earnedCredits;

    public WorkerDetailsDto(String name) {
        this.userName = name;
        this.registeredTasks = new HashSet<>();
        earnedCredits = 0;
    }


    public Integer getEarnedCredits() {
        return earnedCredits;
    }

    public String getUserName() {
        return userName;
    }

    public void addCredits(Integer amountOfCredits) {
        earnedCredits += amountOfCredits;
    }

    public Set<String> getRegisteredTasks() {
        return registeredTasks;
    }

    public void registerToTask(String task) {
        registeredTasks.add(task);
    }

    public void unregisterFromTask(String task) {
        registeredTasks.remove(task);
    }
}
