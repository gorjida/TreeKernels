package utils;

/**
 * Created by u6042446 on 2016-12-05.
 */

import conf.Configuration;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.ArrayMap;
import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.data.representation.tree.node.TreeNode;
import it.uniroma2.sag.kelp.data.representation.structure.StructureElementFactory;

import java.util.*;
import edu.stanford.nlp.process.DocumentPreprocessor;
import java.io.StringReader;



//loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz","-maxLength","80","-retainTmpSubcategories"
//);
public class TreeKernelNew {
    public static Configuration confObject = Configuration.getInstance();

    /**
     *
     * @param leftParentNode leftParentNode in the subsetTree
     * @param rightParentNode rightParentNode in the subsetTree
     * @param subsetMap Map of all the subsets
     * @param minDepth Depth should be at least equal to this value
     * @param maxDepth Depth shouldn't violate the maxDepth value
     * @param minWidth Width should be at least equal to this value
     * @param maxWidth Width shoudn't violate the maxWidth
     * @param nodeSimilarity The similarity score for all the pairs
     * @return
     */
    public static List<SubsetTreeNode> subsetTreeKernel(utils.TreeNode leftParentNode, utils.TreeNode rightParentNode, Map<String,List<SubsetTreeNode>> subsetMap,
                                                         int minDepth, int maxDepth, int minWidth, int maxWidth, Map<String,Double> nodeSimilarity,double scorePruneThreshold) {

        //Calculate node-similarity between the two parent nodes
        double sim = nodeSimilarity.get(leftParentNode.getHashkey()+"|"+rightParentNode.getHashkey());//This is the similarity score between original nodes
        if (sim<scorePruneThreshold)
        {
            return (new ArrayList<SubsetTreeNode>());
        }

        //Get list of children for any of these two nodes
        List<utils.TreeNode> leftNodeChildren = leftParentNode.getChildrens();
        List<utils.TreeNode> rightNodeChildren = rightParentNode.getChildrens();
        //Check if any or both of children are terminals
        if (leftNodeChildren.size()==0 || rightNodeChildren.size()==0) {
            //Return TERMINAL
            List<SubsetTreeNode> listOfSubsetTrees = new ArrayList<SubsetTreeNode>();
            listOfSubsetTrees.add(new SubsetTreeNode(leftParentNode,rightParentNode,sim));//Creat a new subSetTreeNode with the given similarity and the pair of nodes
            return (listOfSubsetTrees);//REturn this as a common subsetTree and assign similarity score of the parents as well
        }

        List<List<SubsetTreeNode>> childrenSubsets = new ArrayList<List<SubsetTreeNode>>();
        List<Integer> childrenSubsetSize = new ArrayList<Integer>();
        //Iterate over all the children of the current pair
        for (utils.TreeNode childT1: leftNodeChildren) {
            for (utils.TreeNode childT2: rightNodeChildren) {
                //Evaluate similarity between children
                //List<Integer> childrenSubsetSize = new ArrayList<Integer>();
                double childSim = nodeSimilarity.get(childT1.getHashkey()+"|"+childT2.getHashkey());
                if (childSim<scorePruneThreshold)
                {
                    continue;
                }

                //if (childT1.getWidth()==maxWidth || childT1.getDepth()==maxDepth) continue;

                List<SubsetTreeNode> childSubsets = subsetTreeKernel(childT1,childT2,subsetMap,minDepth,maxDepth,minWidth,maxWidth,nodeSimilarity,scorePruneThreshold);//All subsetTrees for these two nodes
                childSubsets.add(new SubsetTreeNode(new utils.TreeNode(),new utils.TreeNode(),(double) 1));
                childrenSubsets.add(childSubsets);//This will keep the list of subsets for any pair of children
                childrenSubsetSize.add(childSubsets.size());
            }
        }

        List<List<Integer>> permutations = new ArrayList<List<Integer>>();
        Operations.treeNodePermutations(childrenSubsetSize,permutations,new ArrayList<Integer>(),0);
        List<SubsetTreeNode> parentSubsets = new ArrayList<SubsetTreeNode>();
        //Loop over all permutations
        List<SubsetTreeNode> listOfSubsetTreeNodes = new ArrayList<SubsetTreeNode>();


        //System.out.print(childrenSubsetSize+"\n");
        for (List<Integer> indexes: permutations) {
            SubsetTreeNode rootT1 = new SubsetTreeNode(leftParentNode,rightParentNode,sim);//sim here is the base similarity for just this node
            List<SubsetTreeNode> rootChildren = new ArrayList<SubsetTreeNode>();

            int counter = 0;
            int maxDepthTemp = -1;
            int width = 0;
            Set<Integer> hashKeysLeft = new HashSet<Integer>();
            Set<Integer> hashKeysRight = new HashSet<Integer>();
            double overallRootScore = sim;
            boolean combIsValid = true;



            for (int index: indexes) {
                SubsetTreeNode subNode = childrenSubsets.get(counter).get(index);//This is a generated subNode (paired node and assigned similarity score)
                //Make sure the same node is not assigned twice; For example, (b,c) and (b,d) shouldn't be considered in the same tree

                if (hashKeysLeft.contains(subNode.getLeftParent().hashkey) || hashKeysRight.contains(subNode.getRightParent().hashkey)) {
                    combIsValid = false;
                    break;
                }


                if (subNode.getLeftParent().getValue()!=null)
                {
                    hashKeysLeft.add(subNode.getLeftParent().getHashkey());
                    hashKeysRight.add(subNode.getRightParent().getHashkey());
                    //This is a real node
                    //Set the similarity score based on the similarity of parents
                    //subNode.setSimScore(subNode.getSimScore()*sim);//Similarity of children \times the similarity of parents
                    overallRootScore*= subNode.getSimScore();
                    rootChildren.add(subNode);

                    //double weightedDepth = subNode.getSubsetNode().getDepth()*sim;//This is the effect of similarity
                    //DISCUSS: do we need to apply this kind of similarities to the DEPTH and WIDTH values?
                    if (subNode.getDepth()>=maxDepthTemp) maxDepthTemp = subNode.getDepth();
                    width+= subNode.getWidth();
                }
                counter+=1;
            }

            if (combIsValid)
            {
                rootT1.setListOfChildren(rootChildren);
                rootT1.setSimScore(overallRootScore);//The score for each subSetTree is equal to the product of all nodes' scores
                if (maxDepthTemp>-1) rootT1.setDepth(maxDepthTemp+1);
                rootT1.setWidth(Math.max(width,1));

                /**
                System.out.print(rootT1.getDepth()+","+rootT1.getWidth()+"\n");
                System.out.print(rootT1.getLeftParent().getValue()+","+rootT1.getRightParent().getValue()+","+rootT1.getSimScore()+"\n");
                System.out.print("      ");
                for (SubsetTreeNode n: rootChildren) {
                    System.out.print(n.getLeftParent().getValue()+","+n.getRightParent().getValue()+"****\n");
                    if (!n.getListOfChildren().isEmpty())
                    {
                        System.out.print("               ");
                        for (SubsetTreeNode n2: n.getListOfChildren()) {
                            System.out.print(n2.getLeftParent().getValue()+","+n2.getRightParent().getValue()+"****\n");
                        }
                    }
                    System.out.print("      ");
                }
                System.out.print("\n\n");
                **/

                if (rootT1.getListOfChildren().size()>0 && rootT1.getDepth()>=minDepth
                        && rootT1.getWidth()>=minWidth && rootT1.getDepth()<=maxDepth && rootT1.getWidth()<=maxWidth) {
                    listOfSubsetTreeNodes.add(rootT1);
                }
                parentSubsets.add(rootT1);
            }


        }


        String key = leftParentNode.hashkey+"|"+rightParentNode.hashkey;
        subsetMap.put(key,listOfSubsetTreeNodes);
        return (parentSubsets);
    }

