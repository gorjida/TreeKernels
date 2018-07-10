package utils;

import conf.Configuration;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.*;
import it.uniroma2.sag.kelp.data.representation.tree.TreeRepresentation;
import it.uniroma2.sag.kelp.data.representation.tree.node.*;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.semgraph.SemanticGraph;
//import edu.stanford.nlp.trees.GrammaticalStructureConversionUtils;
import edu.stanford.nlp.trees.TreePrint;

//import guru.nidi.graphviz.engine.Graphviz;


/**
 * Created by u6042446 on 2018-05-17.
 */
public class ParseTree {

    public static Configuration confObject = Configuration.getInstance();
    public LexicalizedParser lp = LexicalizedParser.
            loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz","-maxLength","100","-retainTmpSubcategories"
            );



    /**
     * TASK get constitutency tree and converts it to a list of PARENT/CHILD tuples
     * @param tree SyntactTree representation of the input sentence
     * @return A list of tuples with (PARENT_LABEL-id,CHILD_LABEL-id)
     */
    public List<String> constitutencyTreeToList(TreeRepresentation tree) {
        List<String> outputList = new ArrayList<String>();
        LinkedList<it.uniroma2.sag.kelp.data.representation.tree.node.TreeNode> nodeKeeper = new LinkedList<it.uniroma2.sag.kelp.data.representation.tree.node.TreeNode>();
        LinkedList<Integer> idKeeper = new LinkedList<Integer>();
        //get root
        nodeKeeper.add(tree.getRoot());
        int idCounter = 1;
        idKeeper.add(idCounter);

        while (!nodeKeeper.isEmpty()) {
            it.uniroma2.sag.kelp.data.representation.tree.node.TreeNode currentNode = nodeKeeper.pop();
            String headValue = currentNode.getContent().getTextFromData();
            int headId = idKeeper.pop();
            for (it.uniroma2.sag.kelp.data.representation.tree.node.TreeNode child: currentNode.getChildren()) {
                //if ((!child.hasChildren()) || (Enums.PartOfSpeech.getPOSIndex(child.getContent().getTextFromData())>-1)) continue;
                if (!child.hasChildren() || Enums.PartOfSpeech.getPOSIndex(child.getContent().getTextFromData())>-1) continue;//Skip if the tag is a POSTAG
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
     * TASK: this method gets the raw-text and extracts ConstituencyTree
     * @param rawText
     * @return TreeBuilder object for the given sentence based on the ConstituencyTree
     * @throws Exception
     * Example: raw-text: I shot an elephent in my pajamas==> Output: [(ROOT-1 ,S-2), (S-2 ,NP-3), (S-2 ,VP-4), (NP-3 ,PRP-5), (VP-4 ,VBD-6), (VP-4 ,NP-7), (VP-4 ,PP-8), (NP-7 ,DT-9), (NP-7 ,NN-10), (PP-8 ,IN-11), (PP-8 ,NP-12), (NP-12 ,PRP$-13), (NP-12 ,NNS-14)]
     */
    public List<TreeBuilder> extractConstituencyTree (String rawText) throws Exception {
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        tlp.setGenerateOriginalDependencies(true);
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        TreeRepresentation tree = new TreeRepresentation();
        DocumentPreprocessor tokanizer = new DocumentPreprocessor(new StringReader(rawText));
        tokanizer.setTokenizerFactory(PTBTokenizer.factory());//Set tokenizer to the rule-based PTBTokenizer
        tokanizer.setSentenceDelimiter(" ");
        List<TypedDependency> tdl1 = new ArrayList<TypedDependency>();
        List<TreeBuilder> builders = new ArrayList<TreeBuilder>();

        for (List<HasWord> sentence: tokanizer) {
            Tree parsedTree = lp.apply(sentence);
            GrammaticalStructure gs1 = gsf.newGrammaticalStructure(parsedTree);
            tree.setDataFromText(parsedTree.toString());
            TreeBuilder treeForm = new TreeBuilder();
            treeForm.initTreeStringInput(constitutencyTreeToList(tree));
            builders.add(treeForm);
        }
        return (builders);
    }

    /**
     *
     * @param rawText input rawText
     * @return
     */
    public static boolean sentenceTokanizerStatus(String rawText)
    {
        boolean status = true;
        List<utils.Sentence> sentences = new ArrayList<Sentence>();
        TreebankLanguagePack tlp = new PennTreebankLanguagePack();
        tlp.setGenerateOriginalDependencies(true);
        GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
        TreeRepresentation tree = new TreeRepresentation();
        DocumentPreprocessor tokanizer = new DocumentPreprocessor(new StringReader(rawText));
        tokanizer.setTokenizerFactory(PTBTokenizer.factory());//Set tokenizer to the rule-based PTBTokenizer
        List<TypedDependency> tdl1 = new ArrayList<TypedDependency>();
        List<TreeBuilder> builders = new ArrayList<TreeBuilder>();

        int counter = 0;
        for (List<HasWord> sentence: tokanizer) {
            //System.out.print(sentence+"\n");
            counter+=1;
        }
        if (counter>1) status = false;
        return (status);
    }

    /**
     * Extracts Dependency parser based on two conventions: Standard STANFORD and UDD_V1
     * @param rawText
     * @param dependencyType type of the dependency parser (UDP and StanfordStandard)
     * @return TreeBuilder object for the given sentence and based on the given dependencyTree
     * @throws Exception
     */
    public List<TreeBuilder> extractDependencyTree (String rawText,Enums.DependencyType dependencyType
            ,boolean printDepGraph,String graphFilePath,Enums.VectorizationType vectorizationType) throws Exception {

        DocumentPreprocessor tokanizer = new DocumentPreprocessor(new StringReader(rawText));
        tokanizer.setTokenizerFactory(PTBTokenizer.factory());//Set tokanizer to the rule-based PTBTokenizer
        tokanizer.setSentenceDelimiter(" ");
        List<TypedDependency> tdl1 = new ArrayList<TypedDependency>();
        //Only a single-sentence is considered here
        List<TreeBuilder> sentenceTree = new ArrayList<TreeBuilder>();
        for (List<HasWord> sentence: tokanizer) {
            Tree parsedTree = lp.apply(sentence);

            TreebankLanguagePack tlp = new PennTreebankLanguagePack();
            if (dependencyType==Enums.DependencyType.StandardStanford) {
                tlp.setGenerateOriginalDependencies(true);//enforce the depndency to generate the original Stanford-dependency
            }
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            GrammaticalStructure gs1 = gsf.newGrammaticalStructure(parsedTree);

            //tdl1 = gs1.typedDependenciesCCprocessed();
            tdl1 = new ArrayList<TypedDependency>(gs1.typedDependencies());
            //System.out.print(tdl1.size()+"\n");


            //GrammaticalStructureConversionUtils.printDependencies(gs1,tdl1,parsedTree,true,false,false);
            SemanticGraph semgraph = new SemanticGraph(tdl1);

            TreeBuilder tree = new TreeBuilder();
            tree.initTreeFromUD(semgraph,vectorizationType);

            String dotFormat = semgraph.toDotFormat();
            if (printDepGraph) {
                GraphViz gv = new GraphViz();
                gv.readDotFormat(dotFormat);
                String type = "gif";
                String repesentationType = "dot";
                java.io.File out = new java.io.File(graphFilePath + "."+type);
                gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type, repesentationType), out);
            }
            sentenceTree.add(tree);
        }
        List<String> stringTdl1 = new ArrayList<String>();
        for (TypedDependency dep: tdl1) stringTdl1.add(dep.toString());
        return(sentenceTree);
    }


    public static void main(String[] argv) throws Exception{

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/u6042446/IdeaProjects/TreeKernels/data/ANC_written_sentenceperline_v4.txt")));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/u6042446/IdeaProjects/TreeKernels/data/list_of_ud_labels.txt")));
        String line;
        ParseTree parseTree = new ParseTree();
        Set<String> labels = new HashSet<String>();
        int counter = 0;
        while ((line=reader.readLine())!=null)
        {
            counter+=1;
            if (counter%1000==0) System.out.print(counter+"\n");
            String[] records = line.split("\t");
            String id = records[0];
            String text = records[1];
            if (sentenceTokanizerStatus(text)) continue;
            List<TreeBuilder> tree = parseTree.extractDependencyTree(text,Enums.DependencyType.UDV1,false,"/Users/u6042446/Desktop/test",
                    Enums.VectorizationType.WordIdentity);
            for (Map.Entry<Integer,TreeNode> entry: tree.get(0).treemap.entrySet())
            {
                if (entry.getKey()<0) continue;
                for (String rel: entry.getValue().getGrammaticalRelations())
                {
                    labels.add(rel);
                }
            }
            //writer.write(line+"\n");
        }
        for (String l: labels) writer.write(l+"\n");
        writer.close();
        reader.close();
        System.exit(1);

        //ParseTree parseTree = new ParseTree();
        String text = "Washington , D.C. : December 1986 .";
        text = "Hector is writing";
        //System.out.print(sentenceTokanizerStatus(text)+"\n");
        //text = "teem of one 's countrymen -- as the services tout .";
        //System.out.print(text.split(" ").length+"\n");

        //List<TreeBuilder> treeConst = parseTree.extractConstituencyTree(text);

        //System.out.print(treeConst);
        List<TreeBuilder> tree = parseTree.extractDependencyTree(text,Enums.DependencyType.UDV1,false,"/Users/u6042446/Desktop/test",
                Enums.VectorizationType.UDV1);

        for (Map.Entry<Integer,TreeNode> entry: tree.get(0).treemap.entrySet())
        {
            System.out.print(entry.getValue().getGrammaticalRelations()+"\n");
        }
        System.exit(1);
        tree =  parseTree.extractDependencyTree(text,Enums.DependencyType.StandardStanford,true,"/Users/u6042446/Desktop/test2",Enums.VectorizationType.StandardStanford);

        //System.out.print(tree.get(0).treemap.get(2).getChildrens().get(1).getVector()+"\n");
        //System.out.print(tree.get(0).getTreeDepth()+"\t"+tree.get(0).getTreeWidth()+"\n");
        //tree = extractDependencyTree(text,Enums.DependencyType.StandardStanford,true,"/Users/u6042446/Desktop/test",Enums.VectorizationType.WordIdentity);
        //System.out.print(treeConst.get(0).getTreeDepth()+"\t"+treeConst.get(0).getTreeWidth()+"\n");
        //List<Sentence> allText = sentenceTokanizer(text);
        //System.out.print(allText);

    }
}
