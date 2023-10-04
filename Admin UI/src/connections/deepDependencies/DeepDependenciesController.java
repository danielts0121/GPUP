package connections.deepDependencies;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import main.AdminMainController;
import target.TargetGraph;

public class DeepDependenciesController {
    @FXML
    private ListView<String> whatIfListView;

    @FXML
    private ComboBox<String> whatIfTargetComboBox;

    @FXML
    private RadioButton whatIfRequiredForRadioButton;

    @FXML
    private RadioButton whatIfDependsOnRadioButton;

    private final SimpleBooleanProperty isWhatIfTargetSelected;
    String whatIfTargetName;
    ObservableList<String> whatIfList = FXCollections.observableArrayList();
    TargetGraph targetGraph;
    ObservableList<String> allTargetsNameList = FXCollections.observableArrayList();
    private AdminMainController adminMainController;
    @FXML
    public void initialize() {
        whatIfListView.disableProperty().bind(isWhatIfTargetSelected.not());
    }

    public DeepDependenciesController(){
        isWhatIfTargetSelected = new SimpleBooleanProperty(false);
    }

    @FXML
    void whatIfDependsOnClicked(ActionEvent event) {
        if(isWhatIfTargetSelected.get()) {
            if (whatIfRequiredForRadioButton.isSelected()) {
                whatIfRequiredForRadioButton.setSelected(false);
            }
            refreshWhatIfList();
        }
    }

    @FXML
    void whatIfDependsOnKeyBoardPressed(KeyEvent event) {
        if(isWhatIfTargetSelected.get())
            refreshWhatIfList();
    }

    @FXML
    void whatIfRequiredForClicked(ActionEvent event) {
        if(isWhatIfTargetSelected.get()) {
            if (whatIfDependsOnRadioButton.isSelected()) {
                whatIfDependsOnRadioButton.setSelected(false);
            }
            refreshWhatIfList();
        }
    }

    @FXML
    void whatIfRequiredForKeyBoardPressed(KeyEvent event) {
        if(isWhatIfTargetSelected.get())
            refreshWhatIfList();
    }

    @FXML
    void whatIfTargetComboBoxClicked(ActionEvent event) {
        whatIfTargetName = whatIfTargetComboBox.getValue();
        if(whatIfTargetName != null) {
            isWhatIfTargetSelected.set(true);
            refreshWhatIfList();
        }
    }

    private void refreshWhatIfList() {
        whatIfList.clear();
        if(isWhatIfTargetSelected.get()) {
            if (whatIfRequiredForRadioButton.isSelected()) {
                whatIfList.addAll(targetGraph.getTarget(whatIfTargetName).getAllRequiredForTargetsAsStrings());
                if(whatIfList.isEmpty())
                    whatIfList.add("The target is not required for any other targets");
            }
            else {
                whatIfList.addAll(targetGraph.getTarget(whatIfTargetName).getAllDependsOnTargetsAsStrings());
                if(whatIfList.isEmpty())
                    whatIfList.add("The target does not depend on any other targets");
            }
        }
        whatIfListView.setItems(whatIfList);
    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        isWhatIfTargetSelected.set(false);
        setAllTargetsNameList();
    }

    private void setAllTargetsNameList() {
        allTargetsNameList.clear();
        allTargetsNameList.addAll(targetGraph.getAllTargets().keySet());
        whatIfTargetComboBox.setItems(allTargetsNameList.sorted());
    }

    public void setMainController(AdminMainController adminMainController) {
        this.adminMainController = adminMainController;
    }
}
