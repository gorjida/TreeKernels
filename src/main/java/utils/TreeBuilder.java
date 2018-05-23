package utils;


import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.ud.CoNLLUDocumentReader;
import edu.stanford.nlp.util.Generics;
import sun.awt.image.ImageWatched;

public class TreeBuilder {

    public TreeNode root;
    public Map<Integer, TreeNode> treemap;

    public TreeBuilder() {
        super();
        this.treemap = new HashMap<Integer, TreeNode>();
    }


    public static String[] customSplit(String input,String delimitter) {

        String[] splitted = input.split(delimitter);
        String leftSplitted = "";
        String rightSplitted = "";

        int counter = 0;
        for (int i=0;i<splitted.length-1;i++) {
            if (counter!=splitted.length-2) {
                leftSplitted+= splitted[i]+delimitter;
            } else {
                leftSplitted+= splitted[i];
            }
            counter+=1;
        }
        rightSplitted = splitted[splitted.length-1];

        String[] finalSplitted = new String[2];
        finalSplitted[0] = leftSplitted;
        finalSplitted[1] = rightSplitted;
        return (finalSplitted);
    }


    /**
     * Initialize the tree using semanticGraph
     * @param graph
     * @throws IOException
     */
    public void initTreeFromUD(SemanticGraph graph,Enums.VectorizationType vectorizationType) throws IOException {
        this.treemap = new HashMap<Integer, TreeNode>();

        //Define the reader
        //CoNLLUDocumentReader reader = new CoNLLUDocumentReader();
        //Iterator<SemanticGraph> it = reader.getIterator(IOUtils.readerFromString(treeBankFilePath));
        //List<SemanticGraph> graphs = Generics.newArrayList();

        //First, build a root
        utils.TreeNode node1 = new utils.TreeNode("ROOT", -1);
        this.root = node1;
        this.treemap.put(-1,node1);

        //LinkedList<IndexedWord> listOfNodes = new LinkedList<IndexedWord>();
        //listOfNodes.add(node1);


        Collection<IndexedWord> listOfRoots = graph.getRoots();

        LinkedList<IndexedWord> listOfNodes = new LinkedList<IndexedWord>();
        for (IndexedWord root: listOfRoots){
            //Extract required information
            String nodeValue = root.value();//Actual value of each node
            String posTag = root.tag();//PartOfSpeechTag for this node
            List<String> relations = new ArrayList<String>();//List of Grammatical relations
            for (GrammaticalRelation rel: graph.relns(root)) relations.add(rel.toString());
            int nodeIndex = root.index();
            //utils.TreeNode node2 = new utils.TreeNode(root.value(),root.index());
            utils.TreeNode node2 = new utils.TreeNode(nodeValue,posTag,relations,nodeIndex,vectorizationType);
            node1.addChild(node2);
            node2.setParents(node1);
            listOfNodes.add(root);
            this.treemap.put(root.index(),node2);
        }

        while (!listOfNodes.isEmpty()) {

            IndexedWord currentNode = listOfNodes.pop();
            node1 = this.treemap.get(currentNode.index());
            //Iterate over all the children
            for (IndexedWord child: graph.getChildren(currentNode)) {
                List<String> relations = new ArrayList<String>();//List of Grammatical relations
                for (GrammaticalRelation rel: graph.relns(child)) relations.add(rel.toString());
               utils.TreeNode node2 = new utils.TreeNode(child.value(),child.tag(),relations,child.index(),vectorizationType);
               node1.addChild(node2);
               node2.setParents(node1);
               listOfNodes.add(child);
               this.treemap.put(child.index(),node2);
            }
        }
    }
    /**
     * Task this method takes the list-based parsedTree and converts it to a Tree-structure that could be used for the kernel computation
     * @param depens
     * @return A boolean indicator that shows if a ROOT has been found in the tree?
     * The code fills the HashMap "treemap" whose Key=node_index and Value=SubTree_of_the_node
     */
    public boolean initTreeStringInput(List<String> depens){
        this.treemap = new HashMap<Integer, TreeNode>();
        boolean hasRoot = false;
        boolean flag = false;
        for (String t : depens) {
            //System.out.print(t+"\n");
            //Process this string and extract gov and dep
            String trimmedString1 = t.replace("("," ").replace(")","");
            String trimmedString = "";
            if (trimmedString1.split(" ")[0].compareTo("")==0) {
                trimmedString = trimmedString1.replace(" ,"," ");
            } else {
                trimmedString = trimmedString1.replace(", "," ");
            }
            //String trimmedString = t.replace("("," ").replace(")","").replace(", "," ");
            String[] splittedString = trimmedString.split(" ");
            String[] splittedGov = customSplit(splittedString[1],"-");
            String[] splittedDep = customSplit(splittedString[2],"-");


            String govValue = splittedGov[0];
            String depValue = splittedDep[0];
            int govIndex = Integer.parseInt(splittedGov[1])-1;
            int depIndex = Integer.parseInt(splittedDep[1])-1;

            //Extract index, dependency

            TreeNode node1 = new TreeNode();
            TreeNode node2 = new TreeNode();


            if (this.treemap.containsKey(govIndex)) {
                node1 = this.treemap.get(govIndex);
            } else {

                node1 = new TreeNode(govValue, govIndex);

                if (flag == false && govValue.equals("ROOT")) {
                    hasRoot = true;
                    this.root = node1;
                    flag = true;
                    this.treemap.put(-3, node1);
                } else {
                    this.treemap.put(govIndex, node1);
                }
            }

            if (this.treemap.containsKey(depIndex)) {
                node2 = this.treemap.get(depIndex);
                node2.setRelp("NONE");
            } else {
                node2 = new TreeNode(depValue, depIndex, "NONE");
                this.treemap.put(depIndex, node2);
            }

            node1.addChild(node2);
            node2.setParents(node1);
        }

        return (hasRoot);
    }

