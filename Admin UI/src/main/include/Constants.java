package main.include;

public class Constants {
    public final static String MAIN_FXML_RESOURCE = "/main/MainMenu.fxml";
    public final static String FIND_PATH_FXML_RESOURCE = "/connections/findPath/FindPath.fxml";
    public final static String FIND_CYCLE_FXML_RESOURCE = "/connections/findCycle/FindCycle.fxml";
    public final static String DEEP_DEPENDENCIES_FXML_RESOURCE = "/connections/deepDependencies/DeepDependencies.fxml";
    public final static String ADD_TASK_FXML_RESOURCE = "/connections/addTask/AddTask.fxml";
    public final static String GRAPH_FXML_RESOURCE = "/graph/Graph.fxml";
    public final static String DASHBOARD_FXML_RESOURCE = "/dashboard/AdminDashboard.fxml";
    public final static String RUNTASK_FXML_RESOURCE = "/runtask/RunTask.fxml";
    public final static String LOGIN_FXML_RESOURCE = "/login/AdminLogin.fxml";
    public final static String BASE_DOMAIN = "localhost";
    public final static String FULL_SERVER_PATH = "http://" + BASE_DOMAIN + ":8080" + "/WebApplication_Web_exploded";
    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String USERS_LISTS = FULL_SERVER_PATH + "/userslists";
    public final static String GRAPHS_PATH = FULL_SERVER_PATH + "/graphs";
    public final static String LOGOUT_PAGE = FULL_SERVER_PATH + "/logout";
    public final static String GRAPHS_LISTS_PAGE = FULL_SERVER_PATH + "/graphslist";
    public final static String TASKS_PATH = FULL_SERVER_PATH + "/tasks";
    public final static String TASKS_LIST_PATH = FULL_SERVER_PATH + "/tasks/list";
    public final static String TASKS_OPERATION_PATH = FULL_SERVER_PATH + "/tasks/operation";
    public static final String WORKER_TASK_PAGE = FULL_SERVER_PATH + "/worker/task";
    public static final String GET_TARGETS_PAGE = FULL_SERVER_PATH + "/tasks/getTargets";
}
