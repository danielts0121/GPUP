package runtask.tableview;

import target.TargetForWorker;
import java.util.Set;

public class TargetInfoTableItem {
    private String name;
    private String type;
    private Set<String> serialSets;
    private String status;

    public TargetInfoTableItem(TargetForWorker target) {
        this.name = target.getName();
        this.type = target.getNodeType();
        this.status = target.getStatus();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