    public void initTree(List<TypedDependency> depens){
        this.treemap = new HashMap<Integer, TreeNode>();

        boolean flag = false;
        for (TypedDependency t : depens) {

            TreeNode node1 = new TreeNode();
            TreeNode node2 = new TreeNode();

            if (this.treemap.containsKey(t.gov().index()-1)) {
                node1 = this.treemap.get(t.gov().index()-1);
            } else {
                node1 = new TreeNode(t.gov().value(), t.gov().index()-1);

                if (flag == false && t.gov().value().equals("ROOT")) {
                    this.root = node1;
                    flag = true;
                    this.treemap.put(-3, node1);
                } else {
                    this.treemap.put(t.gov().index()-1, node1);
                }
            }

            if (this.treemap.containsKey(t.dep().index()-1)) {
                node2 = this.treemap.get(t.dep().index()-1);
                node2.setRelp(t.reln().toString());
            } else {
                node2 = new TreeNode(t.dep().value(), t.dep().index()-1, t.reln().toString());
                this.treemap.put(t.dep().index()-1, node2);
            }

            node1.addChild(node2);
            node2.setParents(node1);
        }
    }

    public String toPennString() {
        StringBuilder sb =  new StringBuilder();
        subPennString(sb, this.root);
        return sb.toString();
    }

    public void subPennString(StringBuilder sb, TreeNode tree) {
        sb.append(" (");
        sb.append(tree.getValue());
        for (TreeNode tn : tree.getChildrens()) {
            this.subPennString(sb, tn);
        }
        sb.append(")");
    }

    public TreeBuilder convertToGrammaticalRelationTree() {
        TreeBuilder dt = new TreeBuilder();
        TreeNode newroot = new TreeNode(this.root);
        dt.root = newroot;
        dt.treemap.put(dt.root.getHashkey(), dt.root);
        if (!dt.root.getValue().equals("ROOT")) {
            dt.root.setValue(dt.root.getRelp());
        }
        this.subConvertToGrammaticalRelationTree(this.root, newroot, dt);
        return dt;
    }

    public void subConvertToGrammaticalRelationTree(TreeNode node, TreeNode newNode, TreeBuilder dt) {
        for (TreeNode n : node.getChildrens()) {
            TreeNode newtmp = new TreeNode(n);
            newNode.addChild(newtmp);
            newtmp.setParents(newNode);
            newtmp.setValue(newtmp.getRelp());
            dt.treemap.put(newtmp.getHashkey(), newtmp);
            this.subConvertToGrammaticalRelationTree(n, newtmp, dt);
        }
    }

    public TreeBuilder convertToGrammaticalRelationWordTree() {
        TreeBuilder dt = new TreeBuilder();
        TreeNode newroot = new TreeNode(this.root);
        dt.root = newroot;
        dt.treemap.put(dt.root.getHashkey(), dt.root);
        this.subConvertToGrammaticalRelationWordTree(this.root, newroot, dt);
        if (dt.treemap.containsKey(-2)) {
            this.tinyChangeOfGRW(dt);
        }
        return dt;
    }

