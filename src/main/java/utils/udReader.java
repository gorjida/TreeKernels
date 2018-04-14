package utils;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.ud.CoNLLUDocumentReader;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.io.*;
import java.util.*;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphFactory;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.Generics;
public class udReader {

    public static final String treeBankFilePath = "/Users/u6042446/Downloads/en_partut-ud-dev.conllu";

    public static void main(String[] argv) throws IOException {
        //Define the reader
        CoNLLUDocumentReader reader = new CoNLLUDocumentReader();

        Iterator<SemanticGraph> it = reader.getIterator(IOUtils.readerFromString(treeBankFilePath));

        //Iterate over all the records
        while (it.hasNext()) {
            TreeBuilder builder = new TreeBuilder();
            SemanticGraph graph = it.next();
            builder.initTreeFromUD(graph);
            List<utils.TreeNode> allSubsets = new ArrayList<utils.TreeNode>();
            List<utils.TreeNode> subsets = TreeKernel.SubsetTree(builder.root.getChildrens().get(0), allSubsets,1);
            System.exit(1);
        }
       //System.out.print(graphs);

    }
}
