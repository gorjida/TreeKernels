package utils;

/**
 * Created by u6042446 on 2018-05-22.
 */

import java.util.List;
import java.util.ArrayList;
public class SubsetTreeNode {

    private utils.TreeNode leftParent;
    private utils.TreeNode rightParent;
    private double simScore;
    private double decayFactor;
    private double nodesContribution;
    private int depth;
    private int width;
    private List<SubsetTreeNode> listOfChildren;

    public SubsetTreeNode(utils.TreeNode leftParent,utils.TreeNode rightParent,double simScore)
    {
        this.leftParent = leftParent;
        this.rightParent = rightParent;
        this.simScore = simScore;
        this.listOfChildren = new ArrayList<SubsetTreeNode>();
        //this.depth = Math.max(leftParent.getDepth(),rightParent.getDepth());
        //this.width = leftParent.getWidth()+rightParent.getWidth();
        this.width = 1;
        this.depth = 0;
        this.nodesContribution = 1;
        this.decayFactor = 1;
    }

    public utils.TreeNode getLeftParent() {return (this.leftParent);}
    public utils.TreeNode getRightParent() {return (this.rightParent);}
    public double getSimScore() {return (this.simScore);}
    public double getNodesContribution() {return (this.nodesContribution);}

    public int getDepth() {return (this.depth);};
    public int getWidth() {return (this.width);};
    public List<SubsetTreeNode> getListOfChildren() {return (this.listOfChildren);}

    public double getDecayFactor() {return (this.decayFactor);}

    public void setSimScore(double score) {this.simScore=score;}
    public void setListOfChildren(List<SubsetTreeNode> listOfChildren) {this.listOfChildren = listOfChildren;}

    public void setDepth(int depth) {this.depth=depth;}
    public void setWidth(int width) {this.width=width;}
    public void setDecayFactor(double decayFactor) {this.decayFactor=decayFactor;}

    public void setNodesContribution(double nodesContribution) {this.nodesContribution = nodesContribution;}

}
