package graph;

import graph.tableview.TargetTableItem;
import graph.tableview.TargetTypeSummery;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import target.Target;
import target.TargetGraph;


public class GraphController {
    @FXML
    private TableView<TargetTableItem> dependenciesTableView;

    @FXML
    private TableColumn<TargetTableItem, String> name;

    @FXML
    private TableColumn<TargetTableItem, String> type;

    @FXML
    private TableColumn<TargetTableItem, Integer> dependsOnDirectly;

    @FXML
    private TableColumn<TargetTableItem, Integer> requiredForDirectly;

    @FXML
    private TableColumn<TargetTableItem, String> extraData;

    @FXML
    private TableView<TargetTypeSummery> typeTableView;

    @FXML
    private TableColumn<TargetTypeSummery, Integer> targetsAmount;

    @FXML
    private TableColumn<TargetTypeSummery, Integer> independentAmount;

    @FXML
    private TableColumn<TargetTypeSummery, Integer> leafAmount;

    @FXML
    private TableColumn<TargetTypeSummery, Integer> middleAmount;

    @FXML
    private TableColumn<TargetTypeSummery, Integer> rootAmount;

    private static final int CHAR_WIDTH=8;
    private final ObservableList<TargetTableItem> targetTableList = FXCollections.observableArrayList();
    private final ObservableList<TargetTypeSummery> typeSummeryList = FXCollections.observableArrayList();
    private TargetGraph targetGraph;

    @FXML
    public void initialize() {
        initializeTargetTable();
        initializeTypeSummeryTable();
    }


    public void initializeTargetTable() {
        name.setCellValueFactory(new PropertyValueFactory<TargetTableItem, String>("Name"));
        type.setCellValueFactory(new PropertyValueFactory<TargetTableItem, String>("Type"));
        dependsOnDirectly.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("DependsOnDirectly"));
        requiredForDirectly.setCellValueFactory(new PropertyValueFactory<TargetTableItem, Integer>("RequiredForDirectly"));
        extraData.setCellValueFactory(new PropertyValueFactory<TargetTableItem, String>("ExtraData"));
    }

    public void initializeTypeSummeryTable() {
        targetsAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("TotalAmountOfTargets"));
        rootAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Root"));
        middleAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Middle"));
        leafAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Leaf"));
        independentAmount.setCellValueFactory(new PropertyValueFactory<TargetTypeSummery, Integer>("Independent"));
    }

    public void setTargetGraph(TargetGraph targetGraph) {
        this.targetGraph = targetGraph;
        initialize();
        setDependenciesTable();
        setTypeSummeryTable();
    }

    private void setDependenciesTable() {
        TargetTableItem currentItem;
        int prefWidth = 0;
        targetTableList.clear();
        for (Target target : targetGraph.getAllTargets().values()) {
            prefWidth = Math.max(prefWidth,CHAR_WIDTH*target.getExtraData().length());
            currentItem = new TargetTableItem(target);
            targetTableList.add(currentItem);
        }

        extraData.setPrefWidth(prefWidth);
        dependenciesTableView.setItems(targetTableList);
    }

    private void setTypeSummeryTable() {
        typeSummeryList.clear();
        TargetTypeSummery typeSummeryItem = new TargetTypeSummery(targetGraph);
        typeSummeryList.add(typeSummeryItem);
        typeTableView.setItems(typeSummeryList);
    }
}

