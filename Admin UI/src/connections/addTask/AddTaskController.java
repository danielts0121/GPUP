package connections.addTask;

import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import main.AdminMainController;
import main.include.Constants;
import okhttp3.*;
import target.TargetGraph;
import task.compilation.CompilationParameters;
import task.compilation.CompilationTaskInformation;
import task.simulation.SimulationParameters;
import task.simulation.SimulationTaskInformation;
import util.http.HttpClientUtil;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class AddTaskController {
    @FXML
    private Pane compilationPane;

    @FXML
    private TextField compileTaskSourceTextField;

    @FXML
    private Button compileTaskSourceSearchButton;

    @FXML
    private TextField compileTaskDestTextField;

    @FXML
    private Button compileTaskDestSearchButton;

    @FXML
    private Pane simulationPane;

    @FXML
    private Spinner<Integer> simulationTimeSpinner;

    @FXML
    private CheckBox simulationRandomCheckBox;

    @FXML
    private Slider successRateSlider;

    @FXML
    private TextField successRateTextField;

    @FXML
    private Slider warningsRateSlider;

    @FXML
    private TextField warningsRateTextField;

    @FXML
    private TextField TaskNameTextField;

    @FXML
    private ComboBox<String> taskTypeComboBox;

    @FXML
    private RadioButton selectAllRadioButton;

    @FXML
    private ListView<String> TargetsListView;

    @FXML
    private Button requiredForButton;

    @FXML
    private Button dependsOnButton;

    @FXML
    private Button addButton;

    @FXML
    private ListView<String> AddedTargetsListView;

    @FXML
    private Button removeButton;

    @FXML
    private Button clearButton;


    private String lastVisitedDirectory = System.getProperty("user.home");
    private final DirectoryChooser directoryChooser = new DirectoryChooser();
    private AdminMainController mainController;
    private final SimpleBooleanProperty isSimulationPossible;
    private final SimpleBooleanProperty isCompilationPossible;
    private final SimpleIntegerProperty howManyTargetsSelected;
    private final SimpleIntegerProperty howManyTargetsAdded;
    private final SimpleBooleanProperty isNameEmpty;
    private final ListChangeListener<String> currentSelectedListListener;
    private final ListChangeListener<String> currentAddedListListener;
    private final ObservableList<String> TargetsNameList = FXCollections.observableArrayList();
    private final ObservableList<String> addedTargetsList = FXCollections.observableArrayList();
    private CompilationTaskInformation compilationTaskInformation;
    private SimulationTaskInformation simulationTaskInformation;
    private ObservableList<String> currentSelectedList = FXCollections.observableArrayList();
    private ObservableList<String> currentSelectedInAddedTargetsList = FXCollections.observableArrayList();
    private ObservableList<String> allTargetsNameList = FXCollections.observableArrayList();
    private final SpinnerValueFactory<Integer> TimeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 3000, 1000);
    String taskName = "";
    TargetGraph targetGraph;
    Gson gson;

    @FXML
    public void initialize() {
        requiredForButton.disableProperty().bind(howManyTargetsSelected.isEqualTo(1).not());
        dependsOnButton.disableProperty().bind(howManyTargetsSelected.isEqualTo(1).not());
        currentSelectedList = TargetsListView.getSelectionModel().getSelectedItems();
        currentSelectedInAddedTargetsList = AddedTargetsListView.getSelectionModel().getSelectedItems();
        currentSelectedList.addListener(currentSelectedListListener);
        addedTargetsList.addListener(currentAddedListListener);
        simulationTimeSpinner.setValueFactory(TimeValueFactory);
        addedTargetsList.clear();
        taskTypeComboBox.setItems(getTasksNames());
        taskTypeComboBox.setValue("Simulation");
        simulationPane.disableProperty().bind(isSimulationPossible.not());
        compilationPane.disableProperty().bind(isCompilationPossible.not());

        successRateSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            String successRate = new DecimalFormat("#0.0#").format(successRateSlider.getValue());
            successRateTextField.setText(successRate);
        });
        successRateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("")) {
                successRateTextField.setText(oldValue);
            } else if (!newValue.matches("\\d{0,1}([\\.]\\d{0,2})?")) {
                successRateTextField.setText(oldValue);
            } else if (Double.parseDouble(successRateTextField.getText()) > 1.0) {
                successRateTextField.setText(oldValue);
            }
            successRateSlider.setValue(Double.parseDouble(successRateTextField.getText()));
        });

        warningsRateSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            String warningsRate = new DecimalFormat("#0.0#").format(warningsRateSlider.getValue());
            warningsRateTextField.setText(warningsRate);
        });
        warningsRateTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals("")) {
                warningsRateTextField.setText(oldValue);
            } else if (!newValue.matches("\\d{0,1}([\\.]\\d{0,2})?")) {
                warningsRateTextField.setText(oldValue);
            } else if (Double.parseDouble(warningsRateTextField.getText()) > 1.0) {
                warningsRateTextField.setText(oldValue);
            }
            warningsRateSlider.setValue(Double.parseDouble(warningsRateTextField.getText()));
        });

        simulationTimeSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                simulationTimeSpinner.getValueFactory().setValue(0);
        });

        TaskNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                isNameEmpty.setValue(true);
            } else {
                isNameEmpty.setValue(false);
            }
            taskName = newValue;
        });

        TargetsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2)
                    addButton.fire();
            }
        });
        AddedTargetsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {

                if (click.getClickCount() == 2)
                    removeButton.fire();
            }
        });
    }

    public AddTaskController() {
        howManyTargetsSelected = new SimpleIntegerProperty(0);
        currentSelectedListListener = change -> howManyTargetsSelected.set(change.getList().size());
        howManyTargetsAdded = new SimpleIntegerProperty(0);
        isNameEmpty = new SimpleBooleanProperty(true);
        isSimulationPossible = new SimpleBooleanProperty(false);
        isCompilationPossible = new SimpleBooleanProperty(false);
        currentAddedListListener = change -> {
            howManyTargetsAdded.set(change.getList().size());
        };
        this.gson = new Gson();
    }

    @FXML
    void SelectAllClicked(ActionEvent event) {
        if (selectAllRadioButton.isSelected()) {
            selectAllRadioButton.setText("Deselect All");
            TargetsListView.getSelectionModel().selectAll();
        } else {
            selectAllRadioButton.setText("Select All");
            TargetsListView.getSelectionModel().clearSelection();
        }
    }

    @FXML
    void addButtonClicked(ActionEvent event) {
        for (String str : currentSelectedList)
            if (!addedTargetsList.contains(str))
                addedTargetsList.add(str);
        AddedTargetsListView.setItems(addedTargetsList);
    }

    @FXML
    void addTaskButtonClicked(ActionEvent event) {
        if (TaskNameTextField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("You must enter a name to the task.");
            Optional<ButtonType> result = alert.showAndWait();
        } else if (addedTargetsList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("The task must have at least 1 selected target.");
            Optional<ButtonType> result = alert.showAndWait();
        } else {
            String taskName = this.TaskNameTextField.getText();
            String uploader = this.mainController.getUserName();
            String graphName = this.targetGraph.getGraphName();
            Set<String> TaskTargets = new HashSet<>();
            String taskTypeRequest = null;
            String stringObject = null;
            TaskTargets.addAll(addedTargetsList);

            if (taskTypeComboBox.getValue().equals("Simulation")) {
                Integer pricing = this.targetGraph.getTaskPricing().get(TargetGraph.TaskType.SIMULATION);

                SimulationParameters simulationParameters = new SimulationParameters(
                        simulationTimeSpinner.getValue(), simulationRandomCheckBox.isSelected(),
                        successRateSlider.getValue(), warningsRateSlider.getValue());

                SimulationTaskInformation taskInfo = new SimulationTaskInformation
                        (taskName, uploader, graphName, TaskTargets, pricing, simulationParameters, false);

                taskTypeRequest = "Simulation";
                stringObject = this.gson.toJson(taskInfo);
            } else if (taskTypeComboBox.getValue().equals("Compilation")) {
                Integer pricing = this.targetGraph.getTaskPricing().get(TargetGraph.TaskType.COMPILATION);

                CompilationParameters compilationParameters = new CompilationParameters(
                        compileTaskSourceTextField.getText(), compileTaskDestTextField.getText());

                CompilationTaskInformation taskInfo = new CompilationTaskInformation(
                        taskName, uploader, graphName, TaskTargets, pricing, compilationParameters, false);

                taskTypeRequest = "Compilation";
                stringObject = this.gson.toJson(taskInfo);
            }

            uploadTaskToServer(stringObject, taskTypeRequest, null);
        }
    }

    @FXML
    void changeTaskPane(ActionEvent event) {
        if (taskTypeComboBox.getValue().equals("Simulation")) {
            simulationPane.toFront();
        } else {
            compilationPane.toFront();
        }
    }

    public ObservableList<String> getTasksNames() {
        ObservableList<String> tasks = FXCollections.observableArrayList();
        tasks.add("Simulation");
        tasks.add("Compilation");
        return tasks;
    }

    private void setAllTargetsNameList() {
        allTargetsNameList.clear();
        allTargetsNameList.addAll(targetGraph.getAllTargets().keySet());
        TargetsNameList.clear();
        TargetsNameList.addAll(targetGraph.getAllTargets().keySet());
        TargetsListView.setItems(TargetsNameList.sorted());
        TargetsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        AddedTargetsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        setAllTargetsNameList();
        isSimulationPossible.setValue(targetGraph.getTaskPricing().containsKey(TargetGraph.TaskType.SIMULATION));
        isCompilationPossible.setValue(targetGraph.getTaskPricing().containsKey(TargetGraph.TaskType.COMPILATION));
        clearButtonClicked(new ActionEvent());
    }

    @FXML
    void clearButtonClicked(ActionEvent event) {
        addedTargetsList.clear();
        AddedTargetsListView.setItems(addedTargetsList);
    }

    @FXML
    void compileTaskDestSearchButtonClicked(ActionEvent event) {
        directoryChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File dir = directoryChooser.showDialog(compileTaskDestSearchButton.getScene().getWindow());
        if (dir != null && dir.isDirectory()) {
            lastVisitedDirectory = dir.getPath();
            compileTaskDestTextField.textProperty().setValue(dir.getPath());
        }
    }

    @FXML
    void compileTaskSourceSearchButtonClicked(ActionEvent event) {
        directoryChooser.setInitialDirectory(new File(lastVisitedDirectory));
        File dir = directoryChooser.showDialog(compileTaskSourceSearchButton.getScene().getWindow());
        if (dir != null && dir.isDirectory()) {
            lastVisitedDirectory = dir.getPath();
            compileTaskSourceTextField.textProperty().setValue(dir.getPath());
        }
    }

    @FXML
    void dependsOnButtonClicked(ActionEvent event) {
        Set<String> dependsOnSet = targetGraph.getTarget(TargetsListView.getSelectionModel().
                getSelectedItem()).getAllDependsOnTargetsAsStrings();
        for (String toSelect : dependsOnSet) {
            TargetsListView.getSelectionModel().select(toSelect);
        }
    }

    @FXML
    void removeButtonClicked(ActionEvent event) {
        addedTargetsList.removeAll(currentSelectedInAddedTargetsList);
        AddedTargetsListView.setItems(addedTargetsList);
    }

    @FXML
    void requiredForButtonClicked(ActionEvent event) {
        Set<String> requiredForSet = targetGraph.getTarget(TargetsListView.getSelectionModel().
                getSelectedItem()).getAllRequiredForTargetsAsStrings();
        for (String toSelect : requiredForSet) {
            TargetsListView.getSelectionModel().select(toSelect);
        }
    }

    public void setMainController(AdminMainController mainController) {
        this.mainController = mainController;
    }

    private void uploadTaskToServer(String stringObject, String taskTypeRequest, String oldTaskName) {
        RequestBody body = RequestBody.create(stringObject, MediaType.parse("application/json"));
        Request request;
        if (oldTaskName != null) {
            request = new Request.Builder()
                    .url(Constants.TASKS_PATH)
                    .post(body)
                    .addHeader(taskTypeRequest, taskTypeRequest)
                    .addHeader("oldTaskName", oldTaskName)
                    .build();
        } else {
            request = new Request.Builder()
                    .url(Constants.TASKS_PATH)
                    .post(body)
                    .addHeader(taskTypeRequest, taskTypeRequest)
                    .build();
        }

        HttpClientUtil.runAsyncWithRequest(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("error msg is: " + e.getMessage());
                Platform.runLater(() -> errorPopup(e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (!(response.code() >= 200 && response.code() < 300)) {
                    String message = response.header("message");
                    Platform.runLater(() -> errorPopup(message));
                } else {// created task successfully
                    Platform.runLater(() -> successAlert());
                    response.close();
                }
            }
        });
    }

    public void uploadCopyTaskToServer(String newTaskName, String oldTaskName, String type, String userName, boolean isIncremental) {
        String taskTypeRequest = null;
        String stringObject = null;
        int attempts = 0;
        compilationTaskInformation = null;
        simulationTaskInformation = null;

        getCurrInformation(oldTaskName, type);
        while (compilationTaskInformation == null && simulationTaskInformation == null && attempts < 1000) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignore) {
            }
            attempts++;
        }
        if (type.equals("Simulation")) {
            taskTypeRequest = "Simulation";
            SimulationTaskInformation newSimulationTaskInformation = new SimulationTaskInformation(newTaskName, userName,
                    simulationTaskInformation.getGraphName(), simulationTaskInformation.getTargetsToExecute(),
                    simulationTaskInformation.getPricingForTarget(), simulationTaskInformation.getSimulationParameters(), isIncremental);
            stringObject = this.gson.toJson(newSimulationTaskInformation);
        } else if (type.equals("Compilation")) {
            taskTypeRequest = "Compilation";
            CompilationTaskInformation newCompilationTaskInformation = new CompilationTaskInformation(newTaskName, userName,
                    compilationTaskInformation.getGraphName(), compilationTaskInformation.getTargetsToExecute(),
                    compilationTaskInformation.getPricingForTarget(), compilationTaskInformation.getCompilationParameters(), isIncremental);
            stringObject = this.gson.toJson(newCompilationTaskInformation);
        }

        if (stringObject != null && taskTypeRequest != null)
            uploadTaskToServer(stringObject, taskTypeRequest, oldTaskName);
    }

    private void getCurrInformation(String oldTaskName, String type) {
        String finalUrl = HttpUrl
                .parse(Constants.TASKS_PATH)
                .newBuilder()
                .addQueryParameter("task", oldTaskName)
                .build()
                .toString();
        HttpClientUtil.runAsync(finalUrl, "GET", null, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorPopup(e.getMessage()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() >= 200 && response.code() < 300) //Success
                {
                    Gson gson = new Gson();
                    ResponseBody responseBody = response.body();
                    if (type.equals("Simulation"))
                        simulationTaskInformation = gson.fromJson(responseBody.string(), SimulationTaskInformation.class);
                    else
                        compilationTaskInformation = gson.fromJson(responseBody.string(), CompilationTaskInformation.class);
                    responseBody.close();
                } else //Failed
                {
                    String message = response.message();
                    Platform.runLater(() -> errorPopup(message));
                }
                Objects.requireNonNull(response.body()).close();
                response.close();
            }
        });
    }

    public void successAlert(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("The task added successfully");
        Optional<ButtonType> result = alert.showAndWait();
    }

    public void errorPopup(String message) {
        Toolkit.getDefaultToolkit().beep();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Loading error");
        alert.setHeaderText(message);
        Optional<ButtonType> result = alert.showAndWait();
    }
}
