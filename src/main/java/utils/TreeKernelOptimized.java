package utils;

/**
 * Created by u6042446 on 2016-12-05.
 */

import conf.Configuration;

import java.util.*;


//loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz","-maxLength","80","-retainTmpSubcategories"
//);
public class TreeKernelOptimized {
    public static final int MAXHYP = (int) Math.pow(3,10);
    public static Configuration confObject = Configuration.getInstance();
    private ParseTree parseTree = new ParseTree();


    /**
     *
     * @param nodeSimilarity Hashmap of nodesimilarity for any pair of nodes in left and right trees
     * @param leftTreeNodes List of all the children in the left-tree
     * @return List of generated hypotheses
     */
    public List<Hypothesis> generateHypotheses(Map<Integer,Map<Integer,Double>> nodeSimilarity,List<Integer> leftTreeNodes,Set<Integer> rightTreeNodes)
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
            if (rightNodes.isEmpty()) rightNodes.add(-1);//There is no assignment to this node
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


        int totalNumHypotheses = 1;
        for (List<Integer> list: assignedNodes) {
            if (list.size()==0) continue;
            totalNumHypotheses*= list.size();
            if (totalNumHypotheses>MAXHYP) break;
        }
        //if (totalNumHypotheses>MAXHYP) {
        //    return (new ArrayList<Hypothesis>());
        //}
        if (totalNumHypotheses>MAXHYP)
        {
            return (null);
        }

        //System.out.print(assignedNodes+">>>>\n");
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
        //Each entry corresponds to an assignment
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
                    //We don't have any concept of UNDEDTECTED; Therefore, no-assignment doesn't contribute to the overal score
                    //score*= negScore.get(counter);
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



