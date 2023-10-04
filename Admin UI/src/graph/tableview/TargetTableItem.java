package graph.tableview;

import target.Target;

public class TargetTableItem {
    private String name;
    private String type;
    private int dependsOnDirectly;
    private int dependsOnTotal;
    private int requiredForDirectly;
    private int requiredForTotal;
    private int amountOfSerialSets;
    private String extraData;

    public TargetTableItem(Target target)
    {
        this.name = target.getName();
        this.type = target.getNodeTypeAsString();
        this.dependsOnDirectly = target.getAmountOfDirectlyDependsOn();
        this.dependsOnTotal = target.getAmountOfTotalDependsOn();
        this.requiredForDirectly = target.getAmountOfDirectlyRequiredFor();
        this.requiredForTotal = target.getAmountOfTotalRequiredFor();
        this.extraData = target.getExtraData();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDependsOnDirectly() {
        return dependsOnDirectly;
    }

    public void setDependsOnDirectly(int dependsOnDirectly) {
        this.dependsOnDirectly = dependsOnDirectly;
    }

    public int getDependsOnTotal() {
        return dependsOnTotal;
    }

    public void setDependsOnTotal(int dependsOnTotal) {
        this.dependsOnTotal = dependsOnTotal;
    }

    public int getRequiredForDirectly() {
        return requiredForDirectly;
    }

    public void setRequiredForDirectly(int requiredForDirectly) {
        this.requiredForDirectly = requiredForDirectly;
    }

    public int getRequiredForTotal() {
        return requiredForTotal;
    }

    public void setRequiredForTotal(int requiredForTotal) {
        this.requiredForTotal = requiredForTotal;
    }

    public int getAmountOfSerialSets() {
        return amountOfSerialSets;
    }

    public void setAmountOfSerialSets(int amountOfSerialSets) {
        this.amountOfSerialSets = amountOfSerialSets;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}
