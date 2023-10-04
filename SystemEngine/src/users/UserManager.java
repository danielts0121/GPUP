package users;

import dtos.WorkerDetailsDto;

import java.util.*;


public class UserManager {

    private final UsersLists usersLists;
    private final Map<String, WorkerDetailsDto> workerDetailsDtoMap;

    public UserManager() {
        usersLists = new UsersLists();
        workerDetailsDtoMap = new HashMap<>();
    }

    public synchronized void addAdmin(String username) {
        usersLists.getAdminsList().add(username);
    }
    public synchronized Set<String> getAdmins() {
        return Collections.unmodifiableSet(usersLists.getAdminsList());
    }

    public synchronized Set<String> getWorkers() {
        return Collections.unmodifiableSet(usersLists.getWorkersList());
    }

    public synchronized void addWorker(String username) {
        usersLists.getWorkersList().add(username);
        if(!workerDetailsDtoMap.containsKey(username)) {
            workerDetailsDtoMap.put(username, new WorkerDetailsDto(username.toLowerCase()));
        }
    }

    public synchronized void removeUser(String username) {
        if(usersLists.getWorkersList().contains(username))
            usersLists.getWorkersList().remove(username);
        else
            usersLists.getAdminsList().remove(username);
    }

    public boolean isUserExists(String username) {
        return (usersLists.getAdminsList().contains(username) || usersLists.getWorkersList().contains(username));
    }

    public UsersLists getUsersLists() { return usersLists; }

    public WorkerDetailsDto getWorkerDetailsDto(String workerName) {
        return workerDetailsDtoMap.get(workerName);
    }
}
