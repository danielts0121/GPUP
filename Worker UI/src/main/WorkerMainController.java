package main;

import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;
import constants.WorkersConstants;
import dtos.WorkerDetailsDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import main.include.Constants;
import okhttp3.*;
import users.UsersLists;
import util.http.HttpClientUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tasks.control.TaskController;
import worker.taskmanagment.WorkerTaskManager;
import workerdashboard.DashboardController;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class WorkerMainController {

    @FXML private ScrollPane runTaskComponent;
    @FXML private TaskController taskController;
    @FXML private SplitPane dashboardComponent;
    @FXML private DashboardController dashboardController;
    @FXML private ListView<String> onlineAdminsListView;
    @FXML private ListView<String> onlineWorkersListView;
    @FXML private Button dashboardButton;
    @FXML private Button MyTasksButton;
    @FXML private ScrollPane mainChangingScene;
    @FXML private Label userNameLabel;
    @FXML private TextField CreditsEarnedTextField;

    private Stage primaryStage;
    private String userName;
    private Integer allocatedThreads;
    private int creditsEarned = 0;
    private Integer amountOfTasksRegisteredTo = 0;
    private WorkerTaskManager taskExecutor = null;
    private ObservableList<String> onlineAdminsList = FXCollections.observableArrayList();
    private ObservableList<String> onlineWorkersList = FXCollections.observableArrayList();
    private Thread refreshUsersListThread;


    @FXML
    public void initialize(Stage primaryStage) throws IOException {
        taskExecutor = new WorkerTaskManager(allocatedThreads, userName);
        userNameLabel.setText("Hello, " + userName);
        this.primaryStage = primaryStage;
        refreshComponentsAndControllers();

        dashboardController.setPrimaryStage(primaryStage);
        dashboardController.setUserName(userName);
        dashboardController.setWorkerMainController(this);
        taskController.setUserName(userName);
        taskController.setWorkerMainController(this);
        taskExecutor.setDaemon(true);
        taskExecutor.start();

        onlineAdminsListView.setItems(onlineAdminsList);
        onlineWorkersListView.setItems(onlineWorkersList);
        refreshUsersListThread = new Thread(this::refreshUsersListThread);
        refreshUsersListThread.setDaemon(true);
        refreshUsersListThread.start();
    }

    @FXML
    void ChangePage(ActionEvent event) {
        if (event.getSource() == MyTasksButton) {
            mainChangingScene.setContent(runTaskComponent);
        } else if (event.getSource() == dashboardButton) {
            mainChangingScene.setContent(dashboardComponent);
        }
    }

    private void refreshUsersListThread() {
        while (refreshUsersListThread.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getUsersLists();
            getCreditsEarnedAntRegisteredTasks();
        }
    }
    private void getCreditsEarnedAntRegisteredTasks() {

        String finalUrl = HttpUrl
                .parse(WorkersConstants.GET_WORKER_PAGE)
                .newBuilder()
                .addQueryParameter("workerName", userName)
                .addQueryParameter("getWorkerDto", "getWorkerDto")
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    ResponseBody responseBody = response.body();
                    Gson gson = new Gson();
                    if (responseBody != null) {
                        Platform.runLater(() -> {
                            try {
                                WorkerDetailsDto workerDetailsDto = gson.fromJson(responseBody.string(), WorkerDetailsDto.class);
                                responseBody.close();
                                creditsEarned = workerDetailsDto.getEarnedCredits();
                                CreditsEarnedTextField.setText(String.valueOf(creditsEarned));
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                response.close();
                            }
                        });
                    }

                } else
                    response.close();
            }
        });
    }


    private void getUsersLists() {
        String finalUrl = HttpUrl
                .parse(Constants.USERS_LISTS)
                .newBuilder()
                .build()
                .toString();


        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Gson gson = new Gson();
                ResponseBody responseBody = response.body();
                UsersLists usersLists = gson.fromJson(responseBody.string(), UsersLists.class);
                responseBody.close();
                Platform.runLater(() -> updateUsersLists(usersLists));
                Objects.requireNonNull(response.body()).close();
                response.close();
            }
        });
    }

    private void updateUsersLists(UsersLists usersLists) {
        onlineAdminsList.clear();
        onlineWorkersList.clear();
        onlineAdminsList.addAll(usersLists.getAdminsList());
        onlineWorkersList.addAll(usersLists.getWorkersList());
    }

    private void refreshComponentsAndControllers() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(WorkersConstants.WORKERS_TASKS_CONTROL_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        runTaskComponent = fxmlLoader.load(url.openStream());
        taskController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(WorkersConstants.WORKERS_DASHBOARD_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        dashboardComponent = fxmlLoader.load(url.openStream());
        dashboardController = fxmlLoader.getController();
    }

    public void setUserName(String userName) { this.userName = userName; }

    public void setAllocatedThreads(Integer allocatedThreads) {
        this.allocatedThreads = allocatedThreads;
    }

    public WorkerTaskManager getTaskExecutor() { return taskExecutor; }
}
