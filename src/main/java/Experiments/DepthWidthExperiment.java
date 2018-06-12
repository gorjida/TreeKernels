package Experiments;

import utils.*;
/**
 * Created by u6042446 on 2018-05-30.
 */

import java.io.*;
import java.util.List;
import java.util.Set;
//import java.util.stream.Collectors;


public class DepthWidthExperiment {

    public static final String dataFilePath = "/Users/u6042446/IdeaProjects/TreeKernels/data/original/SICK-data/SICK_train.txt";
    public static final String outputFilePath = "/Users/u6042446/IdeaProjects/TreeKernels/data/original/SICK-data/depthWidthJune1st_SICK_DATA_constituency.txt";


    public static String printResults(SubsetTreeStats self,String sentenceId,String type,int sentenceLength)
    {
        return (sentenceId+"\t"+sentenceLength+"\t"+self.getLeftTreeDepth()+"\t"+self.getLeftTreeWidth()+"\t"+self.getTotalNumSubsets()+"\t"+type);
    }


    public static void main(String[] argv) throws Exception {

        Enums.DependencyType type = Enums.DependencyType.StandardStanford;

        String text = "It takes another hour to search for the Book of the Dead 's opposite number , which will theoretically send Imhotep back to the cosmic soup from which he sprang before he can transfer the heroine 's soul to the embalmed remains of his lady love .";
        //String text2 = "But instead of boring you with self-serving `` designspeak , '' I 've prepared a concise roll call of what 's new and different in the new and different";
        SubsetTreeStats self = TreeKernelOptimized.calculateSubsetKernelSimilarity(text,text,type,Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.1,1,1);
        //SubsetTreeStats selff = TreeKernelNew.calculateSubsetKernelSimilarity(text2,text2,-1,1000,-1,1000,type,Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.1,1,1);
        //SubsetTreeStats intraa = TreeKernelNew.calculateSubsetKernelSimilarity(text,text2,-1,1000,-1,1000,type,Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.1,1,1);
        System.out.print(printResults(self,"1","",10)+"\n");
        //System.out.print(selff.getTotalNumSubsets()+"\n");
        //System.out.print(intraa.getTotalNumSubsets()+"\n");
        //System.out.print(intraa.getTotalNumSubsets()/(Math.sqrt(self.getTotalNumSubsets()*selff.getTotalNumSubsets())));
        System.exit(1);
    }

}