    public void subConvertToGrammaticalRelationWordTree(TreeNode node, TreeNode newNode, TreeBuilder dt) {
        for (TreeNode n : node.getChildrens()) {

            if ( (this.treemap.containsKey(-1) && this.treemap.get(-1) == n)
                    || (this.treemap.containsKey(-2) && this.treemap.get(-2) == n)) {
                TreeNode newtmp = new TreeNode(n);
                newtmp.setParents(newNode);
                newNode.addChild(newtmp);
                dt.treemap.put(newtmp.getHashkey(), newtmp);
                this.subConvertToGrammaticalRelationWordTree(n, newtmp, dt);
                continue;
            }
            TreeNode newtmp = new TreeNode(n);

            TreeNode newtmp2 = new TreeNode();
            newtmp2.setValue(newtmp.getRelp());
            newtmp2.setParents(newNode);
            newtmp2.addChild(newtmp);

            newtmp.setParents(newtmp2);
            newNode.addChild(newtmp2);

            dt.treemap.put(newtmp.getHashkey(), newtmp);
            this.subConvertToGrammaticalRelationWordTree(n, newtmp, dt);
        }
    }

    public void tinyChangeOfGRW(TreeBuilder dt){
        TreeNode t1 = dt.treemap.get(-1);
        TreeNode t2 = dt.treemap.get(-2);
        TreeNode root = dt.root;
        if (t1.equals(root)) {
            TreeNode t1c = t1.getChildrens().get(0);
            t1.setChildrens(t1c.getChildrens());
            t1.setParents(t1c);
            List<TreeNode> tmp = new ArrayList<TreeNode>();
            tmp.add(t1);
            t1c.setChildrens(tmp);
            dt.root = t1c;
        } else {
            TreeNode t1c = t1.getChildrens().get(0);
            t1.setChildrens(t1c.getChildrens());
            t1c.setParents(t1.getParents());

            t1.parents.replaceChild(t1, t1c);

            t1.setParents(t1c);
            List<TreeNode> tmp = new ArrayList<TreeNode>();
            tmp.add(t1);
            t1c.setChildrens(tmp);
        }
        if (t2.equals(root)) {
            TreeNode t1c = t2.getChildrens().get(0);
            t2.setChildrens(t1c.getChildrens());
            t2.setParents(t1c);
            List<TreeNode> tmp = new ArrayList<TreeNode>();
            tmp.add(t2);
            t1c.setChildrens(tmp);
            dt.root = t1c;
        } else {
            TreeNode t1c = t2.getChildrens().get(0);
            t2.setChildrens(t1c.getChildrens());
            t1c.setParents(t2.getParents());

            t2.parents.replaceChild(t2, t1c);

            t2.setParents(t1c);
            List<TreeNode> tmp = new ArrayList<TreeNode>();
            tmp.add(t2);
            t1c.setChildrens(tmp);
        }
    }

    public TreeBuilder convertToSQGRWTree() {
        TreeBuilder dt = new TreeBuilder();
        List<TreeNode> path = this.findPath(this.treemap.get(-1), this.treemap.get(-2));
        TreeNode root = new TreeNode();
        root.setValue("ROOT");
        dt.root = root;
        for (TreeNode tn : path) {
            TreeNode tmp = new TreeNode(tn);
            root.addChild(tmp);
        }
        return dt;
    }

    public List<TreeNode> findPath(TreeNode tn1, TreeNode tn2) {
        List<TreeNode> tnList = new ArrayList<TreeNode>();
        List<Integer> para = new ArrayList<Integer>();
        para.add(tn1.getHashkey());
        para.add(tn2.getHashkey());
        TreeNode commonParent = this.findLowestCommonParent(para);
        this.findSubPath(tnList, commonParent, tn1);
        tnList.add(commonParent);

        List<TreeNode> tmpList = new ArrayList<TreeNode>();
        this.findSubPath(tmpList, commonParent, tn2);
        for (int i = tmpList.size()-1; i>=0; i--) {
            tnList.add(tmpList.get(i));
        }
        return tnList;
    }

    public List<TreeNode> findRootPath(TreeNode tn) {
        List<TreeNode> tnList = new ArrayList<TreeNode>();
        this.findSubPath(tnList, this.root, tn);
        return tnList;
    }

    public boolean findSubPath(List<TreeNode> path, TreeNode start, TreeNode end) {
        for (TreeNode tn : start.getChildrens()) {
            if (tn.equals(end)) {
                path.add(tn);
                return true;
            } else {
                if (this.findSubPath(path, tn, end)) {
                    path.add(tn);
                    return true;
                }
            }
        }
        return false;
    }

    public void addTargets(List<Integer> index1, String targetName1, List<Integer> index2, String targetName2) {
        this.addTarget(index1, targetName1, -1);
        this.addTarget(index2, targetName2, -2);
    }

