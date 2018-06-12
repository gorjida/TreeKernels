package utils;

/**
 * Created by u6042446 on 2016-12-05.
 */

import conf.Configuration;

import java.util.*;


//loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz","-maxLength","80","-retainTmpSubcategories"
//);
public class TreeKernelOptimized {
    public static Configuration confObject = Configuration.getInstance();


    /**
     *
     * @param nodeSimilarity Hashmap of nodesimilarity for any pair of nodes in left and right trees
     * @param leftTreeNodes List of all the children in the left-tree
     * @return List of generated hypotheses
     */
    public static List<Hypothesis> generateHypotheses(Map<Integer,Map<Integer,Double>> nodeSimilarity,List<Integer> leftTreeNodes,Set<Integer> rightTreeNodes)
    {

        //Generate all potential assignments
        List<List<Integer>> assignedNodes = new ArrayList<List<Integer>>();
        List<Double> negScore = new ArrayList<Double>();
        for (int id: leftTreeNodes)
        {
            List<Integer> rightNodes = new ArrayList<Integer>();
            for (int potentialId: nodeSimilarity.get(id).keySet()) {
                if (rightTreeNodes.contains(potentialId) || potentialId==-1) rightNodes.add(potentialId);
            }
            assignedNodes.add(rightNodes);
            //Calculate negative-sample score
            double maxVal = Double.NEGATIVE_INFINITY;
            for (int i: rightNodes)
            {
                if (i==-1) continue;
                if (nodeSimilarity.get(id).get(i)>maxVal) maxVal=nodeSimilarity.get(id).get(i);
            }
            if (maxVal>0)
            {
                negScore.add(1-maxVal);
            } else
            {
                negScore.add((double) 1);
            }
        }

        //System.out.print(assignedNodes);
        //System.exit(1);
        //Find all the permutations
        //utils.Operations.distinctTreeNodePermutations();
        List<Hypothesis> listOfHypotheses = new ArrayList<Hypothesis>();
        List<List<Integer>> permutations = new ArrayList<List<Integer>>();
        //This will generate all possible hypotheses where each hypothesis denotes a set of node-to-node assignments
        utils.Operations.distinctTreeNodePermutations(assignedNodes,permutations,new ArrayList<Integer>(),0);
        //System.out.print(permutations);
        //System.exit(1);
        //Iterate over each hypothesis
        double totalScore = 0;
        for (List<Integer> h: permutations)
        {
            //Calculate score for this hypothesis
            double score = 1;
            int counter = 0;
            int effectiveCounter = 0;
            Hypothesis hypothesis = new Hypothesis();
            for (int id: h)
            {
                if (id==-1)
                {

                    score*= negScore.get(counter);
                    counter+=1;
                    continue;
                }
                //New pair:
                hypothesis.addLeftNode(leftTreeNodes.get(counter));
                hypothesis.addRightNode(id);
                score*= nodeSimilarity.get(leftTreeNodes.get(counter)).get(id);
                counter+=1;
                effectiveCounter+=1;
            }
            if (hypothesis.getRightNodes().isEmpty()) continue;
            totalScore+= score;
            hypothesis.setScore(score);
            listOfHypotheses.add(hypothesis);
        }

        //Normalize the score for each hypothesis
        for (Hypothesis h: listOfHypotheses) h.setScore(h.getScore()/totalScore);
        //for (Hypothesis h: listOfHypotheses) System.out.print(h+"\n");
        return (listOfHypotheses);
    }

    /**
     *
     * @param leftParentNode
     * @param rightParentNode
     * @param nodeSimilarity
     * @param leftTreeMap
     * @param rightTreeMap
     * @param scorePruneThreshold
     * @param nodeStatWeight
     * @param decayFactor
     * @return
     */



