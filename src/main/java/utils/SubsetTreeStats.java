package utils;

import java.util.*;

/**
 * Created by u6042446 on 2018-05-30.
 */
public class SubsetTreeStats {

    private int leftTreeDepth;
    private int leftTreeWidth;
    private int rightTreeDepth;
    private int rightTreeWidth;
    private double totalNumSubsets;

    public SubsetTreeStats(int leftTreeDepth,int leftTreeWidth,int rightTreeDepth,int rightTreeWidth,double totalNumSubsets)
    {
        this.leftTreeDepth = leftTreeDepth;
        this.leftTreeWidth = leftTreeWidth;
        this.rightTreeDepth = rightTreeDepth;
        this.rightTreeWidth = rightTreeWidth;
        this.totalNumSubsets = totalNumSubsets;
    }

    public int getLeftTreeDepth() {return (this.leftTreeDepth);}
    public int getLeftTreeWidth() {return (this.leftTreeWidth);}
    public int getRightTreeDepth() {return (this.rightTreeDepth);}
    public int getRightTreeWidth() {return (this.rightTreeWidth);}
    public double getTotalNumSubsets() {return (this.totalNumSubsets);}
}
