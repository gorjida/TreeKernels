package utils;

import java.util.*;
/**
 * Created by u6042446 on 2018-06-08.
 */
public class Hypothesis {

    private List<Integer> leftNodes;
    private List<Integer> rightNodes;
    private double score;

    public Hypothesis() {
        this.leftNodes = new ArrayList<Integer>();
        this.rightNodes = new ArrayList<Integer>();
    }

    public double getScore() {return (this.score);}
    public List<Integer> getLeftNodes() {return (this.leftNodes);}
    public List<Integer> getRightNodes() {return (this.rightNodes);}

    public void setScore(double score) {this.score=score;}
    public void addLeftNode(int leftNodeIndex) {this.leftNodes.add(leftNodeIndex);}
    public void addRightNode(int rightNodeIndex) {this.rightNodes.add(rightNodeIndex);}

    @Override
    public String toString()
    {
        String value = "";
        int counter = 0;
        for (int i=0;i<leftNodes.size();i++)
        {
            value+= leftNodes.get(i)+"|"+rightNodes.get(i)+",";
        }
        value+=score;
        return (value);
    }

}
