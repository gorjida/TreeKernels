package utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TreeNode implements Comparable<TreeNode> {

    public String value;
    public Integer hashkey;
    public List<TreeNode> childrens;
    public TreeNode parents;
    public String relp;
    public int depth;
    public int width;

    public TreeNode() {
        this.childrens = new ArrayList<TreeNode>();
        this.depth = 0;
        this.width = 1;
    }

    public TreeNode(TreeNode node) {
        this.depth = 0;
        this.width = 1;
        this.value = node.getValue();
        this.hashkey = node.getHashkey();
        this.relp = node.getRelp();
        this.childrens = new ArrayList<TreeNode>();
    }

    public TreeNode(String value, Integer hashkey) {
        this.depth = 0;
        this.width = 1;
        this.childrens = new ArrayList<TreeNode>();
        this.value = value;
        this.hashkey = hashkey;
    }

    public TreeNode(String value, Integer hashkey, String relp) {
        this.depth = 0;
        this.width = 1;
        this.childrens = new ArrayList<TreeNode>();
        this.value = value;
        this.hashkey = hashkey;
        this.relp = relp;
    }

    public String getRelp() {
        return relp;
    }


    public void setRelp(String relp) {
        this.relp = relp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getHashkey() {
        return hashkey;
    }

    public void setHashkey(Integer hashkey) {
        this.hashkey = hashkey;
    }

    public List<TreeNode> getChildrens() {
        return childrens;
    }

    public void setChildrens(List<TreeNode> childrens) {
        this.childrens = childrens;
    }

    public TreeNode getParents() {
        return parents;
    }

    public void setParents(TreeNode parents) {
        this.parents = parents;
    }

    public void addChild(TreeNode child) {
        this.childrens.add(child);
    }

    public int getChildIndex(TreeNode child) {
        return this.childrens.indexOf(child);
    }

    public void addChild(TreeNode child, int i) {
        this.childrens.add(i, child);
    }

    public int getDepth() {return this.depth;}
    public void setDepth(int depth) {this.depth = depth;}

    public int getWidth() {return this.width;}
    public void setWidth(int width) {this.width = width;}

    public void replaceChild(TreeNode ochild, TreeNode nchild) {
        int index = this.childrens.indexOf(ochild);
        this.childrens.remove(ochild);
        this.childrens.add(index, nchild);
    }

    public static String printTreeBF(utils.TreeNode tree) {
        LinkedList<utils.TreeNode> buffer = new LinkedList<utils.TreeNode>();
        buffer.add(tree);
        String output = "";
        while (!buffer.isEmpty()) {
            utils.TreeNode node = buffer.pop();

            output+= node.value+",";
            for (utils.TreeNode child: node.childrens) buffer.add(child);
        }
        output+= tree.getDepth()+","+tree.getWidth();
        return (output);
    }

    @Override
    public int compareTo(TreeNode o) {
        // TODO Auto-generated method stub
        System.out.println("wrong contains");
        if (this.hashkey == o.hashkey)
            return 0;
        else if (this.hashkey < o.hashkey)
            return -1;
        else
            return 1;
    }

}