    /**
     *
     * @param parentNdoe
     * @param allSubsets
     * @return
     */
    public static List<utils.TreeNode> SubsetTree(utils.TreeNode parentNdoe,List<utils.TreeNode> allSubsets
            ,int minDepth,int maxDepth,int minWidth,int maxWidth) {

        //Check for the node's children
        List<utils.TreeNode> children = parentNdoe.getChildrens();
        if (children.size()>0) {
            //Process left and right
            List<List<utils.TreeNode>> childrenSubsets = new ArrayList<List<utils.TreeNode>>();
            List<Integer> childrenSubsetSize = new ArrayList<Integer>();

            for (utils.TreeNode child: children) {
                if (child.getWidth()==maxWidth || child.getDepth()==maxDepth) continue;
                List<utils.TreeNode> childSubsets = SubsetTree(child,allSubsets,minDepth,maxDepth,minWidth,maxWidth);
                //add dummy
                childSubsets.add(new utils.TreeNode());
                childrenSubsets.add(childSubsets);
                childrenSubsetSize.add(childSubsets.size());
            }

            List<utils.TreeNode> parentSubsets = new ArrayList<utils.TreeNode>();
            //Find all permutations

            List<List<Integer>> permutations = new ArrayList<List<Integer>>();
            Operations.treeNodePermutations(childrenSubsetSize,permutations,new ArrayList<Integer>(),0);
            //Loop over all permutations
            for (List<Integer> indexes: permutations) {
                utils.TreeNode root = new utils.TreeNode(parentNdoe);
                List<utils.TreeNode> rootChildren = new ArrayList<utils.TreeNode>();
                int counter = 0;
                int maxDepthTemp = -1;
                int width = 0;
                for (int index: indexes) {
                    utils.TreeNode subNode = childrenSubsets.get(counter).get(index);
                    if (subNode.value!=null)
                    {
                        rootChildren.add(subNode);
                        if (subNode.getDepth()>=maxDepthTemp) maxDepthTemp = subNode.getDepth();
                        width+= subNode.getWidth();
                    }

                    counter+=1;
                }
                root.setChildrens(rootChildren);
                if (maxDepthTemp>-1) root.setDepth(maxDepthTemp+1);
                root.setWidth(Math.max(width,1));

                if (root.childrens.size()>0 && root.getDepth()>=minDepth
                        && root.getWidth()>=minWidth && root.getDepth()<=maxDepth && root.getWidth()<=maxWidth) {
                    allSubsets.add(root);
                }
                parentSubsets.add(root);
            }
            return(parentSubsets);

            //Generate subsetTrees based on the generated left/right subsets
        } else {
            utils.TreeNode terminal = new utils.TreeNode(parentNdoe);
            List<utils.TreeNode> terminalList = new ArrayList<utils.TreeNode>();
            terminalList.add(terminal);
            return (terminalList);
        }

    }


