package utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;

public class TreeNode implements Comparable<TreeNode> {

    public String value;
    public Integer hashkey;
    public List<TreeNode> childrens;
    public TreeNode parents;
    public String relp;
    public List<String> grammaticalRelations;
    public String tag;
    public int depth;
    public int width;
    public Vector<Integer> vector;
    //A few more data-points
    //Embedding vector
    //StanfordStandardBased tag representation
    //UDDBased tag representation

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

    public TreeNode(String value,String tag, List<String> grammaticalRelations, Integer index,Enums.VectorizationType vectorizationType) {
        this.depth = 0;
        this.width = 1;
        this.childrens = new ArrayList<TreeNode>();
        this.value = value;
        this.tag = tag;
        this.grammaticalRelations = grammaticalRelations;
        this.hashkey = index;

        this.vector = createVectorRepresentation(this.tag,this.grammaticalRelations,vectorizationType);



    }

    public String getRelp() {
        return relp;
    }

    public String getTag() {return this.tag;}

    public List<String> getGrammaticalRelations() {return this.grammaticalRelations;}

    public Vector<Integer> getVector() {return this.vector;}

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

    /**
     *
     * @param posTag
     * @return
     */
    public static Vector<Integer> createVectorRepresentation(String posTag,List<String> relationTag,Enums.VectorizationType vectorizationType)
    {

        Vector<Integer> vectorRepresentation = new Vector<Integer>();

        //new int[Enums.PartOfSpeech.getNumPosTags()+Enums.StanfordDependencyRelations.getNumRelationTags()];
        if (vectorizationType==Enums.VectorizationType.StandardStanford)
        {
            int posTagIndex = Enums.PartOfSpeech.getPOSIndex(posTag);

            int bias = Enums.PartOfSpeech.getNumPosTags();
            Set<Integer> relationTagIndices = new HashSet<Integer>();
            for (String relation: relationTag) {
                int relationTagIndex = Enums.StanfordDependencyRelations.getRelationIndex(relation);
                relationTagIndices.add(bias+relationTagIndex);
            }
            for (int index=0;index<Enums.PartOfSpeech.getNumPosTags()+Enums.StanfordDependencyRelations.getNumRelationTags();index+=1)
            {
                if (index==posTagIndex || relationTagIndices.contains(index))
                {
                    vectorRepresentation.add(1);
                } else {
                    vectorRepresentation.add(0);
                }
            }
        } else if (vectorizationType==Enums.VectorizationType.UDV1)
        {
            //Add vector initiation by UDV
        } else if (vectorizationType==Enums.VectorizationType.WordIdentity)
        {
            //Add vector initiation by wordIdentity
        }

        return (vectorRepresentation);
    }


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
        output+=tree.hashkey+","+tree.getDepth()+","+tree.getWidth();
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
