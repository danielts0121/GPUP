package users;

import java.util.HashSet;
import java.util.Set;

public class UsersLists {
    private Set<String> adminsList;
    private Set<String> workersList;

    public UsersLists() {
        adminsList = new HashSet<>();
        workersList = new HashSet<>();
    }

    public Set<String> getAdminsList() {
        return adminsList;
    }

    public Set<String> getWorkersList() {
        return workersList;
    }
}
