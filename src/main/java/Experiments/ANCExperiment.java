package Experiments;

import utils.*;

import java.io.IOException;
import java.io.*;
import java.util.*;

/**
 * Created by u6042446 on 2018-06-07.
 */
public class SentenceTokanization {

    public static final int lowSentLengthThreshold = 7;
    public static final int highSentLengthThreshold = 40;

    public static String printResults(SubsetTreeStats self,String sentenceId,String type,int sentenceLength)
    {
        return (sentenceId+"\t"+sentenceLength+"\t"+self.getLeftTreeDepth()+"\t"+self.getLeftTreeWidth()+"\t"+self.getTotalNumSubsets()+"\t"+type);
    }

    public static void main(String[] argv) throws Exception
    {
        String basePath = "/Users/u6042446/IdeaProjects/TreeKernels/data/";
        String readPath = basePath + "ANC_written_sentenceperline_v3.txt";
        String writePath = basePath + "stats.txt";

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(readPath),"UTF-8"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writePath),"UTF-8"));

        String line;

        int counter = 0;
        while ((line=reader.readLine())!=null)
        {
            String[] split = line.split("\t");
            String text = split[1];
            String Id = split[0];
            int sentLength = Integer.parseInt(split[2]);
            if (sentLength>=lowSentLengthThreshold && sentLength<=highSentLengthThreshold)
            {
                System.out.print(Id+","+sentLength+"\n");
                SubsetTreeStats selfUDV = TreeKernelOptimized.calculateSubsetKernelSimilarity(text,text,Enums.DependencyType.UDV1,
                        Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.1,1,1);

                SubsetTreeStats selfStanford = TreeKernelOptimized.calculateSubsetKernelSimilarity(text,text,Enums.DependencyType.StandardStanford,
                        Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.1,1,1);

                SubsetTreeStats selfConstituency = TreeKernelOptimized.calculateSubsetKernelSimilarity(text,text,Enums.DependencyType.CONSTITUENCY,
                        Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.1,1,1);

                String resultsUDV = printResults(selfUDV,Id,"UDV",sentLength);
                String resultsStanford = printResults(selfStanford,Id,"Stanford",sentLength);
                String resulsConst = printResults(selfConstituency,Id,"Constituency",sentLength);
                writer.write(resultsStanford+"\n");
                writer.write(resultsUDV+"\n");
                writer.write(resulsConst+"\n");
                if (counter%100==0 && counter>0) writer.flush();
                counter+=1;
            }
        }
        reader.close();
        writer.close();

    }
}