    public void addTarget(List<Integer> index, String targetName, int mapindex) {
        TreeNode commonParent = this.findLowestCommonParent(index);

//  System.out.println(commonParent.getValue());
//  System.out.println(commonParent.getParents().getValue());
        TreeNode tNode = new TreeNode();
        tNode.setValue(targetName);
        tNode.setRelp(targetName);
        tNode.setHashkey(mapindex);

        this.treemap.put(mapindex, tNode);

        if (commonParent.getChildrens().size()==1 && index.size()==1) {
            TreeNode tmpNode = commonParent.getChildrens().get(0);
            commonParent.replaceChild(tmpNode, tNode);
            tNode.addChild(tmpNode);
            tNode.setParents(commonParent);
            tmpNode.setParents(tNode);
            return;
        }

        tNode.addChild(commonParent);
        tNode.setParents(commonParent.getParents());
        commonParent.getParents().addChild(tNode, commonParent.getParents().getChildIndex(commonParent));
        commonParent.getParents().getChildrens().remove(commonParent);
        commonParent.setParents(tNode);
    }

    public void convertToSmallestCommonSubTree() {
        TreeNode commonParent = this.findLowestCommonParent(new ArrayList<Integer>(){{add(-1); add(-2);}});
//  System.out.println(commonParent.getChildrens());
        TreeNode tag1 = this.treemap.get(-1);
        TreeNode tag2 = this.treemap.get(-2);
        int flag = 0;
        for (int i=0; i<commonParent.getChildrens().size(); i++) {
            TreeNode tn = commonParent.getChildrens().get(i);
            if (tn.equals(tag1) || tn.equals(tag2) || this.isParent(tn, tag1) || this.isParent(tn, tag2)) {
                flag ++;
                continue;
            } else {
                if (flag == 0 || flag >= 2) {
                    commonParent.getChildrens().remove(tn);
                    i--;
                }
            }
        }

        TreeNode partmp = tag1.getParents();
        TreeNode chitmp = tag1;
        while (!partmp.equals(commonParent)) {
            int count = partmp.getChildrens().size();
            int i=0;
            while (i < count) {
                if (partmp.getChildrens().get(i).equals(chitmp)) {
                    break;
                }
                partmp.getChildrens().remove(i);
                count--;
            }
            chitmp = partmp;
            partmp = partmp.getParents();
        }

        partmp = tag2.getParents();
        chitmp = tag2;
        boolean mark = false;
        while (!partmp.equals(commonParent)) {
            int count = partmp.getChildrens().size();
            int i=0;
            while( i < count ) {
                if (mark == true) {
                    partmp.getChildrens().remove(i);
                    count--;
                    continue;
                }
                if (partmp.getChildrens().get(i).equals(chitmp)) {
                    mark = true;
                }
                i++;
            }
            chitmp = partmp;
            partmp = partmp.getParents();
            mark = false;
        }

        this.root = commonParent;
    }

    public TreeNode findLowestCommonParent(List<Integer> index) {
        if (index.isEmpty()) {
            System.out.println("target index empty");
        }

        TreeNode penParentNode = this.treemap.get(index.get(0));
//  System.out.println(penParentNode.getValue());
        boolean cond = true;
        do {
            cond = true;
            penParentNode = penParentNode.getParents();
            for (int i = 1; i < index.size(); i++) {
                if (!this.isParent(penParentNode, this.treemap.get(index.get(i)))) {
                    cond = false;
                    break;
                }
            }
        } while (!cond);
        return penParentNode;
    }

    public boolean isParent(TreeNode parent, TreeNode vliadNode) {
        if (parent.childrens.contains(vliadNode) || parent.equals(vliadNode)) {
            return true;
        }
        for (TreeNode tn : parent.getChildrens()) {
            if (this.isParent(tn, vliadNode)) {
                return true;
            }
        }
        return false;
    }

    // type=1, t1 - Root; type=2, t2 - root; type=3, t1-t2
    public Set<Integer> getPathWordIndexes(int type) {
        Set<Integer> re = new HashSet<Integer>();
        List<TreeNode> tns = new ArrayList<TreeNode>();
        if (type==1) {
            tns = this.findRootPath(this.treemap.get(-1));
        } else if (type==2) {
            tns = this.findRootPath(this.treemap.get(-2));
        } else {
            tns = this.findPath(this.treemap.get(-1), this.treemap.get(-2));
        }
        for (TreeNode tn : tns) {
//   System.out.println(tn.getHashkey());
            re.add(tn.getHashkey());
        }
        return re;
    }

    public static void main(String[] argv) {
        String[] splitted = new String[2];
        splitted[0] = "";
        splitted[1] = "";
        splitted = customSplit("NP-1","-");
        System.out.print(splitted[0]+"\n");
        System.out.print(splitted[1]+"\n");
    }

}

