package connections.findPath;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import main.AdminMainController;
import target.TargetGraph;

public class FindPathController {
    @FXML
    private ComboBox<String> sourceComboBox;

    @FXML
    private ComboBox<String> destinationComboBox;

    @FXML
    private RadioButton pathRequiredForRadioButton;

    @FXML
    private RadioButton pathDependsOnRadioButton;

    @FXML
    private ListView<String> pathListView;

    String sourceTargetName;
    String destinationTargetName;
    TargetGraph targetGraph;
    ObservableList<String> pathList = FXCollections.observableArrayList();
    ObservableList<String> allTargetsNameList = FXCollections.observableArrayList();
    private final SimpleBooleanProperty isSourceTargetSelected;
    private final SimpleBooleanProperty isDestinationTargetSelected;
    private AdminMainController adminMainController;

    public void initialize() {
        destinationComboBox.disableProperty().bind(isSourceTargetSelected.not());
        pathListView.disableProperty().bind(isDestinationTargetSelected.not());

    }

    public FindPathController() {
        isSourceTargetSelected = new SimpleBooleanProperty(false);
        isDestinationTargetSelected = new SimpleBooleanProperty(false);
    }

    public void setMainController(AdminMainController adminMainController) {
        this.adminMainController = adminMainController;
    }

    @FXML
    void destinationComboBoxClicked(ActionEvent event) {
        destinationTargetName = destinationComboBox.getValue();
        pathList.clear();
        if(destinationTargetName != null) {
            isDestinationTargetSelected.set(true);
            refreshPathList();
        }
    }

    @FXML
    void pathDependsOnKeyboardPress(KeyEvent event) {
        if(isDestinationTargetSelected.get()) {
            refreshPathList();
        }
    }

    @FXML
    void pathDependsOnRadioButtonClicked(ActionEvent event) {
        if(isDestinationTargetSelected.get()) {
            if(pathRequiredForRadioButton.isSelected()){
                pathRequiredForRadioButton.setSelected(false);
            }
            refreshPathList();
        }
    }

    @FXML
    void pathRequiredForKeyboardPress(KeyEvent event) {
        if(isDestinationTargetSelected.get()) {
            refreshPathList();
        }
    }

    @FXML
    void pathRequiredForRadioButtonClicked(ActionEvent event) {
        if(isDestinationTargetSelected.get()) {
            if(pathDependsOnRadioButton.isSelected()){
                pathDependsOnRadioButton.setSelected(false);
            }
            refreshPathList();
        }
    }

    @FXML
    void sourceComboBoxClicked(ActionEvent event) {
        sourceTargetName = sourceComboBox.getValue();
        pathList.clear();
        if(sourceTargetName != null)
            isSourceTargetSelected.set(true);
        if(isDestinationTargetSelected.get())
            refreshPathList();
    }

    private void refreshPathList() {
        pathList.clear();
        if(pathRequiredForRadioButton.isSelected()) {
            pathList.addAll(targetGraph.getAllPathsFromTwoTargetsAsStrings(sourceTargetName,destinationTargetName, TargetGraph.pathDirection.REQUIRED_FOR));
        }
        else {
            pathList.addAll(targetGraph.getAllPathsFromTwoTargetsAsStrings(sourceTargetName,destinationTargetName, TargetGraph.pathDirection.DEPENDS_ON));
        }
        if(pathList.isEmpty())
            pathList.add("There is no path between those two targets in this direction");
        pathListView.setItems(pathList);
    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        isSourceTargetSelected.set(false);
        isDestinationTargetSelected.set(false);
        setAllTargetsNameList();
        pathList.clear();
    }

    private void setAllTargetsNameList() {
        allTargetsNameList.clear();
        allTargetsNameList.addAll(targetGraph.getAllTargets().keySet());
        sourceComboBox.setItems(allTargetsNameList.sorted());
        destinationComboBox.setItems(allTargetsNameList.sorted());
    }
}
