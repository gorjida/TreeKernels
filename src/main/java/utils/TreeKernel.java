package utils;

/**
 * Created by u6042446 on 2016-12-05.
 */

import conf.Configuration;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.ArrayMap;
import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.data.representation.tree.node.TreeNode;
import it.uniroma2.sag.kelp.data.representation.structure.StructureElementFactory;

import java.util.*;
import edu.stanford.nlp.ling.Sentence;


public class TreeKernel {
    public static Configuration confObject = Configuration.getInstance();
    public static LexicalizedParser lp = LexicalizedParser.
            loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz","-maxLength","80","-retainTmpSubcategories"
            );

    public static TreeNode constructTree(Map<String,List<String>> leftMap,Map<String,List<String>> rightMap,TreeNode node) throws Exception {
        String nodeContent = node.getContent().getTextFromData();
        //System.out.print(nodeContent+"\n");
        int id = node.getId();
        TreeNode child = null;

        if (leftMap.containsKey(nodeContent)) {

            id+=1;
            String leftContent = leftMap.get(nodeContent).get(0);//This is the left child
            TreeNode leftNode = new TreeNode(id,StructureElementFactory.getInstance().parseStructureElement(leftContent),node);//node is father
            child = constructTree(leftMap,rightMap,leftNode);
            node.getChildren().add(child);
            //node.getChildren().add(childIndex,leftNode);
        }

        if (rightMap.containsKey(nodeContent)) {
            id+=1;
            String rightContent = rightMap.get(nodeContent).get(0);
            TreeNode rightNode = new TreeNode(id,StructureElementFactory.getInstance().parseStructureElement(rightContent),node);
            child = constructTree(leftMap,rightMap,rightNode);
            node.getChildren().add(child);
            //node.getChildren().add(childIndex,rightNode);
        }
        return node;
    }

    /**
    public static TreeNode cleanDependencyEntry(Collection<TypedDependency> dependencyCollection) throws Exception {

        String rootString = "ROOT-0";

        Map<String,List<String>> headMapLeft = new HashMap<String, List<String>>();
        Map<String,List<String>> headMapRight = new HashMap<String, List<String>>();

        for (TypedDependency dependency: dependencyCollection) {
            String entry = dependency.toString();
            String[] splitted = entry.split("\\(");
            String dependencyType = splitted[0];

            String[] levelSplitted = splitted[1].split(",");
            String head = levelSplitted[0];

            String tail = levelSplitted[1].replace(" ","").split("\\)")[0];
            if (!headMapLeft.containsKey(head)) {
                List<String> child = new ArrayList<String>();
                child.add(tail);
                child.add(dependencyType);
                headMapLeft.put(head,child);
                continue;
            }

            List<String> child = new ArrayList<String>();
            System.out.print(head+"|"+tail+"\n");
            child.add(tail);
            child.add(dependencyType);
            headMapRight.put(head,child);
        }

        //Start from the root
        System.out.print(headMapLeft+"\n");
        System.out.print(headMapRight+"\n");
        TreeNode rootNode = new TreeNode(0,StructureElementFactory.getInstance().parseStructureElement(rootString),null);//Generate root node
        TreeNode node = constructTree(headMapLeft,headMapRight,rootNode);

        return (node);

    }
     **/


    /**
     * TASK: calculate subtreeKernel for given nodes "parentNodeT1" and "parentNodeT2" with "T1" and "T2" being given trees
     * @param parentNodeT1
     * @param parentNodeT2
     * @param minDepth
     * @param maxDepth
     * @param minWidth
     * @param maxWidth
     * @return
     */
    public static List<utils.TreeNode> subsetTreeKernel(utils.TreeNode parentNodeT1,utils.TreeNode parentNodeT2,Map<String,List<utils.TreeNode>> subsetMap,
                                        int minDepth,int maxDepth,int minWidth,int maxWidth) {

        //For now, we do nothing on this
        if (parentNodeT1.getValue().compareTo(parentNodeT2.getValue())!=0) {
            return (new ArrayList<utils.TreeNode>());
        }
        //Get list of childeren for the current two parent nodes (in either trees)
        List<utils.TreeNode> childrenT1 = parentNodeT1.getChildrens();
        List<utils.TreeNode> childrenT2 = parentNodeT2.getChildrens();

        if (childrenT1.size()==0 || childrenT2.size()==0) {
            //Return TERMINAL
            utils.TreeNode terminal = new utils.TreeNode();
            terminal.setValue( parentNodeT1.getValue());
            terminal.setDepth(0);
            terminal.setWidth(1);
            List<utils.TreeNode> terminalList = new ArrayList<utils.TreeNode>();
            terminalList.add(terminal);
            return (terminalList);
        }

        List<List<utils.TreeNode>> childrenSubsets = new ArrayList<List<utils.TreeNode>>();
        List<Integer> childrenSubsetSize = new ArrayList<Integer>();
        for (utils.TreeNode childT1: childrenT1) {
            for (utils.TreeNode childT2: childrenT2) {
                //Evaluate similarity between children
                //List<Integer> childrenSubsetSize = new ArrayList<Integer>();
                if (childT1.getValue().compareTo(childT2.getValue())==0) {
                    //Similar Children
                    if (childT1.getWidth()==maxWidth || childT1.getDepth()==maxDepth) continue;

                    List<utils.TreeNode> childSubsets = subsetTreeKernel(childT1,childT2,subsetMap,minDepth,maxDepth,minWidth,maxWidth);
                    childSubsets.add(new utils.TreeNode());
                    childrenSubsets.add(childSubsets);
                    childrenSubsetSize.add(childSubsets.size());
                }
            }
        }

        List<List<Integer>> permutations = new ArrayList<List<Integer>>();
        Operations.treeNodePermutations(childrenSubsetSize,permutations,new ArrayList<Integer>(),0);

        List<utils.TreeNode> parentSubsets = new ArrayList<utils.TreeNode>();
        //Loop over all permutations
        List<utils.TreeNode> listOfTreeNodes = new ArrayList<utils.TreeNode>();

        for (List<Integer> indexes: permutations) {
            utils.TreeNode rootT1 = new utils.TreeNode(parentNodeT1);

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



            rootT1.setChildrens(rootChildren);
            if (maxDepthTemp>-1) rootT1.setDepth(maxDepthTemp+1);
            rootT1.setWidth(Math.max(width,1));
            if (rootT1.childrens.size()>0 && rootT1.getDepth()>=minDepth
                    && rootT1.getWidth()>=minWidth && rootT1.getDepth()<=maxDepth && rootT1.getWidth()<=maxWidth) {
                listOfTreeNodes.add(rootT1);
            }
            parentSubsets.add(rootT1);
        }

        //System.out.print("*******\n");
        //for (utils.TreeNode n: parentSubsets) {
         //   System.out.print(n.getValue()+"\n");
        //}
        String key = parentNodeT1.hashkey+"|"+parentNodeT2.hashkey;
        subsetMap.put(key,listOfTreeNodes);
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

            //for (utils.TreeNode leftNode: leftSubset) {
              //  for (utils.TreeNode rightNode: rightSubset) {
                    //create new left and right nodes
                //    utils.TreeNode root = new utils.TreeNode(parentNdoe);
                  //  List<utils.TreeNode> rootChildren = new ArrayList<utils.TreeNode>();
                    //if (leftNode.value!=null) {
                        //This is not dummy
                      //  rootChildren.add(leftNode);
                   // }
                    //if (rightNode.value!=null) rootChildren.add(rightNode);
                    //root.setChildrens(rootChildren);
                    //if (root.childrens.size()>0) {
                      //  allSubsets.add(root);
                    //}

                    //parentSubsets.add(root);
                //}
            //}

            //System.out.print(parentSubsets+"\n");
            //System.exit(1);
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
     * TASK generates all permuations of children for a parent node (the size is given by permutationSize)
     * @param permutations
     * @param localPermutation
     * @param childrenList
     * @param permutationSize
     * @return
     */

    public static List<List<utils.TreeNode>> generateChildrenPermutation(List<List<utils.TreeNode>> permutations,
                                                                         List<utils.TreeNode> localPermutation,List<utils.TreeNode> childrenList,int permutationSize) {

        if (localPermutation.size()==permutationSize) {
            permutations.add(new ArrayList<utils.TreeNode>(localPermutation));
            return (permutations);
        }

        int indexConter = 0;
        for (utils.TreeNode node: childrenList) {
            localPermutation.add(node);
            permutations = generateChildrenPermutation(permutations,localPermutation,childrenList.subList(indexConter+1,childrenList.size()),permutationSize);
            localPermutation.remove(localPermutation.size()-1);//Remove the last item
            indexConter+=1;
        }
        return (permutations);
    }


    public static void commonPeakPathOptimized(String postFix,utils.TreeNode tree1Child,utils.TreeNode tree2Child
            ,Map<String,Float> CDPMap,String childKey,Map<String,Float> CPPMap,
                                               Map<utils.TreeNode,Integer> invDependency1,Map<utils.TreeNode,Integer> invDependency2) {

        List<utils.TreeNode> chil1ChildrenList = tree1Child.getChildrens();
        List<utils.TreeNode> chil2ChildrenList = tree2Child.getChildrens();
        float tempCPP = CDPMap.get(childKey);//Initial CPP
        int minChildNumber = Math.min(chil1ChildrenList.size(), chil2ChildrenList.size());//Go with minimum number
        if (minChildNumber < 2) {
            //System.out.print(parentKey + ":" + tempCPP);
            CPPMap.put(childKey, tempCPP);
        } else {
            //Consider all possible tuples of children
            List<List<utils.TreeNode>> tree1Permuations = generateChildrenPermutation
                    (new ArrayList<List<utils.TreeNode>>(),
                            new ArrayList<utils.TreeNode>(), chil1ChildrenList, 2);
            List<List<utils.TreeNode>> tree2Permuations = generateChildrenPermutation
                    (new ArrayList<List<utils.TreeNode>>(),
                            new ArrayList<utils.TreeNode>(), chil2ChildrenList, 2);

            //Loop over permutations and update CPP thereafter
            for (List<utils.TreeNode> tree1Permute : tree1Permuations) {
                for (List<utils.TreeNode> tree2Permute : tree2Permuations) {
                    //we need to compare these two permutations
                    tempCPP += calculateCPPContribution(postFix,CDPMap, tree1Permute, tree2Permute,
                            invDependency1, invDependency2);//This is the final CPP
                }
            }
            CPPMap.put(childKey, tempCPP);
        }
    }

    /**
     * TASK: Conducts the recursive part for generating CDP values
     * @param CDP
     * @param tree1Node
     * @param tree2Node
     * @param invDependency1
     * @param invDependency2
     * @return
     */
    public static float commonDownardPathRecursionOptimized(float degree,float CDP,float subsetIndex,utils.TreeNode tree1Node,
                                                            utils.TreeNode tree2Node,
                                                            Map<utils.TreeNode, Integer> invDependency1,
                                                            Map<utils.TreeNode, Integer> invDependency2
            ,Map<String,Float> CDPMap,Map<String,Float> CPPMap) {

        String postFix = "";
        if (subsetIndex>-1) postFix = "|"+subsetIndex;
        if (tree1Node.getValue().compareTo(tree2Node.getValue())!=0) {
            //CDPMap.put(invDependency1.get(tree1Node)+"|"+invDependency2.get(tree2Node),0f);//No commonpath
            //CPPMap.put(invDependency1.get(tree1Node)+"|"+invDependency2.get(tree2Node),0f);

            //ADDED BY ALI
            CDPMap.put(tree1Node.hashkey+"|"+tree2Node.hashkey+postFix,0f);//No commonpath
            CPPMap.put(tree1Node.hashkey+"|"+tree2Node.hashkey+postFix,0f);
            //END OF ADDED BY ALI


            return (CDP);//The current CDP is zero
        } else {
            //Get list of children
            CDP= confObject.dependencyTreeHyperParameter*(CDP+1);//Add CDP by 1, because this is one common node
            //temporary put this one in CDPMap
            String parentKey = invDependency1.get(tree1Node)+"|"+invDependency2.get(tree2Node);
            //ADDED BY ALI
            parentKey = tree1Node.hashkey+"|"+tree2Node.hashkey+postFix;
            //END OF ADDED BY ALI

            CDPMap.put(parentKey,CDP);
            degree+=1;
            List<utils.TreeNode> tree1Children = tree1Node.getChildrens();//List of children
            List<utils.TreeNode> tree2Children = tree2Node.getChildrens();//List of children
            //Iterate over children now
            for (utils.TreeNode tree1Child: tree1Children) {
                for (utils.TreeNode tree2Child : tree2Children) {
                    String childKey = invDependency1.get(tree1Child) + "|" + invDependency2.get(tree2Child);
                    //ADDED BY ALI
                    childKey = tree1Child.hashkey+"|"+tree2Child.hashkey+postFix;
                    //END OF ADDED BY ALI

                    float newOutput = commonDownardPathRecursionOptimized(0, 0,subsetIndex, tree1Child,
                            tree2Child, invDependency1,
                            invDependency2, CDPMap, CPPMap);//Compute CDP fpr this combination

                    //if (childKey.compareTo("3|3")==0) System.out.print(CDPMap+"\n");
                    commonPeakPathOptimized(postFix,tree1Child,tree2Child,CDPMap,childKey,CPPMap,invDependency1,invDependency2);
                    //update parent
                    float parentCDP = CDPMap.get(parentKey);
                    if (CDPMap.containsKey(childKey)) {
                        parentCDP += confObject.dependencyTreeHyperParameter
                                * (CDPMap.get(childKey));
                        CDPMap.put(parentKey, parentCDP);
                        //if (true){
                        //  System.out.print(childKey+ "\t" +parentKey+"\t"+CDPMap.get(parentKey)+ "\n****\n");
                        //}
                    }
                }
            }
        }
        return (CDP);
    }


    /**
     * TASK: Conducts the recursive part for generating CDP values
     * @param CDP
     * @param tree1Node
     * @param tree2Node
     * @param invDependency1
     * @param invDependency2
     * @return
     */
    public static float commonDownardPathRecursion(float CDP,utils.TreeNode tree1Node,
                                                   utils.TreeNode tree2Node,
                                                   Map<utils.TreeNode, Integer> invDependency1,
                                                   Map<utils.TreeNode, Integer> invDependency2) {

        if (tree1Node.getValue().compareTo(tree2Node.getValue())!=0) {
            return (CDP);//The current CDP is zero
        } else {
            //Get list of children
            CDP= confObject.dependencyTreeHyperParameter*(CDP+1);//Add CDP by 1, because this is one common node
            List<utils.TreeNode> tree1Children = tree1Node.getChildrens();//List of children
            List<utils.TreeNode> tree2Children = tree2Node.getChildrens();//List of children
            //Iterate over children now
            for (utils.TreeNode tree1Child: tree1Children) {
                for (utils.TreeNode tree2Child: tree2Children) {
                    CDP = commonDownardPathRecursion(CDP,tree1Child,tree2Child,invDependency1,invDependency2);//Recursive operation
                }
            }

        }
        return(CDP);
    }



    public static Map<String,Map<String,Float>> commonDownwardPathSubsetTree(List<utils.TreeNode> subset1,List<utils.TreeNode> subset2) {

        Map<String,Map<String,Float>> commonPath = new HashMap<String, Map<String, Float>>();

        Map<String,Float> CDP = new HashMap<String, Float>();
        Map<String,Float> CPP = new HashMap<String, Float>();

        int subsetIndex = 0;
        for (utils.TreeNode node1: subset1) {
            for (utils.TreeNode node2: subset2) {

                commonDownardPathRecursionOptimized(0,0,subsetIndex,node1,node2,new HashMap<utils.TreeNode, Integer>(),new HashMap<utils.TreeNode, Integer>(),CDP,CPP);
                subsetIndex+=1;
            }
        }

        commonPath.put("CDP",CDP);
        commonPath.put("CPP",CPP);
        return (commonPath);
    }


    /**
     * TASK: calculates both CDP and CPP for the given trees
     * @param dependency1
     * @param dependency2
     * @param invDependency1
     * @param invDependency2
     * @return CDP and CPP for each pair of nodes between two trees
     */
    public static Map<String,Map<String,Float>> commonDownwardPath(Map<Integer, utils.TreeNode> dependency1,
                                                                   Map<Integer, utils.TreeNode> dependency2,
                                                                   Map<utils.TreeNode, Integer> invDependency1,
                                                                   Map<utils.TreeNode, Integer> invDependency2) {

        Map<String,Map<String,Float>> output = new HashMap<String, Map<String, Float>>();
        Map<String,Float> CDP = new HashMap<String, Float>();
        Map<String,Float> CPP = new HashMap<String, Float>();
        //System.out.print("Time1:"+(end-init)+"\n");
        //System.out.print((end-init)+"\n");
        long init = System.currentTimeMillis();
        //Kernel calculation
        for (Map.Entry<Integer,utils.TreeNode> entry1: dependency1.entrySet()) {
            for (Map.Entry<Integer,utils.TreeNode> entry2: dependency2.entrySet()) {
                //Calculate commmonDownwardPath for this combination
                if ((entry1.getKey()<0) || (entry2.getKey()<0)) continue;//For now, we skip ROOT
                if (!CDP.containsKey(entry1.getKey()+"|"+entry2.getKey())) {
                    //This recursive function takes nodes and calculates CPP and CDP
                    float result = commonDownardPathRecursionOptimized(0,0,-1,entry1.getValue(),entry2.getValue(),invDependency1,invDependency2,CDP,CPP);
                }

            }
        }
        //Fill out those not-generated CPPS
        for (String key: CDP.keySet()) {
            if (!CPP.containsKey(key)) {
                String[] splitted = key.split("\\|");
                int node1 = Integer.parseInt(splitted[0]);
                int node2 = Integer.parseInt(splitted[1]);
                String childKey = node1+"|"+node2;
                //Calculate CPP here
                commonPeakPathOptimized("",dependency1.get(node1),dependency2.get(node2),CDP,childKey
                        ,CPP,invDependency1,invDependency2);
            }
        }
        long end = System.currentTimeMillis();
        //System.out.print((end-init)+"\n");
        //System.out.print(CP2+"\n");
        //System.out.print((end-init)+"\n");
        output.put("CDP",CDP);
        output.put("CPP",CPP);
        return (output);
    }

    /**
     * TASK
     * @param CDP
     * @param tree1Children
     * @param tree2Children
     * @param invDependency1
     * @param invDependency2
     * @return
     */
    public static float calculateCPPContribution(String postFix,Map<String,Float> CDP,
                                                 List<utils.TreeNode> tree1Children,
                                                 List<utils.TreeNode> tree2Children,
                                                 Map<utils.TreeNode, Integer> invDependency1,
                                                 Map<utils.TreeNode, Integer> invDependency2) {

        float cppCont = 0;
        List<String> pairedIndexes = new ArrayList<String>();
        for (utils.TreeNode node1: tree1Children) {
            for (utils.TreeNode node2: tree2Children) {
                if (node1.getValue().compareTo(node2.getValue())==0) {
                    //There is a common child between these two trees (c1.w=c2.w)
                    //int index1 = invDependency1.get(node1);
                    //int index2 = invDependency2.get(node2);
                    //ADDED BY ALI
                    int index1 = node1.hashkey;
                    int index2 = node2.hashkey;
                    //ADDED BY ALI
                    pairedIndexes.add(index1+"|"+index2+postFix);
                    break;
                }
            }
        }


        if (pairedIndexes.size()!=tree1Children.size()) {
            //We cannot see common permutation
            return (cppCont);
        } else {
            //Refer to the paper for explanation
            float sum = 0;
            float multiply = 1;
            for (String key: pairedIndexes) {
                //System.out.print(key+"\n");
                if (CDP.containsKey(key)) {
                    sum+= (float) CDP.get(key);
                    multiply*= (float) CDP.get(key);
                }

            }
            return (cppCont+1*confObject.dependencyTreeHyperParameter*confObject.dependencyTreeHyperParameter+
                    confObject.dependencyTreeHyperParameter*sum+confObject.dependencyTreeHyperParameter*confObject.dependencyTreeHyperParameter*multiply);
        }
    }

    /**
     * Calculates Common Peak Paths for each node in the dependency tree
     * @param CDP Common
     * @param dependency1 HashMap of nodes in the first dependency tree
     * @param dependency2 HashMap of nodes in the second dependency tree
     * @param invDependency1 InverseHashmap for the first tree (maps each node to ID)
     * @param invDependency2 InverseHashmap for the second tree (maps each node to ID)
     * @return For each tuple of nodes in trees, returns Common Peak Pathes
     */
    public static Map<String,Float> commonPeakPath(Map<String,Float> CDP,Map<Integer, utils.TreeNode> dependency1,
                                                   Map<Integer, utils.TreeNode> dependency2, Map<utils.TreeNode, Integer> invDependency1,
                                                   Map<utils.TreeNode, Integer> invDependency2) {
        Map<String,Float> CPP = new HashMap<String, Float>();
        //Loop over all nodes in dependency1 and dependency2
        for (Map.Entry<Integer,utils.TreeNode> entry1: dependency1.entrySet()) {
            for (Map.Entry<Integer,utils.TreeNode> entry2: dependency2.entrySet()) {
                //Calculate commmonDownwardPath for this combination
                if ((entry1.getKey()<0) || (entry2.getKey()<0)) continue;//For now, we skip ROOT
                float tempCPP = (float) CDP.get(entry1.getKey()+"|"+entry2.getKey());//Initialize with CDP since "common peak paths" value is initialized by a CDP
                //Find list of children for any of these two nodes
                List<utils.TreeNode> tree1Children = entry1.getValue().childrens;//List of children nodes for tree1Node
                List<utils.TreeNode> tree2Children = entry2.getValue().childrens;//List of children nodes for tree2Node
                //Get minimum number of children
                int minChildNumber = Math.min(tree1Children.size(),tree2Children.size());//Go with minimum number
                if (minChildNumber<2){
                    CPP.put(entry1.getKey()+"|"+entry2.getKey(),tempCPP);
                    continue;//with only one child, just go with CDP
                }
                for (int permutationSize=2;permutationSize<=minChildNumber;permutationSize++) {
                    if (permutationSize>2) continue;//For now, ignore longer common paths
                    //Generate all node permutations
                    List<List<utils.TreeNode>> tree1Permuations = generateChildrenPermutation(new ArrayList<List<utils.TreeNode>>(),
                            new ArrayList<utils.TreeNode>(),tree1Children,permutationSize);
                    List<List<utils.TreeNode>> tree2Permuations = generateChildrenPermutation(new ArrayList<List<utils.TreeNode>>(),
                            new ArrayList<utils.TreeNode>(),tree2Children,permutationSize);
                    //if (entry1.getKey()==4 && entry2.getKey()==7) {
                    //  System.out.print(tree2Children.size()+"\n\n");
                    //}
                    //Loop over all possible permutations
                    for (List<utils.TreeNode> tree1Permute: tree1Permuations) {
                        for (List<utils.TreeNode> tree2Permute: tree2Permuations) {
                            //we need to compare these two permutations
                            tempCPP+= calculateCPPContribution("",CDP,tree1Permute,tree2Permute,invDependency1,invDependency2);//This is the final CPP
                        }
                    }
                }
                CPP.put(entry1.getKey()+"|"+entry2.getKey(),tempCPP);
            }
        }
        return (CPP);
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
     * TASK: this method gets the raw-text and extracts ConstituencyTree
     * @param rawText
     * @return List of vertrices and edges in the constituencyTree
     * @throws Exception
     * Example: raw-text: I shot an elephent in my pajamas==> Output: [(ROOT-1 ,S-2), (S-2 ,NP-3), (S-2 ,VP-4), (NP-3 ,PRP-5), (VP-4 ,VBD-6), (VP-4 ,NP-7), (VP-4 ,PP-8), (NP-7 ,DT-9), (NP-7 ,NN-10), (PP-8 ,IN-11), (PP-8 ,NP-12), (NP-12 ,PRP$-13), (NP-12 ,NNS-14)]
     */
    public static List<String> extractConstituencyTree (String rawText) throws Exception {
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        tlp.setGenerateOriginalDependencies(true);
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        TreeRepresentation tree = new TreeRepresentation();
        String[] sent = rawText.split(" ");
        List<CoreLabel> rawWords = edu.stanford.nlp.ling.Sentence.toCoreLabelList(sent);
        Tree parsedTree = lp.apply(rawWords);
        GrammaticalStructure gs1 = gsf.newGrammaticalStructure(parsedTree);
        tree.setDataFromText(parsedTree.toString());
        return (constitutencyTreeToList(tree));
    }

    /**
     * TASK: this method gets the raw-text and extracts Dependency tree from StanfordParser
     * Based on Hector's Nodes, it is "TreeBuilder", I am not sure what variation it is (based on the notes about variaties)
     * @param rawText
     * @throws Exception
     * @return Flat list of labels
     * EXAMPLE: raw-text: I shot an elephent in my pajamas==> Output: [nsubj(shot-2, I-1), root(ROOT-0, shot-2), det(elephent-4, an-3), dobj(shot-2, elephent-4), case(pajamas-7, in-5), nmod:poss(pajamas-7, my-6), nmod:in(shot-2, pajamas-7)]
     */
    public static List<String> extractDependencyTree (String rawText) throws Exception {

        List<CoreLabel> rawWords = Sentence.toCoreLabelList(rawText.split(" "));
        Tree parsedTree = lp.apply(rawWords);
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        GrammaticalStructure gs1 = gsf.newGrammaticalStructure(parsedTree);
        List<TypedDependency> tdl1 = gs1.typedDependenciesCCprocessed();

        List<String> stringTdl1 = new ArrayList<String>();
        for (TypedDependency dep: tdl1) stringTdl1.add(dep.toString());
        return(stringTdl1);
    }


    /**
     * TASK get constitutency tree and converts it to a list of PARENT/CHILD tuples
     * @param tree SyntactTree representation of the input sentence
     * @return A list of tuples with (PARENT_LABEL-id,CHILD_LABEL-id)
     */
    public static List<String> constitutencyTreeToList(TreeRepresentation tree) {
        List<String> outputList = new ArrayList<String>();
        LinkedList<TreeNode> nodeKeeper = new LinkedList<TreeNode>();
        LinkedList<Integer> idKeeper = new LinkedList<Integer>();
        //get root
        nodeKeeper.add(tree.getRoot());
        int idCounter = 1;
        idKeeper.add(idCounter);

        while (!nodeKeeper.isEmpty()) {
            TreeNode currentNode = nodeKeeper.pop();
            String headValue = currentNode.getContent().getTextFromData();
            int headId = idKeeper.pop();
            for (TreeNode child: currentNode.getChildren()) {
                if (!child.hasChildren()) continue;
                idCounter+=1;
                String depValue = child.getContent().getTextFromData();
                int depId = idCounter;
                outputList.add("("+headValue+"-"+headId+" ,"+depValue+"-"+depId+")");
                nodeKeeper.add(child);
                idKeeper.add(idCounter);
            }
        }
        return (outputList);
    }


    /**
     *
     * @param text1
     * @param text2
     * @param minDepth
     * @param maxDepth
     * @param minWidth
     * @param maxWidth
     * @param type
     * @return
     * @throws Exception
     */
    public static double calculateSubsetKernelSimilarity(String text1,String text2,int minDepth,
                                                       int maxDepth,int minWidth,int maxWidth,Enums.Type type) throws Exception{


        List<String> dependencyListTree1 = new ArrayList<String>();
        List<String> dependencyListTree2 = new ArrayList<String>();

        if (type==Enums.Type.CONSTITUENCY) {
            dependencyListTree1 = extractConstituencyTree(text1);
            dependencyListTree2 = extractConstituencyTree(text2);
        } else if (type == Enums.Type.StanfordDependency) {
            dependencyListTree1 = extractDependencyTree(text1);
            dependencyListTree2 = extractDependencyTree(text2);
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

        //Loop over all the intra-nodes
        Map<String,List<utils.TreeNode>> mapOfIntraSubsets = new HashMap<String, List<utils.TreeNode>>();
        for (Map.Entry<Integer,utils.TreeNode> entry1: tree1Nodes.entrySet()) {
            for (Map.Entry<Integer,utils.TreeNode> entry2: tree2Nodes.entrySet()) {
                //Calculate commmonDownwardPath for this combination
                if ((entry1.getKey()<0) || (entry2.getKey()<0)) continue;//For now, we skip ROOT
                if (!mapOfIntraSubsets.containsKey(entry1.getKey()+"|"+entry2.getKey())) {
                    //This recursive function takes nodes and calculates CPP and CDP
                    subsetTreeKernel(entry1.getValue(),entry2.getValue(),mapOfIntraSubsets,minDepth,maxDepth,minWidth,maxDepth);
                }
            }
        }

        double totalNumSubsets= 0 ;
        for (Map.Entry<String,List<utils.TreeNode>> entry: mapOfIntraSubsets.entrySet()) {
            totalNumSubsets+= entry.getValue().size();
        }
        return (totalNumSubsets);

    }


    /**
     *
     * @param text1
     * @param text2
     * @param type
     * @return
     * @throws Exception
     */
    public static double calculateKernelSimilarity(String text1,String text2,Enums.Type type) throws Exception {

        List<String> dependencyListTree1 = new ArrayList<String>();
        List<String> dependencyListTree2 = new ArrayList<String>();

        if (type==Enums.Type.CONSTITUENCY) {
            dependencyListTree1 = extractConstituencyTree(text1);
            dependencyListTree2 = extractConstituencyTree(text2);
        } else if (type == Enums.Type.StanfordDependency) {
            dependencyListTree1 = extractDependencyTree(text1);
            dependencyListTree2 = extractDependencyTree(text2);
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

        //Initialize values here:
        //CommonDownwardPath (number of common paths between each two nodes of trees that originate from the aforementioned nodes and go all downward)
        Map<String,Float> intraCDP = new HashMap<String, Float>();
        Map<String,Float> intraCDPOpt = new HashMap<String, Float>();

        //CommonPickPath (number of common paths between each two nodes of trees that peak at the mentioned two nodes. That is the paths should have the aforementioned nodes as the ROOT.)
        Map<String,Float> intraCPP = new HashMap<String, Float>();
        Map<String,Float> CPP1 = new HashMap<String, Float>();
        Map<String,Float> CPP2 = new HashMap<String, Float>();

        //This calculates all the CPP and CDP values
        Map<String,Map<String,Float>> output =
                commonDownwardPath(tree1Nodes,tree2Nodes,invTree1Nodes,invTree2Nodes);//CommonDownwardPaths

        System.out.print(output.get("CPP")+"\n");
        long end = System.currentTimeMillis();

        intraCPP = output.get("CPP");
        //Calculate self-CPP and self-CDP values
        output = commonDownwardPath(tree1Nodes,tree1Nodes,invTree1Nodes,invTree1Nodes);
        CPP1 = output.get("CPP");
        output = commonDownwardPath(tree2Nodes,tree2Nodes,invTree2Nodes,invTree2Nodes);
        CPP2 = output.get("CPP");

        //Calculate similarity based on the extracted CPP values
        double similarity = ((double) calcKernel(intraCPP))/Math.sqrt((double) (calcKernel(CPP1)*calcKernel(CPP2)));
        end = System.currentTimeMillis();
        //System.out.print((end-init)+"\n");
        return (similarity);
    }


    /**
    public double stringKernel(List<String> dependencyListTree1) {
        long init = System.currentTimeMillis();
        TreeBuilder depTree1 = new TreeBuilder();
        boolean status1 = depTree1.initTreeStringInput(dependencyListTree1);
        if (!status1) return (-1);
        //Get list of all nodes

        Map<Integer, utils.TreeNode> tree1Nodes = depTree1.treemap;
        //Creates inverse-treeNode
        Map<utils.TreeNode, Integer> invTree1Nodes = new HashMap<utils.TreeNode, Integer>();
        for (Map.Entry<Integer, utils.TreeNode> entry: tree1Nodes.entrySet()) {
            invTree1Nodes.put(entry.getValue(),entry.getKey());
        }

        Map<String,Map<String,Float>> output =
                commonDownwardPath(tree1Nodes,tree1Nodes,invTree1Nodes,invTree1Nodes);//CommonDownwardPaths
        Map<String,Float> CPP1 = new HashMap<String, Float>();
        CPP1 = output.get("CPP");
        return (calcKernel(CPP1));

    }
     **/

    /**
     * TASK calculates similatiry based on the subtree-kernal
     * @param dependencyListTree1 List-based tree representation for the first constituency tree
     * @param dependencyListTree2 List-based tree representation for the second constituency tree
     * @return
     */

    /**
    public double calculateKernelSimilarityWithQueryKernel(List<String> dependencyListTree1,List<String> dependencyListTree2,float selfQueryNorm,float selfAnswerNorm) {

        //STEP1: convert to TreeNode object
        //System.out.print(dependencyListTree1+"\n");
        //System.out.print(dependencyListTree1+"\n");
        long init = System.currentTimeMillis();
        TreeBuilder depTree1 = new TreeBuilder();
        TreeBuilder depTree2 = new TreeBuilder();
        boolean status1 = depTree1.initTreeStringInput(dependencyListTree1);
        boolean status2 = depTree2.initTreeStringInput(dependencyListTree2);
        if (!status1 || !status2) return (-1);
        //Get list of all nodes

        Map<Integer, utils.TreeNode> tree1Nodes = depTree1.treemap;
        Map<Integer, utils.TreeNode> tree2Nodes = depTree2.treemap;
        //Creates inverse-treeNode
        Map<utils.TreeNode, Integer> invTree1Nodes = new HashMap<utils.TreeNode, Integer>();
        Map<utils.TreeNode, Integer> invTree2Nodes = new HashMap<utils.TreeNode, Integer>();
        for (Map.Entry<Integer, utils.TreeNode> entry: tree1Nodes.entrySet()) {
            invTree1Nodes.put(entry.getValue(),entry.getKey());
        }
        for (Map.Entry<Integer, utils.TreeNode> entry: tree2Nodes.entrySet()) {
            invTree2Nodes.put(entry.getValue(),entry.getKey());
        }

        Map<String,Float> intraCDP = new HashMap<String, Float>();
        Map<String,Float> intraCDPOpt = new HashMap<String, Float>();

        Map<String,Float> intraCPP = new HashMap<String, Float>();
        Map<String,Float> CPP1 = new HashMap<String, Float>();
        Map<String,Float> CPP2 = new HashMap<String, Float>();

        //intraCDP and CPP

        Map<String,Map<String,Float>> output =
                commonDownwardPath(tree1Nodes,tree2Nodes,invTree1Nodes,invTree2Nodes);//CommonDownwardPaths
        long end = System.currentTimeMillis();

        intraCPP = output.get("CPP");
        //System.out.print(intraCDP+"\n");
        double similarity = ((double) calcKernel(intraCPP))/Math.sqrt((double) (selfAnswerNorm*selfQueryNorm));
        end = System.currentTimeMillis();
        //System.out.print((end-init)+"\n");
        return (similarity);
    }
    **/

    public static void main(String[] argv) throws Exception {


        //TreeKernel dep = new TreeKernel();
        //List<String> dep = extractDependencyTree("I shot an elephent in my pajamas");
        //TreeBuilder depTree1 = new TreeBuilder();
        //boolean status1 = depTree1.initTreeStringInput(dep);
        //System.exit(1);

        //Example:

        Enums.Type type = Enums.Type.StanfordDependency;

        String sampleText = "Deep learning tools have gained tremendous attention in applied machine learning.";
        String sampleText2 = "Deep learning algorithms have received much attention in applied machine learning.";

        List<String> constTree = extractConstituencyTree(sampleText);
        List<String> constTree2 = extractConstituencyTree(sampleText2);

        System.out.print(constTree+"\n");
        System.out.print(constTree2+"\n");

        //constTree = extractDependencyTree(sampleText);

        //double similarity = calculateKernelSimilarity(constTree,constTree2);
        //System.exit(1);

        TreeBuilder depTree1 = new TreeBuilder();
        TreeBuilder depTree2 = new TreeBuilder();




        boolean status1 = depTree1.initTreeStringInput(constTree);
        boolean status2 = depTree2.initTreeStringInput(constTree2);

        List<utils.TreeNode> allSubsets = new ArrayList<utils.TreeNode>();
        Map<String,List<utils.TreeNode>> subsetMap = new HashMap<String, List<utils.TreeNode>>();



        subsetTreeKernel(depTree1.root.getChildrens().get(0),depTree2.root.getChildrens().get(0),subsetMap,-1,1000,-1,1000);
        //System.out.print(depTree1.root.getChildrens().get(0).getValue().compareTo(depTree2.root.getChildrens().get(0).getValue())+"\n");
       // System.out.print(depTree1.root.getChildrens().get(0).getHashkey()+"\n");
        double intraComm = calculateSubsetKernelSimilarity(sampleText,sampleText2,-1,3,-1,3,type);
        double selfComm1 = calculateSubsetKernelSimilarity(sampleText,sampleText,-1,3,-1,3,type);
        double selfComm2 = calculateSubsetKernelSimilarity(sampleText2,sampleText2,-1,3,-1,3,type);
        double similarity = ((double) intraComm)/Math.sqrt((double) (selfComm1*selfComm2));
        System.out.print(similarity+"\n");
        System.out.print(calculateKernelSimilarity(sampleText,sampleText2,type));

        /**
         TreebankLanguagePack tlp = new PennTreebankLanguagePack();
         tlp.setGenerateOriginalDependencies(true);
         GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
         TreeRepresentation tree = new TreeRepresentation();
         TreeRepresentation tree2 = new TreeRepresentation();
         String[] sent = text.split(" ");
         List<CoreLabel> rawWords = edu.stanford.nlp.ling.Sentence.toCoreLabelList(sent);

         String[] sent2 = text2.split(" ");
         List<CoreLabel> rawWords2 = edu.stanford.nlp.ling.Sentence.toCoreLabelList(sent2);
         Tree parsedTree = lp.apply(rawWords);
         GrammaticalStructure gs1 = gsf.newGrammaticalStructure(parsedTree);
         Collection<TypedDependency> tdl1 = gs1.typedDependenciesCCprocessed();

         Tree parsedTree2 = lp.apply(rawWords2);
         GrammaticalStructure gs2 = gsf.newGrammaticalStructure(parsedTree2);
         tree.setDataFromText(parsedTree.toString());
         tree2.setDataFromText(parsedTree2.toString());
         SubTreeKernel subTreeKernel = new SubTreeKernel();
         SubSetTreeKernel subSetTreeKernel = new SubSetTreeKernel();
         System.out.print(subSetTreeKernel.kernelComputation(tree,tree2)/Math.sqrt(subSetTreeKernel.kernelComputation(tree,tree)*subSetTreeKernel.kernelComputation(tree2,tree2))+"\n\n\n\n");
         System.exit(1);
         **/

    }
}