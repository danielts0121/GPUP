package main;

import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;
import connections.addTask.AddTaskController;
import connections.deepDependencies.DeepDependenciesController;
import connections.findCycle.FindCycleController;
import connections.findPath.FindPathController;
import dashboard.DashboardController;
import graph.GraphController;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import login.AdminLoginController;
import main.include.Constants;
import okhttp3.*;
import runtask.TaskController;
import target.TargetGraph;
import users.UsersLists;
import util.http.HttpClientUtil;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static main.include.Constants.*;

public class AdminMainController {

    @FXML
    private AnchorPane findPathComponent;

    @FXML
    private FindPathController findPathController;

    @FXML
    private AnchorPane findCycleComponent;

    @FXML
    private FindCycleController findCycleController;

    @FXML
    private AnchorPane deepDependenciesComponent;

    @FXML
    private DeepDependenciesController deepDependenciesController;

    @FXML
    private ScrollPane addTaskComponent;

    @FXML
    private AddTaskController addTaskController;

    @FXML
    private ScrollPane graphComponent;

    @FXML
    private GraphController graphController;

    @FXML
    private SplitPane runTaskComponent;

    @FXML
    private TaskController taskController;

    @FXML
    private SplitPane dashboardComponent;

    @FXML
    private DashboardController dashboardController;

    @FXML
    private ListView<String> onlineAdminsListView;

    @FXML
    private ListView<String> onlineWorkersListView;

    @FXML
    private TextField SelectedGraphTextField;

    @FXML
    private TextField SelectedTaskTextField;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button displayButton;

    @FXML
    private SplitMenuButton actionsButton;

    @FXML
    private MenuItem findPathMenuItem;

    @FXML
    private MenuItem findCycleMenuItem;

    @FXML
    private MenuItem deepDependenciesMenuItem;

    @FXML
    private MenuItem addTaskMenuItem;

    @FXML
    private Button tasksButton;

    @FXML
    private ScrollPane mainChangingScene;

    @FXML
    private Label userNameLabel;
    private Stage primaryStage;
    private final SimpleBooleanProperty isFileSelected;
    private SimpleBooleanProperty isTaskSelected;
    private ObservableList<String> onlineAdminsList = FXCollections.observableArrayList();
    private ObservableList<String> onlineWorkersList = FXCollections.observableArrayList();
    private String userName;
    private Thread refreshUsersListThread;

    public AdminMainController()
    {
        isFileSelected = new SimpleBooleanProperty(false);
        isTaskSelected = new SimpleBooleanProperty(false);
    }

    @FXML
    public void initialize(Stage primaryStage) throws IOException {

        this.primaryStage = primaryStage;
        displayButton.disableProperty().bind(isFileSelected.not());
        actionsButton.disableProperty().bind(isFileSelected.not());
        tasksButton.disableProperty().bind(isTaskSelected.not());
        SelectedTaskTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            isTaskSelected.setValue(newValue!= null && !newValue.trim().equals(""));
        });
        refreshComponentsAndControllers();

        dashboardController.setPrimaryStage(primaryStage);
        dashboardController.setUserName(userName);
        dashboardController.setMainController(this);

        findPathController.setMainController(this);
        findCycleController.setMainController(this);
        deepDependenciesController.setMainController(this);
        addTaskController.setMainController(this);
        taskController.setMainController(this);

        SelectedTaskTextField.textProperty().addListener(observable -> {
            taskController.setNewTask();
        });

        onlineAdminsListView.setItems(onlineAdminsList);
        onlineWorkersListView.setItems(onlineWorkersList);
        refreshUsersListThread = new Thread(this::refreshUsersListThread);
        refreshUsersListThread.setDaemon(true);
        refreshUsersListThread.start();

        userNameLabel.setText("Hello, " + userName);
    }

    private void refreshUsersListThread() {
        while (refreshUsersListThread.isAlive()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getUsersLists();
        }
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
        URL url = getClass().getResource(FIND_PATH_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        findPathComponent = fxmlLoader.load(url.openStream());
        findPathController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(FIND_CYCLE_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        findCycleComponent = fxmlLoader.load(url.openStream());
        findCycleController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(DEEP_DEPENDENCIES_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        deepDependenciesComponent = fxmlLoader.load(url.openStream());
        deepDependenciesController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(ADD_TASK_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        addTaskComponent = fxmlLoader.load(url.openStream());
        addTaskController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(GRAPH_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        graphComponent = fxmlLoader.load(url.openStream());
        graphController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(RUNTASK_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        runTaskComponent = fxmlLoader.load(url.openStream());
        taskController = fxmlLoader.getController();

        fxmlLoader = new FXMLLoader();
        url = getClass().getResource(DASHBOARD_FXML_RESOURCE);
        fxmlLoader.setLocation(url);
        dashboardComponent = fxmlLoader.load(url.openStream());
        dashboardController = fxmlLoader.getController();
    }



    @FXML
    void ChangePage(ActionEvent event) {
        if (event.getSource() == dashboardButton) {
            mainChangingScene.setContent(dashboardComponent);
        } else if (event.getSource() == displayButton) {
            mainChangingScene.setContent(graphComponent);
        } else if (event.getSource() == tasksButton){
            mainChangingScene.setContent(runTaskComponent);
        } else if (event.getSource() == findPathMenuItem) {
            mainChangingScene.setContent(findPathComponent);
        } else if (event.getSource() == findCycleMenuItem) {
            mainChangingScene.setContent(findCycleComponent);
        } else if (event.getSource() == deepDependenciesMenuItem) {
            mainChangingScene.setContent(deepDependenciesComponent);
        } else if (event.getSource() == addTaskMenuItem) {
            mainChangingScene.setContent(addTaskComponent);
        }
    }

    @FXML
    void menuBarOpenButtonClicked(ActionEvent event) throws IOException {
        dashboardController.addNewGraphToList();
        mainChangingScene.setContent(dashboardComponent);
    }

    public void LoadXMLFile(File file) {
        if (file != null) {
            try {
                TargetGraph targetGraph = TargetGraph.createTargetGraphFromXml(file.toPath());
                graphController.setTargetGraph(targetGraph);
                findPathController.setTargetGraph(targetGraph);
                findCycleController.setTargetGraph(targetGraph);
                deepDependenciesController.setTargetGraph(targetGraph);
                addTaskController.setTargetGraph(targetGraph);
                taskController.setTargetGraph(targetGraph);
                isFileSelected.set(true);
            } catch (Exception e) {
                Toolkit.getDefaultToolkit().beep();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Loading error");
                alert.setHeaderText(e.getMessage());
                alert.initOwner(primaryStage);
                Optional<ButtonType> result = alert.showAndWait();
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSelectedGraphTextField(String graphName){
        this.SelectedGraphTextField.setText(graphName);
    }

    public void setSelectedTaskTextField(String taskName){
        this.SelectedTaskTextField.setText(taskName);
        this.taskController.setStopAndPauseInit();
    }

    public String getTaskName() {
        return SelectedTaskTextField.getText();
    }

    public void createIncrementalTask(String newTaskName, String selectedReloadTaskName, String type, boolean isIncremental) {
        addTaskController.uploadCopyTaskToServer(newTaskName, selectedReloadTaskName, type, userName, isIncremental);
    }
}
