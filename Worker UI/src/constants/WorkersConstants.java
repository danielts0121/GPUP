package constants;

public class WorkersConstants {
    /*------------------------------------------FXMLs-------------------------------------------------------*/
    public static final String WORKERS_LOGIN_FXML_RESOURCE = "/login/WorkersLogin.fxml";
    public static final String WORKERS_DASHBOARD_FXML_RESOURCE = "/workerdashboard/WorkersDashboard.fxml";
    public static final String WORKERS_MAIN_MENU_FXML_RESOURCE = "/main/WorkersMainMenu.fxml";
    public static final String WORKERS_TASKS_CONTROL_FXML_RESOURCE = "/tasks/control/WorkersTasksControl.fxml";

    /*--------------------------------------------Servlets---------------------------------------------------*/
    public final static String BASE_DOMAIN = "localhost";
    private final static String FULL_SERVER_PATH = "http://" + BASE_DOMAIN + ":8080" + "/WebApplication_Web_exploded";
    public static final String WORKER_TASK_PAGE = FULL_SERVER_PATH + "/worker/task";
    public static final String GET_WORKER_PAGE = FULL_SERVER_PATH + "/getWorker";
}