    /**
     * TASK calculates kernel for the given two trees
     * @param CPP
     * @return
     */
    public static float calcKernel(Map<String,Float> CPP) {
        float count = 0;
        for (float x: CPP.values()) count+=x;
        return (count);
    }


    /**
     *
     * @param text1 First sentence raw-text
     * @param text2 Second sentence raw-text
     * @param minDepth Minimum depth of the subsetTree
     * @param maxDepth Maximum depth of the subsetTree
     * @param minWidth Minimum width of the subsetTree
     * @param maxWidth Maximum width of the subsetTree
     * @param type Type of  the parser
     * @param vectorizationType Type of vectorization
     * @param scorePruneThreshold Threshold onf the similarity score between each two nodes
     * @return
     * @throws Exception
     */
    public static double calculateSubsetKernelSimilarity(String text1,String text2,int minDepth,
                                                         int maxDepth,int minWidth,int maxWidth,Enums.DependencyType type,Enums.VectorizationType vectorizationType, double scorePruneThreshold) throws Exception{


        List<String> dependencyListTree1 = new ArrayList<String>();
        List<String> dependencyListTree2 = new ArrayList<String>();

        if (type==Enums.DependencyType.CONSTITUENCY) {
            dependencyListTree1 = ParseTree.extractConstituencyTree(text1);
            dependencyListTree2 = ParseTree.extractConstituencyTree(text2);
        } else if (type == Enums.DependencyType.StandardStanford || type == Enums.DependencyType.UDV1) {

            dependencyListTree1 = ParseTree.extractDependencyTree(text1,type,false,"",vectorizationType);
            dependencyListTree2 = ParseTree.extractDependencyTree(text2,type,false,"",vectorizationType);
        }

        long init = System.currentTimeMillis();

        //TreeBuilder Class: this takes list-based parsed-tree and creates a TreeObject (that can be used for subtree creation as well)
        TreeBuilder depTree1 = new TreeBuilder();
        TreeBuilder depTree2 = new TreeBuilder();
        boolean status1 = depTree1.initTreeStringInput(dependencyListTree1);
        boolean status2 = depTree2.initTreeStringInput(dependencyListTree2);
        if (!status1 || !status2) return (-1);
        //Map of nodes within the tree
        Map<Integer, utils.TreeNode> tree1Nodes = depTree1.treemap;
        Map<Integer, utils.TreeNode> tree2Nodes = depTree2.treemap;
        //Create Inverse tree-node
        Map<utils.TreeNode, Integer> invTree1Nodes = new HashMap<utils.TreeNode, Integer>();
        Map<utils.TreeNode, Integer> invTree2Nodes = new HashMap<utils.TreeNode, Integer>();
        for (Map.Entry<Integer, utils.TreeNode> entry: tree1Nodes.entrySet()) {
            invTree1Nodes.put(entry.getValue(),entry.getKey());
        }
        for (Map.Entry<Integer, utils.TreeNode> entry: tree2Nodes.entrySet()) {
            invTree2Nodes.put(entry.getValue(),entry.getKey());
        }

        //Calculate similarity-score between each two nodes in any of two parsed-trees
        Map<String,Double> nodeSimilarity = new HashMap<String, Double>();
        for (Map.Entry<Integer,utils.TreeNode> entry1: tree1Nodes.entrySet()) {
            for (Map.Entry<Integer,utils.TreeNode> entry2: tree2Nodes.entrySet()) {
                //Calculate commmonDownwardPath for this combination
                if ((entry1.getKey()<0) || (entry2.getKey()<0)) continue;//For now, we skip ROOT
                Vector<Integer> vector1 = entry1.getValue().getVector();
                Vector<Integer> vector2 = entry2.getValue().getVector();
                //Now calculate similarity
                nodeSimilarity.put(entry1.getKey()+"|"+entry2.getKey(),Operations.calculateNodeSimilarity(entry1.getValue(),entry2.getValue(),vectorizationType));
            }
        }

        //Loop over all the intra-nodes
        Map<String,List<SubsetTreeNode>> mapOfIntraSubsets = new HashMap<String, List<SubsetTreeNode>>();
        for (Map.Entry<Integer,utils.TreeNode> entry1: tree1Nodes.entrySet()) {
            for (Map.Entry<Integer,utils.TreeNode> entry2: tree2Nodes.entrySet()) {
                //Calculate commmonDownwardPath for this combination
                if ((entry1.getKey()<0) || (entry2.getKey()<0)) continue;//For now, we skip ROOT
                if (!mapOfIntraSubsets.containsKey(entry1.getKey()+"|"+entry2.getKey())) {
                    //This recursive function takes nodes and calculates CPP and CDP
                    subsetTreeKernel(entry1.getValue(),entry2.getValue(),mapOfIntraSubsets,minDepth,maxDepth,minWidth,maxDepth,nodeSimilarity,scorePruneThreshold);
                }
            }
        }
        //System.out.print(mapOfIntraSubsets);
        //System.exit(0);

        double totalNumSubsets= 0 ;
        for (Map.Entry<String,List<SubsetTreeNode>> entry: mapOfIntraSubsets.entrySet()) {

            for (SubsetTreeNode subsetTree: entry.getValue()) {
                //System.out.print(subsetTree.getLeftParent().getValue()+","+subsetTree.getRightParent().getValue()+","+subsetTree.getSimScore()+"\n");
                totalNumSubsets+= subsetTree.getSimScore();
            }

        }
        //System.out.print(totalNumSubsets);
        return (totalNumSubsets);

    }


    public static void main(String[] argv) throws Exception {


        Enums.DependencyType type = Enums.DependencyType.CONSTITUENCY;
        String sampleText = "I ate food at restaurant";
        //sampleText = "Bills on ports and immigration";
        sampleText = "Bills on ports and immigration were submitted by Senator Brownback, Republican of Kansas";
        //System.out.print(ParseTree.extractDependencyTree(sampleText,Enums.DependencyType.StandardStanford,false,"",Enums.VectorizationType.StandardStanford));
        //System.exit(1);
        String sampleText2 = "I ate food at restaurant";
        sampleText2= "Bills on ports and immigration were submitted by Senator Cruz, Republican of Texas";


        double self1 = calculateSubsetKernelSimilarity(sampleText,sampleText,-1,100,-1,100,type,Enums.VectorizationType.WordIdentity,.5);
        double self2 = calculateSubsetKernelSimilarity(sampleText2,sampleText2,-1,100,-1,100,type,Enums.VectorizationType.WordIdentity,.5);
        double intra = calculateSubsetKernelSimilarity(sampleText,sampleText2,-1,100,-1,100,type,Enums.VectorizationType.WordIdentity,.5);
        System.out.print(intra/(Math.sqrt(self1*self2)));
    }
}