    public double subsetTreeKernel(utils.TreeNode leftParentNode, utils.TreeNode rightParentNode, Map<Integer,Map<Integer,Double>> nodeSimilarity,
                                          Map<Integer, utils.TreeNode> leftTreeMap,Map<Integer, utils.TreeNode> rightTreeMap,double scorePruneThreshold
            ,double nodeStatWeight,double decayFactor,double widthFactor) {

        //Calculate node-similarity between the two parent nodes
        double sim = 0;
        if (nodeSimilarity.get(leftParentNode.getHashkey()).containsKey(rightParentNode.getHashkey())) sim = nodeSimilarity.get(leftParentNode.getHashkey()).get(rightParentNode.getHashkey());

        //if (sim<scorePruneThreshold)
        //{
        //    return (0); //If similarity is below the threshold, simply return "0"
        //}

        //if (leftParentNode.getHashkey()==3 && rightParentNode.getHashkey()==3) {
        //    System.out.print(sim+ ">>>>>\n");
        //}
        //System.out.print(leftParentNode.getHashkey()+","+rightParentNode.getHashkey()+ "," + sim+"\n");
        //Gather all the children
        List<utils.TreeNode> leftNodeChildren = leftParentNode.getChildrens();
        List<utils.TreeNode> rightNodeChildren = rightParentNode.getChildrens();
        //Check if any or both of children are terminals
        if (leftNodeChildren.size()==0 || rightNodeChildren.size()==0) {
            return (sim);//The common similarity between the terminals
        }

        //Get set of all the hypotheses

        //System.out.print(leftParentNode.getChildrenHashkeys().size()+"\n");

        List<Hypothesis> listOfHypotheses = generateHypotheses(nodeSimilarity,leftParentNode.getChildrenHashkeys(),
                new HashSet<Integer>(rightParentNode.getChildrenHashkeys()));

        //if (leftParentNode.getHashkey()==2 && rightParentNode.getHashkey()==2)
        //{
         //   System.out.print(leftParentNode.getChildrenHashkeys()+","+ rightParentNode.getChildrenHashkeys()+"\n");
        //}


        if (listOfHypotheses==null)
        {
            //System.out.print("HERE\n");
            return (-1);
        } else  if (listOfHypotheses.size()==0)
        {
            return (sim);

        } else
        {
            //Loop over each hypothesis
            double totalNumSubtrees=0;
            //Loop over all the hypotheses
            for (Hypothesis h: listOfHypotheses)
            {
                List<Integer> leftNodes = h.getLeftNodes();
                List<Integer> rightNodes = h.getRightNodes();
                double thisHypothesisSubsetTree = 1;
                for (int i=0;i<leftNodes.size();i++)
                {
                    utils.TreeNode leftTreeNode = leftTreeMap.get(leftNodes.get(i));
                    utils.TreeNode rightTreeNode = rightTreeMap.get(rightNodes.get(i));
                    double hSim = subsetTreeKernel(leftTreeNode,rightTreeNode,nodeSimilarity,leftTreeMap,rightTreeMap,scorePruneThreshold,nodeStatWeight,decayFactor,widthFactor);
                    if (hSim==-1) {
                        thisHypothesisSubsetTree = -1;
                        break;
                    }
                    thisHypothesisSubsetTree*= (1+widthFactor*hSim);
                }
                if (thisHypothesisSubsetTree==-1)
                {
                    totalNumSubtrees = -1;
                    break;
                }
                totalNumSubtrees+= h.getScore()*thisHypothesisSubsetTree*decayFactor;
                //if (leftParentNode.getHashkey()==2 && rightParentNode.getHashkey()==2)
                //{
                //    System.out.print(totalNumSubtrees+"\n");
                //}
            }
            return (sim*totalNumSubtrees);
        }


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
    public List<utils.TreeNode> SubsetTree(utils.TreeNode parentNdoe,List<utils.TreeNode> allSubsets
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
     *
     * @param text1 sentence1
     * @param text2 sentence2
     * @param type type of the parse-tree
     * @param vectorizationType type of vector representation for each node in the tree
     * @param treeStat an object that keeps tree statistics
     * @param scorePruneThreshold threshold for similarity between each two nodes
     * @param nodeStatWeight
     * @param decayFactor decaying fator of the depth of the tree
     * @param widthFactor
     * @param isSelf
     * @throws Exception
     */
    public BaseLineStats calculateBaseLineSimilarity(String text1,String text2,Enums.DependencyType type,
                                            Enums.VectorizationType vectorizationType,Enums.TreeStat treeStat,
                                            double scorePruneThreshold,double nodeStatWeight,double decayFactor,double widthFactor,boolean isSelf) throws Exception
    {
        List<TreeBuilder> dependencyListTree1= new ArrayList<TreeBuilder>();
        List<TreeBuilder> dependencyListTree2 = new ArrayList<TreeBuilder>();

        long currentTime = System.currentTimeMillis();
        if (type==Enums.DependencyType.CONSTITUENCY) {
            //System.out.print("Enforcing Vectorization for Constituency Tree (we only support identity similarity)\n");
            vectorizationType = Enums.VectorizationType.WordIdentity;
            dependencyListTree1 = parseTree.extractConstituencyTree(text1);
            if (!isSelf)
            {
                dependencyListTree2 = parseTree.extractConstituencyTree(text2);
            } else {
                dependencyListTree2 = new ArrayList<TreeBuilder>(dependencyListTree1);
            }

        } else if (type == Enums.DependencyType.StandardStanford || type == Enums.DependencyType.UDV1) {

            dependencyListTree1 = parseTree.extractDependencyTree(text1,type,false,"",vectorizationType);
            //System.out.print(text1+"\n");
            //System.out.print(dependencyListTree1.get(0).treemap.get(2).getChildrens().get(1).getValue()+"****\n");
            if (!isSelf)
            {
                dependencyListTree2 = parseTree.extractDependencyTree(text2,type,false,"",vectorizationType);
            } else
            {
                dependencyListTree2 = new ArrayList<TreeBuilder>(dependencyListTree1);
            }

        }
        long finishTime = System.currentTimeMillis();
        //System.out.print("Parsing time:"+(finishTime-currentTime)/1000+"\n");
        long init = System.currentTimeMillis();

        //For now, there is only one sentence, so get the first entry (fix this later)
        currentTime = System.currentTimeMillis();
        TreeBuilder depTree1 = dependencyListTree1.get(0);
        TreeBuilder depTree2= dependencyListTree2.get(0);
        //Map of nodes within the tree
        Map<Integer, utils.TreeNode> tree1Nodes = depTree1.treemap;
        Map<Integer, utils.TreeNode> tree2Nodes = depTree2.treemap;
        //Calculate vector representation using any of the representations
        Vector<Double> vecTree1 = new Vector<Double>();
        Vector<Double> vecTree2 = new Vector<Double>();

        List<String> postagTree1 = new ArrayList<String>();
        List<String> relationsTree1 = new ArrayList<String>();
        List<String> wordsTree1 = new ArrayList<String>();

        for (Map.Entry<Integer,utils.TreeNode> entry: tree1Nodes.entrySet())
        {
            if (entry.getKey()>0)
            {
                String token = entry.getValue().getValue();
                postagTree1.add(token+"_"+entry.getValue().getTag());
                wordsTree1.add(token);
                for (String relation: entry.getValue().getGrammaticalRelations()) relationsTree1.add(token+"_"+relation);
                Vector<Double> nodeRepres = entry.getValue().getVector();
                //System.out.print(nodeRepres);
                vecTree1 = Operations.addVectors(nodeRepres,vecTree1);
            }
        }

        List<String> postagTree2 = new ArrayList<String>();
        List<String> relationsTree2= new ArrayList<String>();
        List<String> wordsTree2 = new ArrayList<String>();
        for (Map.Entry<Integer,utils.TreeNode> entry: tree2Nodes.entrySet())
        {
            if (entry.getKey()>0)
            {
                String token = entry.getValue().getValue();
                postagTree2.add(token+"_"+entry.getValue().getTag());
                wordsTree2.add(token);
                for (String relation: entry.getValue().getGrammaticalRelations()) relationsTree2.add(token+"_"+relation);
                Vector<Double> nodeRepres = entry.getValue().getVector();
                vecTree2 = Operations.addVectors(nodeRepres,vecTree2);
            }
        }

       //Calculate cosine similarity based on the one-hot vector representation
        double sim = Operations.calculateCosineSimilarity(vecTree1,vecTree2);
        //Calculate Jaccard similarity based on the extracted set of labels
        float simWordSim = Operations.jaccard_similarity(new HashSet<String>(wordsTree1),new HashSet<String>(wordsTree2));
        float simPostagSim = Operations.jaccard_similarity(new HashSet<String>(postagTree1),new HashSet<String>(postagTree2));
        float simRelationSim = Operations.jaccard_similarity(new HashSet<String>(relationsTree1),new HashSet<String>(relationsTree2));
        //Form baseline object
        BaseLineStats baseLineStats = new BaseLineStats(sim,(double) simWordSim,(double) simPostagSim,(double) simRelationSim);

        return (baseLineStats);

    }

    public SubsetTreeStats calculateSubsetKernelSimilarity(String text1,String text2,Enums.DependencyType type,
                                                                  Enums.VectorizationType vectorizationType,Enums.TreeStat treeStat,
                                                           double scorePruneThreshold,double nodeStatWeight,double decayFactor,double widthFactor,boolean isSelf) throws Exception{


        List<TreeBuilder> dependencyListTree1= new ArrayList<TreeBuilder>();
        List<TreeBuilder> dependencyListTree2 = new ArrayList<TreeBuilder>();

        long currentTime = System.currentTimeMillis();
        if (type==Enums.DependencyType.CONSTITUENCY) {
            //System.out.print("Enforcing Vectorization for Constituency Tree (we only support identity similarity)\n");
            vectorizationType = Enums.VectorizationType.WordIdentity;
            dependencyListTree1 = parseTree.extractConstituencyTree(text1);


            if (!isSelf)
            {
                dependencyListTree2 = parseTree.extractConstituencyTree(text2);
            } else {
                dependencyListTree2 = new ArrayList<TreeBuilder>(dependencyListTree1);
            }

        } else if (type == Enums.DependencyType.StandardStanford || type == Enums.DependencyType.UDV1) {

            dependencyListTree1 = parseTree.extractDependencyTree(text1,type,false,"",vectorizationType);
            //System.out.print(text1+"\n");
            //System.out.print(dependencyListTree1.get(0).treemap.get(2).getChildrens().get(1).getValue()+"****\n");
            if (!isSelf)
            {
                dependencyListTree2 = parseTree.extractDependencyTree(text2,type,false,"",vectorizationType);
            } else
            {
                dependencyListTree2 = new ArrayList<TreeBuilder>(dependencyListTree1);
            }

        }
        long finishTime = System.currentTimeMillis();
        //System.out.print("Parsing time:"+(finishTime-currentTime)/1000+"\n");

        long init = System.currentTimeMillis();

        //For now, there is only one sentence, so get the first entry (fix this later)
        currentTime = System.currentTimeMillis();
        TreeBuilder depTree1 = dependencyListTree1.get(0);
        TreeBuilder depTree2= dependencyListTree2.get(0);



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


        //Initialize nodeSimilarity
        for (Map.Entry<Integer,utils.TreeNode> entry1: tree1Nodes.entrySet()) {
            nodeSimilarity.put(entry1.getKey(),new HashMap<Integer, Double>());
        }
            //Map<Integer,Map<Integer,>> assignedNodes
        for (Map.Entry<Integer,utils.TreeNode> entry1: tree1Nodes.entrySet()) {
            for (Map.Entry<Integer,utils.TreeNode> entry2: tree2Nodes.entrySet()) {
                //Calculate commmonDownwardPath for this combination
                if ((entry1.getKey()<0) || (entry2.getKey()<0))  continue;//For now, we skip ROOT
                Vector<Double> vector1 = entry1.getValue().getVector();
                Vector<Double> vector2 = entry2.getValue().getVector();
                //Now calculate similarity
                double sim = Operations.calculateNodeSimilarity(entry1.getValue(),entry2.getValue(),vectorizationType);
                if (sim<scorePruneThreshold) continue;//PRune all assignments below a threshold
                //System.out.print(sim+"\n");
                //if ((entry1.getKey()==1 && entry2.getKey()==2)) sim = .5;
                //System.out.print(entry1.getKey()+","+entry1.getValue().getValue()+"\n");
                if (!maxSaver.containsKey(entry1.getKey())) maxSaver.put(entry1.getKey(),Double.NEGATIVE_INFINITY);
                if (sim>maxSaver.get(entry1.getKey())) maxSaver.put(entry1.getKey(),sim);
                Map<Integer,Double> soFarSimilarities = nodeSimilarity.get(entry1.getKey());
                if (sim>0) soFarSimilarities.put(entry2.getKey(),sim);
            }
        }
        System.out.print(nodeSimilarity+"\n");
        //Loop over all the intra-nodes
        Map<String,Double> mapOfIntraSubsets = new HashMap<String, Double>();
        for (Map.Entry<Integer,utils.TreeNode> entry1: tree1Nodes.entrySet()) {
            for (Map.Entry<Integer,utils.TreeNode> entry2: tree2Nodes.entrySet()) {
                //Calculate commmonDownwardPath for this combination
                if ((entry1.getKey()<0) || (entry2.getKey()<0)) continue;//For now, we skip ROOT
                if (!mapOfIntraSubsets.containsKey(entry1.getKey()+"|"+entry2.getKey())) {
                    //System.out.print(entry1.getKey()+","+entry2.getKey()+"\n");

                        double value = subsetTreeKernel(entry1.getValue(),entry2.getValue(),nodeSimilarity,tree1Nodes,tree2Nodes,scorePruneThreshold,nodeStatWeight,decayFactor,widthFactor);
                        //System.out.print(value+"\n");
                        if (value<0) return (new SubsetTreeStats(dependencyListTree1.get(0).getTreeDepth(),dependencyListTree1.get(0).getTreeWidth(),dependencyListTree1.get(0).getTreeDepth(),dependencyListTree1.get(0).getTreeWidth(),-1));
                        //System.exit(1);
                        //System.out.print(entry1.getKey()+"|"+entry2.getKey()+","+value+"\n");
                        mapOfIntraSubsets.put(entry1.getKey()+"|"+entry2.getKey(),value);
                    //double value = subsetTreeKernel(entry2.getValue(),entry2.getValue(),nodeSimilarity,tree1Nodes,tree2Nodes,scorePruneThreshold,nodeStatWeight,decayFactor);

                }
            }
        }
        finishTime = System.currentTimeMillis();
        //System.out.print("Subset-tree time is:"+(finishTime-currentTime)+"\n");
        double totalNumSubsets= 0 ;


        for (Map.Entry<String,Double> entry: mapOfIntraSubsets.entrySet()) {

            totalNumSubsets+= (Math.max(0,entry.getValue()-1));

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
        TreeKernelOptimized kernel = new TreeKernelOptimized();
        SubsetTreeStats stats= kernel.calculateSubsetKernelSimilarity(sampleText,sampleText, type, Enums.VectorizationType.WordIdentity, Enums.TreeStat.PRODUCT, 1,.9,1,1,true);

        //System.out.print(stats.getLeftTreeDepth()+"\n"+stats.getLeftTreeWidth()+"\n"+stats.getTotalNumSubsets()+"\n");
    }
}