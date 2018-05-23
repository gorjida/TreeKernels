package utils;

/**
 * Created by u6042446 on 2016-11-17.
 */

import conf.Configuration;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Operations {

    public static Configuration confObject = Configuration.getInstance();


    public static void treeNodePermutations(List<Integer> treeNodeMaxIndexes,List<List<Integer>> permutations
            ,List<Integer> tempPermutations,int currentIndex) {
        if (currentIndex>treeNodeMaxIndexes.size()-1) {
            permutations.add(tempPermutations);
            return;
        }
        int maxIndex = treeNodeMaxIndexes.get(currentIndex);
        for (int i=0;i<maxIndex;i++) {

            List<Integer> internalPermutation = new ArrayList<Integer>(tempPermutations);
            internalPermutation.add(i);
            treeNodePermutations(treeNodeMaxIndexes,permutations,internalPermutation,currentIndex+1);
        }
        return;
    }

    public static Set<String> stopWords() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(confObject.stropWords),"UTF-8"));
        Set<String> stopWordsSet = new HashSet<String>();
        String line;
        while ((line=reader.readLine())!=null){
            stopWordsSet.add(line);
        }
        reader.close();
        return (stopWordsSet);
    }

    public static int setSubtract (Set<String> set1,Set<String> set2) {
        int size = set1.size();
        for (String x: set1) {
            if (set2.contains(x)) size-=1;
        }
        return(size);
    }

    public static Set<String> setIntersect (Set<String> set1,Set<String> set2) {
        Set<String> commonSet = new HashSet<String>();
        for (String x: set1) {
            if (set2.contains(x)) commonSet.add(x);
        }
        return(commonSet);
    }

    public static Set<String> setUnion (Set<String> set1,Set<String> set2) {
        Set<String> out = new HashSet<String>();
        for (String obj: set1) out.add(obj);
        for (String obj: set2) out.add(obj);
        return (out);
    }



    public static String floatListToString (List<Float> vals) {
        String out="";
        int count = 0;
        for (float ob: vals) {
            out+= Float.toString(ob);
            if (count<vals.size()-1) {
                out+= "\t";
            }
            count+=1;
        }
        return (out);
    }

    public static String intListToString (List<Integer> vals) {
        String out="";
        int count = 0;
        for (int ob: vals) {
            out+= Integer.toString(ob);
            if (count<vals.size()-1) {
                out+= "\t";
            }
            count+=1;
        }


        return (out);
    }

    public static String stringSetToString (HashSet<String> vals,String delimitter) {
        String out="";
        int count = 0;
        for (String ob: vals) {
            out+= ob;
            if (count<vals.size()-1) {
                out+= delimitter;
            }
            count+=1;
        }
        return (out);
    }

    public static String stringListToString (List<String> vals,String delimitter) {
        String out="";
        int count = 0;
        for (String ob: vals) {
            out+= ob;
            if (count<vals.size()-1) {
                out+= delimitter;
            }
            count+=1;
        }
        return (out);
    }

    public static String stringListToString (List<String> vals) {
        String out="";
        int count = 0;
        for (String ob: vals) {
            out+= ob;
            if (count<vals.size()-1) {
                out+= " ";
            }
            count+=1;
        }


        return (out);
    }

    public static Set<Object> FLOAT_SET_TO_OBJECT (Set<Float> set) {
        return (new HashSet<Object>(new ArrayList<Object>(Arrays.asList(set.toArray()))));
    }

    public static Set<Float> OBJECT_SET_TO_FLOAT (Set<Object> set) {
        return (new HashSet<Float>(new ArrayList<Float>(Arrays.asList(set.toArray(new Float[set.size()])))));
    }

    public static Set<Object> STRING_SET_TO_OBJECT (Set<String> set) {
        return (new HashSet<Object>(new ArrayList<Object>(Arrays.asList(set.toArray()))));
    }

    public static Set<String> OBJECT_SET_TO_STRING (Set<Object> set) {
        return (new HashSet<String>(new ArrayList<String>(Arrays.asList(set.toArray(new String[set.size()])))));
    }

    public static float jaccard_similarity(Set<String> set1,Set<String> set2) {
        return ((setIntersect(set1,set2).size()*1f)/setUnion(set1,set2).size());
    }

    public static List<Float> stringToFloatList (String input,String delimitter) {
        List<Float> initList = new ArrayList<Float>();
        for (String x: input.split(delimitter)) {
            initList.add(Float.parseFloat(x));
        }
        return (initList);
    }


    public static float getAvgOfList(List<Float> input) {
        float avg = 0;
        int length = input.size();
        for (float x: input) avg+=x/length;
        return (avg);
    }

    public static float getMinOfList(List<Float> input) {
        float min = 1000000;

        for (float x: input) {
            if (x<min) min =x ;
        }
        return (min);
    }

    public static float getMaxOfList(List<Float> input) {
        float max = -1000000;

        for (float x: input) {
            if (x>max) max =x ;
        }
        return (max);
    }


    public static float getMedianOfList(List<Float> input) {

        int length = input.size();
        float median;
        Collections.sort(input);
        if (length%2==0) {
            median = (input.get(length/2)+input.get(length/2-1))/2;
        } else {
            median = input.get(length/2);
        }

        return (median);
    }

    public static float innderProduct (List<Float> input1,List<Float> input2) {
        float value = 0;
        for (int i=0;i<input1.size();i++) {
            value+= input1.get(i)*input2.get(i);
        }
        return (Math.abs(value));
    }

    public static float similarity (List<Float> input1,List<Float> input2) {
        float value = 0;
        float value2 = 0;
        float value3 = 0;

        for (int i=0;i<input1.size();i++) {
            value+= input1.get(i)*input2.get(i);
            value2+= input1.get(i)*input1.get(i);
            value3+= input2.get(i)*input2.get(i);
        }



        return (Math.abs(value)/((float) Math.sqrt(value2)*(float) Math.sqrt(value3)));
    }

    public static double[] generateWeights(double lambda,int[] indexes) {
        int numIndexes = indexes.length;
        double[] listOfWeights = new double[numIndexes];

        //Calculate summations
        double sum = 0;
        for (int index: indexes) {
            double term = Math.exp(-1*lambda*index);
            listOfWeights[index] = term;
            sum+= term;
        }

        //Normalization
        for (int i=0;i<listOfWeights.length;i++) {
            listOfWeights[i] = listOfWeights[i]/sum;
        }

        return (listOfWeights);
    }

    public static double calculateNodeSimilarity(utils.TreeNode leftNode,utils.TreeNode rightNode,Enums.VectorizationType vectorizationType)
    {
        if (vectorizationType==Enums.VectorizationType.WordIdentity) {
            if (leftNode.getValue().compareTo(rightNode.getValue())==0) {
                return (1);
            } else {
                return (0);
            }
        } else if (vectorizationType==Enums.VectorizationType.StandardStanford) {
            Vector<Integer> leftRepresentation = leftNode.getVector();
            Vector<Integer> rightRepresentation = rightNode.getVector();
            //Calculate similarity
            return (0);
        } else
        {
            return (0);
        }
    }



    public static void main(String[] argv) throws Exception{



        List<Integer> maxLength = new ArrayList<Integer>();
        maxLength.add(3);
        maxLength.add(3);
        //maxLength.add(2);
        //maxLength.add(2);
        //maxLength.add(2);
        //maxLength.add(2);
        //maxLength.add(2);
        //maxLength.add(2);
        //maxLength.add(2);

        List<List<Integer>> permutations = new ArrayList<List<Integer>>();

        treeNodePermutations(maxLength,permutations,new ArrayList<Integer>(),0);
        System.out.print("\n"+permutations);
        System.exit(1);

        int[] indexes = new int[5];
        indexes[0] = 0;
        indexes[1] = 1;
        indexes[2] = 2;
        indexes[3] = 3;
        indexes[4] = 4;
        double[] out = generateWeights(10,indexes);
        for (double x: out) System.out.print(x+"\n");
        System.exit(1);

        System.out.print(cleanText("What is IRC Section 183?"));
        System.exit(1);
        List<Float> sample = new ArrayList<Float>();
        sample.add(1f);
        sample.add(10f);
        sample.add(7f);
        sample.add(14f);
        sample.add(3f);
        sample.add(-4f);
        sample.add(4f);

        int length = 11;
        int medium = length%2;
        System.out.print(getMedianOfList(sample));
        System.exit(1);
        //Set<String> a = new HashSet<String>(new ArrayList<String>(Arrays.asList("a","b","c")));
        //Set<String> b = new HashSet<String>(new ArrayList<String>(Arrays.asList("aa","bb","cc")));
        //System.out.print(setSubtract(a,b));
        List<String> yy = new ArrayList<String>();
        yy.add("ali");
        yy.add("reza");
        System.out.print(stringListToString(yy,"|||"));
        System.exit(1);

        String[] x = {"a","b"};
        List<String> xx = new ArrayList<String>(Arrays.asList(x));
        Set<String> a = new HashSet<String>(xx);

        String c = "ali";
        Set<Object> d = STRING_SET_TO_OBJECT(a);

        //Set<Object> b = setUnion(d,d);
        //Set<String> bb = OBJECT_SET_TO_STRING(b);

        //for (String ob: bb) {
         //   System.out.print(ob.toString()+"\n");
        //}
    }

    public static String cleanText(String text) throws IOException{

        String[] tokens = text.split(" ");
        String cleanedString = "";
        int index = 0;
        for (String x: tokens) {
            String cleanedToken = x.replaceAll("[^a-zA-Z0-9 ]", "");
            int numUpperCase = 0;
            for (int i=0;i<cleanedToken.length();i++) {
                if (Character.isUpperCase(cleanedToken.codePointAt(i))) {
                    numUpperCase+=1;
                }
            }
            if (x.endsWith(".") && Character.isUpperCase(x.codePointAt(0)) && numUpperCase<2) {
                x = x.replace(".","");
            }
            if (stopWords().contains(x)) continue;
            cleanedString+= x;
            if (index<tokens.length-1) cleanedString+=" ";
            index+=1;
        }

        String resultString = cleanedString.replaceAll("[^\\x00-\\x7F]", "");//remove non-ascii characters
        resultString = resultString.replaceAll("[^a-zA-Z0-9. ]", "");//remove punctuations except for dot

        return (resultString);
    }
}
