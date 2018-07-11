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


    public static String genHeader()
    {
        String header = "Id\tSent1\tSent2\tSTANFORD_IDENTITY\tSTANFORD_SOFT\tSTANFORD_HYBRID\tUD_IDENTITY\tUD_SOFT" +
                "\tUD_HYBRID\tCONST_IDENTITY\tSTANFORD_EMBED\tSTANFORD_HYBRID_EMBED\tUD_EMBED\tUD_HYBRID_EMBED\tBASE_VECTOR\tBASE_JACC_WORD\t" +
                "BASE_JACC_POSTAG\tBASE_JACC_RELATION\n";
        return (header);
    }
    public static String runExperiment(String Id,String text1,String text2) throws Exception
    {

        String stats = "";
        stats+= Id+"\t"+text1+"\t"+text2+"\t";
        TreeKernelOptimized kernel = new TreeKernelOptimized();

        //
        Enums.DependencyType type = Enums.DependencyType.StandardStanford;
        Enums.VectorizationType vecType = Enums.VectorizationType.WordIdentity;
        SubsetTreeStats self = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        SubsetTreeStats selff = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        SubsetTreeStats intraa = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,false);
        double sim = intraa.getTotalNumSubsets()/(Math.sqrt(self.getTotalNumSubsets()*selff.getTotalNumSubsets()));
        stats+= sim+"\t";

        type = Enums.DependencyType.StandardStanford;
        vecType = Enums.VectorizationType.StandardStanford;
        self = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        selff = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        intraa = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,false);
        sim = intraa.getTotalNumSubsets()/(Math.sqrt(self.getTotalNumSubsets()*selff.getTotalNumSubsets()));
        stats+= sim+"\t";

        type = Enums.DependencyType.StandardStanford;
        vecType = Enums.VectorizationType.HybridStanford;
        self = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        selff = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        intraa = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,false);
        sim = intraa.getTotalNumSubsets()/(Math.sqrt(self.getTotalNumSubsets()*selff.getTotalNumSubsets()));
        stats+= sim+"\t";

        type = Enums.DependencyType.UDV1;
        vecType = Enums.VectorizationType.WordIdentity;
        self = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        selff = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        intraa = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,false);
        sim = intraa.getTotalNumSubsets()/(Math.sqrt(self.getTotalNumSubsets()*selff.getTotalNumSubsets()));
        stats+= sim+"\t";

        type = Enums.DependencyType.UDV1;
        vecType = Enums.VectorizationType.UDV1;
        self = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        selff = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        intraa = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,false);
        sim = intraa.getTotalNumSubsets()/(Math.sqrt(self.getTotalNumSubsets()*selff.getTotalNumSubsets()));
        stats+= sim+"\t";

        type = Enums.DependencyType.UDV1;
        vecType = Enums.VectorizationType.HybridUD;
        self = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        selff = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        intraa = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,false);
        sim = intraa.getTotalNumSubsets()/(Math.sqrt(self.getTotalNumSubsets()*selff.getTotalNumSubsets()));
        stats+= sim+"\t";

        type = Enums.DependencyType.CONSTITUENCY;
        vecType = Enums.VectorizationType.WordIdentity;
        self = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        selff = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,true);
        intraa = kernel.calculateSubsetKernelSimilarity(text1,text2,type,vecType,Enums.TreeStat.MEAN,.2,1,1,1,false);
        sim = intraa.getTotalNumSubsets()/(Math.sqrt(self.getTotalNumSubsets()*selff.getTotalNumSubsets()));
        stats+= sim+"\t";

        //Embeddings
        stats+= "\t\t\t\t";

        //BaseLines
        //
        return (stats);
    }

    public static void main(String[] argv) throws Exception {

        Enums.DependencyType type = Enums.DependencyType.StandardStanford;

        String text = "It takes another hour to search for the Book of the Dead 's opposite number , which will theoretically send Imhotep back to the cosmic soup from which he sprang before he can transfer the heroine 's soul to the embalmed remains of his lady love .";
        text = "They 're self-conscious , postmodern comments on crudely sexualized violent films .";

        text = "Four kids are doing backbends in the park";
        String text2 = "Four children are doing backbends in the park";

        //text = "I ate food at restaurent";
        //text2 = "He was at restaurent yesterday";
        TreeKernelOptimized kernel = new TreeKernelOptimized();
        //BaseLineStats sim = kernel.calculateBaseLineSimilarity(text,text2,type,Enums.VectorizationType.StandardStanford,Enums.TreeStat.MEAN,.9,1,1,1,false);

        SubsetTreeStats self = kernel.calculateSubsetKernelSimilarity(text,text,type,Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.2,1,1,1,true);
        SubsetTreeStats selff = kernel.calculateSubsetKernelSimilarity(text2,text2,type,Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.2,1,1,1,true);
        SubsetTreeStats intraa = kernel.calculateSubsetKernelSimilarity(text,text2,type,Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.2,1,1,1,false);
        //System.out.print(intraa.getTotalNumSubsets());

        //System.out.print(self.getTotalNumSubsets()+"\n");
        //System.out.print(selff.getTotalNumSubsets()+"\n");
        //System.out.print(intraa.getTotalNumSubsets()+"\n");
        System.out.print(intraa.getTotalNumSubsets()/(Math.sqrt(self.getTotalNumSubsets()*selff.getTotalNumSubsets())));
        //System.exit(1);
    }

}
