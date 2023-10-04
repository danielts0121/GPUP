package graph.tableview;

import target.TargetGraph;

public class TargetTypeSummery {
    private int totalAmountOfTargets;
    private int independent;
    private int leaf;
    private int middle;
    private int root;

    public TargetTypeSummery(TargetGraph graph)
    {
        this.totalAmountOfTargets = graph.getAmountOfTargets();
        this.independent = graph.getAmountOfIndependent();
        this.leaf = graph.getAmountOfLeaves();
        this.middle = graph.getAmountOfMiddles();
        this.root = graph.getAmountOfRoots();
    }

    public int getTotalAmountOfTargets() {
        return totalAmountOfTargets;
    }

    public void setTotalAmountOfTargets(int totalAmountOfTargets) {
        this.totalAmountOfTargets = totalAmountOfTargets;
    }

    public int getIndependent() {
        return independent;
    }

    public void setIndependent(int independent) {
        this.independent = independent;
    }

    public int getLeaf() {
        return leaf;
    }

    public void setLeaf(int leaf) {
        this.leaf = leaf;
    }

    public int getMiddle() {
        return middle;
    }

    public void setMiddle(int middle) {
        this.middle = middle;
    }

    public int getRoot() {
        return root;
    }

    public void setRoot(int root) {
        this.root = root;
    }
}