    public static double subsetTreeKernel(utils.TreeNode leftParentNode, utils.TreeNode rightParentNode, Map<Integer,Map<Integer,Double>> nodeSimilarity,
                                          Map<Integer, utils.TreeNode> leftTreeMap,Map<Integer, utils.TreeNode> rightTreeMap,double scorePruneThreshold,double nodeStatWeight,double decayFactor) {

        //Calculate node-similarity between the two parent nodes
        double sim = 0;
        if (nodeSimilarity.get(leftParentNode.getHashkey()).containsKey(rightParentNode.getHashkey())) sim = nodeSimilarity.get(leftParentNode.getHashkey()).get(rightParentNode.getHashkey());
        //System.out.print(leftParentNode.getHashkey()+","+rightParentNode.getHashkey()+"\n");

        if (sim<scorePruneThreshold)
        {
            return (0); //If similarity is below the threshold, simply return "0"
        }
        //Gather all the children
        List<utils.TreeNode> leftNodeChildren = leftParentNode.getChildrens();
        List<utils.TreeNode> rightNodeChildren = rightParentNode.getChildrens();
        //Check if any or both of children are terminals
        if (leftNodeChildren.size()==0 || rightNodeChildren.size()==0) {
            return (sim);//The common similarity between the terminals
        }

        //Get set of all the hypotheses
        List<Hypothesis> listOfHypotheses = generateHypotheses(nodeSimilarity,leftParentNode.getChildrenHashkeys(),
                new HashSet<Integer>(rightParentNode.getChildrenHashkeys()));
        //System.out.print(listOfHypotheses.size()+"\n");
        //Loop over each hypothesis
        double totalNumSubtrees=0;
        for (Hypothesis h: listOfHypotheses)
        {
            List<Integer> leftNodes = h.getLeftNodes();
            List<Integer> rightNodes = h.getRightNodes();
            double thisHypothesisSubsetTree = 1;
            for (int i=0;i<leftNodes.size();i++)
            {
                utils.TreeNode leftTreeNode = leftTreeMap.get(leftNodes.get(i));
                utils.TreeNode rightTreeNode = rightTreeMap.get(rightNodes.get(i));
                double hSim = subsetTreeKernel(leftTreeNode,rightTreeNode,nodeSimilarity,leftTreeMap,rightTreeMap,scorePruneThreshold,nodeStatWeight,decayFactor);
                thisHypothesisSubsetTree*= (1+hSim);
            }
            totalNumSubtrees+= h.getScore()*thisHypothesisSubsetTree*decayFactor;
        }
        return (sim*totalNumSubtrees);
    }


