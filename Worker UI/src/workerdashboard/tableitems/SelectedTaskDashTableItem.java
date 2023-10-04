package workerdashboard.tableitems;

public class SelectedTaskDashTableItem {
    private final String status;
    private final Integer workers;
    private final Integer targetPayment;

    public SelectedTaskDashTableItem(String status, Integer workers, Integer targetPayment) {
        this.status = status;
        this.workers = workers;
        this.targetPayment = targetPayment;
    }

    public String getStatus() {
        return this.status;
    }

    public Integer getWorkers() {
        return this.workers;
    }

    public Integer getTargetPayment() {
        return this.targetPayment;
    }
}
