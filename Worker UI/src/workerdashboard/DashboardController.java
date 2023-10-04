package workerdashboard;

import com.google.gson.Gson;
import constants.WorkersConstants;
import dashboard.tableitems.TargetsInfoTableItem;
import dtos.TaskDetailsDto;
import dtos.WorkerDetailsDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import main.WorkerMainController;
import main.include.Constants;
import okhttp3.*;
import com.sun.istack.internal.NotNull;
import util.http.HttpClientUtil;
import workerdashboard.tableitems.SelectedTaskDashTableItem;
import java.awt.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class DashboardController {

    @FXML
    private ListView<String> OnlineTasksListView;

    @FXML
    private TextField TaskNameTextField;

    @FXML
    private TextField UploadedByTextField;

    @FXML
    private TableView<TargetsInfoTableItem> TaskTypesAmountTableView;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> TargetsColumn;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> IndependentColumn;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> LeafColumn;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> MiddleColumn;

    @FXML
    private TableColumn<TargetsInfoTableItem, Integer> RootColumn;

    @FXML
    private TableView<SelectedTaskDashTableItem> TaskInfoTableView;

    @FXML
    private TableColumn<SelectedTaskDashTableItem, String> TaskStatusColumn;

    @FXML
    private TableColumn<SelectedTaskDashTableItem, Integer> currentWorkersColumn;

    @FXML
    private TableColumn<SelectedTaskDashTableItem, Integer> TaskWorkPaymentColumn;

    @FXML
    private TextField TaskTypeTextField;

    @FXML
    private TextField AmIRegisteredTextField;

    @FXML
    private Button RegisterTaskButton;

    private ObservableList<String> onlineTasksList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedTaskList = FXCollections.observableArrayList();
    private ObservableList<TargetsInfoTableItem> targetsTypeInfoTableList = FXCollections.observableArrayList();
    private ObservableList<SelectedTaskDashTableItem> TaskInfoTableList = FXCollections.observableArrayList();
    private SimpleBooleanProperty isRegisteredToCurrentTask;
    private Set<String> RegisteredTasks = new HashSet<>();
    private ListChangeListener<String> currentSelectedTaskListListener;
    private Stage primaryStage;
    private String userName;
    private Thread refreshDashboardDataThread;
    private WorkerMainController workerMainController;

    public DashboardController() {
        this.isRegisteredToCurrentTask = new SimpleBooleanProperty(true);
        currentSelectedTaskListListener = change -> {
            displaySelectedTaskInfo();
            if (change.getList().isEmpty())
                isRegisteredToCurrentTask.set(true);
        };
    }

    public void initialize() {
        currentSelectedTaskList = OnlineTasksListView.getSelectionModel().getSelectedItems();
        currentSelectedTaskList.addListener(currentSelectedTaskListListener);
        initializeTargetDetailsTable();
        initializeTaskStatusTable();
        refreshDashboardDataThread = new Thread(this::refreshDashboardData);
        Thread suddenExitHook = new Thread(this::logout);
        Runtime.getRuntime().addShutdownHook(suddenExitHook);
        OnlineTasksListView.setItems(onlineTasksList);
        refreshDashboardDataThread.setDaemon(true);
        refreshDashboardDataThread.start();
        RegisterTaskButton.disableProperty().bind(isRegisteredToCurrentTask);
    }

    @FXML
    void RegisterTaskButtonClicked(ActionEvent event) {
        String selectedItem = OnlineTasksListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null) {
            AmIRegisteredTextField.setText("Yes");
            registerToTask(selectedItem);
        }
    }

    private void displaySelectedTaskInfo() {
        if (currentSelectedTaskList.isEmpty())
            return;

        String selectedTaskName = currentSelectedTaskList.get(0);

        String finalUrl = HttpUrl
                .parse(Constants.TASKS_PATH)
                .newBuilder()
                .addQueryParameter("selectedTaskName", selectedTaskName)
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
                    try {
                        if (responseBody != null) {
                            TaskDetailsDto taskDetailsDto = gson.fromJson(responseBody.string(), TaskDetailsDto.class);
                            responseBody.close();
                            Platform.runLater(() -> displaySelectedTaskInfoFromDto(taskDetailsDto));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Objects.requireNonNull(response.body()).close();
                response.close();
            }
        });
    }

    private void displaySelectedTaskInfoFromDto(TaskDetailsDto taskDetailsDto) {
        this.TaskNameTextField.setText(taskDetailsDto.getTaskName());
        this.UploadedByTextField.setText(taskDetailsDto.getUploader());
        this.AmIRegisteredTextField.setText(RegisteredTasks.contains(taskDetailsDto.getTaskName())? "Yes" : "No");
        this.isRegisteredToCurrentTask.set(RegisteredTasks.contains(taskDetailsDto.getTaskName()));
        this.TaskTypeTextField.setText(taskDetailsDto.getTaskTypeAsString());
        updateTaskDetailsTables(taskDetailsDto);
    }

    private void updateTaskDetailsTables(TaskDetailsDto taskDetailsDto) {

        TargetsInfoTableItem targetsInfoTableItem = new TargetsInfoTableItem(taskDetailsDto.getRoots(),
                taskDetailsDto.getMiddles(), taskDetailsDto.getLeaves(), taskDetailsDto.getIndependents(), taskDetailsDto.getTargets());
        SelectedTaskDashTableItem selectedTaskDashTableItem = new SelectedTaskDashTableItem(taskDetailsDto.getTaskStatus(),
                        taskDetailsDto.getTotalWorkers(),taskDetailsDto.getTotalPayment()/taskDetailsDto.getTargets());

        this.targetsTypeInfoTableList.clear();
        this.targetsTypeInfoTableList.add(targetsInfoTableItem);
        this.TaskInfoTableList.clear();
        this.TaskInfoTableList.add(selectedTaskDashTableItem);
        this.TaskTypesAmountTableView.setItems(this.targetsTypeInfoTableList);
        this.TaskInfoTableView.setItems(this.TaskInfoTableList);
    }

    public void initializeTargetDetailsTable() {
        this.TargetsColumn.setCellValueFactory(new PropertyValueFactory<>("targets"));
        this.RootColumn.setCellValueFactory(new PropertyValueFactory<>("roots"));
        this.MiddleColumn.setCellValueFactory(new PropertyValueFactory<>("middles"));
        this.LeafColumn.setCellValueFactory(new PropertyValueFactory<>("leaves"));
        this.IndependentColumn.setCellValueFactory(new PropertyValueFactory<>("independents"));
    }

    public void initializeTaskStatusTable() {
        this.TaskStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        this.currentWorkersColumn.setCellValueFactory(new PropertyValueFactory<>("workers"));
        this.TaskWorkPaymentColumn.setCellValueFactory(new PropertyValueFactory<>("targetPayment"));
    }

    private void refreshDashboardData() {
        while (refreshDashboardDataThread.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getOnlineTasksList();
            getRegisteredTasks();
            if (!currentSelectedTaskList.isEmpty()) {
                displaySelectedTaskInfo();
            }
        }
    }

    private void getRegisteredTasks() {
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
                                    RegisteredTasks.clear();
                                    RegisteredTasks.addAll(workerDetailsDto.getRegisteredTasks());
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

    public void getOnlineTasksList() {
        String finalUrl = HttpUrl
                .parse(Constants.TASKS_LIST_PATH)
                .newBuilder()
                .addQueryParameter("onlineTasksList", "onlineTasksList")
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300)
                {
                    ResponseBody responseBody = response.body();
                    Gson gson = new Gson();
                    try {
                        if (responseBody != null) {
                            Set<String> taskList = gson.fromJson(responseBody.string(), Set.class);
                            Platform.runLater(() ->updateOnlineTasksList(taskList));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Objects.requireNonNull(response.body()).close();
                response.close();
            }
        });
    }

    private void updateOnlineTasksList(Set<String> taskList) {
        onlineTasksList.removeAll(onlineTasksList.stream().filter(task -> !taskList.contains(task)).collect(Collectors.toSet()));
        for (String task: taskList) {
            if(!onlineTasksList.contains(task))
                onlineTasksList.add(task);
        }
    }

    private void logout() {
        if (userName == null)
            return;

        String finalUrl = HttpUrl
                .parse(Constants.LOGOUT_PAGE)
                .newBuilder()
                .addQueryParameter("username", userName)
                .build()
                .toString();


        HttpClientUtil.runAsync(finalUrl, "DELETE", null, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            }
        });
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void registerToTask(String taskName) {
        RequestBody body = RequestBody.create("null",MediaType.parse("application/json"));
        String finalUrl = HttpUrl
                .parse(Constants.WORKER_TASK_PAGE)
                .newBuilder()
                .addQueryParameter("registerToTask", "registerToTask")
                .addQueryParameter("taskName",taskName)
                .addQueryParameter("workerName", userName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, "POST", body, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() > 300 || response.code() < 200) {
                    String message = response.header("message");
                    Platform.runLater(() -> errorPopup(message));
                }
                else
                    workerMainController.getTaskExecutor().addRegisteredTask(taskName);
                response.close();
            }
        });
    }

    public void errorPopup(String message) {
        Toolkit.getDefaultToolkit().beep();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(message);
        alert.initOwner(primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
    }

    public void setWorkerMainController(WorkerMainController workerMainController) {
        this.workerMainController = workerMainController;
    }
}