    /**
     *
     * @param parentNdoe
     * @param allSubsets
     * @param minDepth
     * @param maxDepth
     * @param minWidth
     * @param maxWidth
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





    public static SubsetTreeStats calculateSubsetKernelSimilarity(String text1,String text2,Enums.DependencyType type,
                                                                  Enums.VectorizationType vectorizationType,Enums.TreeStat treeStat, double scorePruneThreshold,double nodeStatWeight,double decayFactor) throws Exception{


        List<TreeBuilder> dependencyListTree1= new ArrayList<TreeBuilder>();
        List<TreeBuilder> dependencyListTree2 = new ArrayList<TreeBuilder>();

        if (type==Enums.DependencyType.CONSTITUENCY) {
            //System.out.print("Enforcing Vectorization for Constituency Tree (we only support identity similarity)\n");
            vectorizationType = Enums.VectorizationType.WordIdentity;
            dependencyListTree1 = ParseTree.extractConstituencyTree(text1);
            dependencyListTree2 = ParseTree.extractConstituencyTree(text2);
            //if ((dependencyListTree1.get(0).treemap.size()>50) || (dependencyListTree2.get(0).treemap.size()>50))
            //{
            //    return (-1);
            //}
        } else if (type == Enums.DependencyType.StandardStanford || type == Enums.DependencyType.UDV1) {

            dependencyListTree1 = ParseTree.extractDependencyTree(text1,type,false,"",vectorizationType);
            dependencyListTree2 = ParseTree.extractDependencyTree(text2,type,false,"",vectorizationType);
        }

        long init = System.currentTimeMillis();

        //For now, there is only one sentence, so get the first entry (fix this later)

        TreeBuilder depTree1 = dependencyListTree1.get(0);
        TreeBuilder depTree2 = dependencyListTree2.get(0);
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
        Map<Integer,Map<Integer,Double>> nodeSimilarity = new HashMap<Integer, Map<Integer, Double>>();
        Map<Integer,Double> temp = new HashMap<Integer, Double>();
        Map<Integer,Double> maxSaver = new HashMap<Integer, Double>();


        //Map<Integer,Map<Integer,>> assignedNodes
        for (Map.Entry<Integer,utils.TreeNode> entry1: tree1Nodes.entrySet()) {
            for (Map.Entry<Integer,utils.TreeNode> entry2: tree2Nodes.entrySet()) {
                //Calculate commmonDownwardPath for this combination
                if ((entry1.getKey()<0) || (entry2.getKey()<0))  continue;//For now, we skip ROOT
                Vector<Double> vector1 = entry1.getValue().getVector();
                Vector<Double> vector2 = entry2.getValue().getVector();
                //Now calculate similarity
                double sim = Operations.calculateNodeSimilarity(entry1.getValue(),entry2.getValue(),vectorizationType);
                //if ((entry1.getKey()==1 && entry2.getKey()==2)) sim = .5;
                if (!nodeSimilarity.containsKey(entry1.getKey())) nodeSimilarity.put(entry1.getKey(),new HashMap<Integer, Double>());
                if (!maxSaver.containsKey(entry1.getKey())) maxSaver.put(entry1.getKey(),Double.NEGATIVE_INFINITY);
                if (sim>maxSaver.get(entry1.getKey())) maxSaver.put(entry1.getKey(),sim);
                Map<Integer,Double> soFarSimilarities = nodeSimilarity.get(entry1.getKey());
                if (sim>0) soFarSimilarities.put(entry2.getKey(),sim);
            }
        }

        //System.out.print(nodeSimilarity+"\n");
        //for (Map.Entry<Integer,Double> entry: maxSaver.entrySet())
        //{
           // Map<Integer,Double> soFarSimilarities = nodeSimilarity.get(entry.getKey());
           // soFarSimilarities.put(-1,1-entry.getValue());
        //}
        //System.out.print(nodeSimilarity+"\n");
        //System.out.print(nodeSimilarity+"\n");
        //List<Integer> temp2 = new ArrayList<Integer>();
        //temp2.add(6);temp2.add(3);
        //HashSet<Integer> temp3 = new HashSet<Integer>();
        //temp3.add(6);temp3.add(3);
        //generateHypotheses(nodeSimilarity,temp2,temp3);


        //Loop over all the intra-nodes
        Map<String,Double> mapOfIntraSubsets = new HashMap<String, Double>();
        for (Map.Entry<Integer,utils.TreeNode> entry1: tree1Nodes.entrySet()) {
            for (Map.Entry<Integer,utils.TreeNode> entry2: tree2Nodes.entrySet()) {
                //Calculate commmonDownwardPath for this combination
                if ((entry1.getKey()<0) || (entry2.getKey()<0)) continue;//For now, we skip ROOT
                if (!mapOfIntraSubsets.containsKey(entry1.getKey()+"|"+entry2.getKey())) {
                    //System.out.print(entry1.getKey()+","+entry2.getKey()+"\n");

                        double value = subsetTreeKernel(entry1.getValue(),entry2.getValue(),nodeSimilarity,tree1Nodes,tree2Nodes,scorePruneThreshold,nodeStatWeight,decayFactor);
                        //System.exit(1);
                        mapOfIntraSubsets.put(entry1.getKey()+"|"+entry2.getKey(),Math.max(0,value-1));

                    //double value = subsetTreeKernel(entry2.getValue(),entry2.getValue(),nodeSimilarity,tree1Nodes,tree2Nodes,scorePruneThreshold,nodeStatWeight,decayFactor);

                }
            }
        }

        //System.out.print(mapOfIntraSubsets);


        double totalNumSubsets= 0 ;

        for (Map.Entry<String,Double> entry: mapOfIntraSubsets.entrySet()) {

            totalNumSubsets+= entry.getValue();

        }
        //System.out.print("\n"+totalNumSubsets);

        //System.out.print(totalNumSubsets);
        //return (new SubsetTreeStats(listOfDepths,listOfWidths,totalNumSubsets,nodeScores));

        SubsetTreeStats stats = new SubsetTreeStats(depTree1.getTreeDepth(),depTree1.getTreeWidth(),depTree2.getTreeDepth(),depTree2.getTreeWidth(),totalNumSubsets);
        return (stats);
    }


    public static void main(String[] argv) throws Exception {

        Enums.DependencyType type = Enums.DependencyType.UDV1;

        String sampleText = "It takes another hour to search for the Book of the Dead 's opposite number , which will theoretically send Imhotep back to the cosmic soup from which he sprang before he can transfer the heroine 's soul to the embalmed remains of his lady love .";

        //sampleText = "I ate food";
        //sampleText = "the literature has vastly discussed different control techniques";
        //System.out.print(ParseTree.extractDependencyTree(sampleText,Enums.DependencyType.StandardStanford,false,"",Enums.VectorizationType.StandardStanford));
        //System.exit(1);
        String sampleText2 = "";
        //sampleText2= "the literature has vastly discussed  multiple trajectory planning methodologies";

        SubsetTreeStats stats= calculateSubsetKernelSimilarity(sampleText,sampleText, type, Enums.VectorizationType.WordIdentity, Enums.TreeStat.PRODUCT, 1,.9,1);

        //System.out.print(stats.getLeftTreeDepth()+"\n"+stats.getLeftTreeWidth()+"\n"+stats.getTotalNumSubsets()+"\n");
    }
}