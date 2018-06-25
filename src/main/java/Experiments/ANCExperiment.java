package Experiments;
import utils.*;

import java.io.IOException;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import utils.Sentence;
import utils.TreeKernelOptimized;

/**
 * Created by u6042446 on 2018-06-07.
 */
public class ANCExperiment implements  Runnable {

    public static final int lowSentLengthThreshold = 7;
    public static final int highSentLengthThreshold = 40;
    public List<Sentence> listOfSentences;
    public BufferedWriter writer;
    public String filePath;
    public TreeKernelOptimized kernel;
    public int threadId;

    public String printResults(SubsetTreeStats self,int sentenceId,String type,int sentenceLength)
    {
        return (sentenceId+"\t"+sentenceLength+"\t"+self.getLeftTreeDepth()+"\t"+self.getLeftTreeWidth()+"\t"+self.getTotalNumSubsets()+"\t"+type);
    }

    public ANCExperiment(List<Sentence> listOfSentences,String filePath,int threadId)
    {
        this.listOfSentences = listOfSentences;
        this.kernel = new TreeKernelOptimized();
        this.threadId = threadId;
        try
        {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath)));
        } catch (IOException ex)
        {
            writer = null;
        }
    }

    public void run()
    {

        System.out.print("Processing Thread:"+this.threadId+"\n");
        List<String> batchStats = new ArrayList<String>();

        int counter = 0;
        for (Sentence sentence: listOfSentences)
        {
            //Create a kernel object
            String text = sentence.getSentenceText();
            int id = sentence.getSentenceId();
            //System.out.print(this.threadId+":"+id+"\n");
            int length = sentence.getSentLength();
            SubsetTreeStats selfUd;
            SubsetTreeStats selfStanford;
            SubsetTreeStats selfConstituency;

            try
            {
                selfUd = this.kernel.calculateSubsetKernelSimilarity(text,text,Enums.DependencyType.UDV1,
                        Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.1,1,Operations.DepthThresholds.get(Enums.DependencyType.UDV1),1,true);
            } catch (Exception ex)
            {
                selfUd = new SubsetTreeStats(-1,-1,-1,-1,-1);
            }

            try
            {
                selfStanford = this.kernel.calculateSubsetKernelSimilarity(text,text,Enums.DependencyType.StandardStanford,
                        Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.1,1,Operations.DepthThresholds.get(Enums.DependencyType.StandardStanford),1,true);

            } catch (Exception ex)
            {
                selfStanford = new SubsetTreeStats(-1,-1,-1,-1,-1);
            }

            try
            {
                selfConstituency = this.kernel.calculateSubsetKernelSimilarity(text,text,Enums.DependencyType.CONSTITUENCY,
                        Enums.VectorizationType.WordIdentity,Enums.TreeStat.MEAN,.1,1,Operations.DepthThresholds.get(Enums.DependencyType.CONSTITUENCY),1,true);

            } catch (Exception ex)
            {
                selfConstituency = new SubsetTreeStats(-1,-1,-1,-1,-1);
            }

            String resultsUDV = printResults(selfUd,id,"UDV",length);
            String resultsStanford = printResults(selfStanford,id,"Stanford",length);
            String resulsConst = printResults(selfConstituency,id,"Constituency",length);
            try
            {
                this.writer.write(resultsUDV+"\n"+resultsStanford+"\n"+resulsConst+"\n");
            } catch (IOException ex)
            {

            }

            counter+=1;

            if (counter%100==0 && counter>0)
            {
                System.out.print(this.threadId+":"+counter+"\n");
                try
                {
                    this.writer.flush();

                } catch (IOException ex)
                {

                }

            }

        }

        try
        {
            this.writer.close();
        } catch (IOException ex)
        {

        }
    }

    public static List<List<Sentence>> sliceList(LinkedList<Sentence> allRecords,int numPerSlice)
    {
        List<List<Sentence>> slices = new ArrayList<List<Sentence>>();

        int sliceLength = 0;
        List<Sentence> slice = new ArrayList<Sentence>();
        while (!allRecords.isEmpty())
        {
            Sentence currentSentence = allRecords.pop();
            if (sliceLength<numPerSlice)
            {
                slice.add(currentSentence);
                sliceLength+=1;

            } else
            {
                slices.add(slice);
                sliceLength = 0;
                slice = new ArrayList<Sentence>();
                slice.add(currentSentence);
                sliceLength+=1;
            }
        }
        slices.add(slice);
        return (slices);
    }

    public static void main(String[] argv) throws Exception
    {
        //int numThreads = 2;
        String basePath = "/data/TreeKernel/data/";
        //basePath = "/Users/u6042446/IdeaProjects/TreeKernels/data/";

        //String readPath = basePath + argv[0];
        String index = argv[2];
        //String index = "0";
        int numThreads = Integer.parseInt(argv[0]);
        //int numThreads = 2;
        String readPath = argv[1];
        //String readPath = basePath + "ANC_written_sentenceperline_v5.txt";
        String writePath = basePath + "stats";

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(readPath),"UTF-8"));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writePath),"UTF-8"));

        String line;

        int counter = 0;
        LinkedList<Sentence> allRecords = new LinkedList<Sentence>();
        while ((line=reader.readLine())!=null)
        {
            String[] split = line.split("\t");
            String text = split[1];
            int Id = Integer.parseInt(split[0]);
            int sentLength = Integer.parseInt(split[2]);
            if (sentLength>=lowSentLengthThreshold && sentLength<=highSentLengthThreshold)
            {
                allRecords.add(new Sentence(text,sentLength,Id));
            }
        }
        reader.close();
        //slice data

        //ANCExperiment experiment = new ANCExperiment(allRecords,writePath+"_"+index+".txt",index);
        //experiment.run();

        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        List<List<Sentence>> slices = sliceList(allRecords,allRecords.size()/numThreads);
        //pool.execute(new ANCExperiment(slices.get(0),writePath+"_"+0+".txt",0));

        //Create a list of tasks
        //System.out.print(slices.get(1).size()+"\n");
        //System.exit(0);
        int workerId = 0;
        for (List<Sentence> slice: slices)
        {
            Runnable worker = new ANCExperiment(slice,writePath+"_"+workerId+ "_" + index + ".txt",workerId);
            workerId+=1;
            pool.execute(worker);
        }
        pool.shutdown();
        // Wait until all threads are finish
        pool.awaitTermination(100,TimeUnit.HOURS);
        writer.close();
    }
}
