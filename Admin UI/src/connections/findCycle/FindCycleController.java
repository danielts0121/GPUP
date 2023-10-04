package connections.findCycle;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import main.AdminMainController;
import target.TargetGraph;

public class FindCycleController {
    @FXML
    private ListView<String> circleListView;

    @FXML
    private ComboBox<String> circleTargetComboBox;

    String circleTargetName;
    private final SimpleBooleanProperty isCircleTargetSelected;
    ObservableList<String> circleList = FXCollections.observableArrayList();
    ObservableList<String> allTargetsNameList = FXCollections.observableArrayList();
    TargetGraph targetGraph;
    private AdminMainController adminMainController;

    public void initialize() {
        circleListView.disableProperty().bind(isCircleTargetSelected.not());
    }

    public FindCycleController(){
        isCircleTargetSelected = new SimpleBooleanProperty(false);
    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        isCircleTargetSelected.set(false);
        setAllTargetsNameList();
    }

    public void setMainController(AdminMainController adminMainController) {
        this.adminMainController = adminMainController;
    }

    @FXML
    void circleTargetComboBoxClicked(ActionEvent event) {
        circleTargetName = circleTargetComboBox.getValue();
        circleList.clear();
        if(circleTargetName != null) {
            isCircleTargetSelected.set(true);
            circleList.add(targetGraph.checkIfTargetIsInACircleAndReturnCircleAsString(circleTargetName));
        }
        circleListView.setItems(circleList);
    }

    private void setAllTargetsNameList() {
        allTargetsNameList.clear();
        allTargetsNameList.addAll(targetGraph.getAllTargets().keySet());
        circleTargetComboBox.setItems(allTargetsNameList.sorted());
    }
}
