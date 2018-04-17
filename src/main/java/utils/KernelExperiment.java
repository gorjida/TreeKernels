package utils;

import java.util.*;

public class KernelExperiment {

    public void subsetIdentityKernel(String text1,String text2,int minDepth,int minWidth,int maxDepth,int maxWidth) throws Exception{
        TreeKernel kernel = new TreeKernel();
        List<String> constTreeText1 = TreeKernel.extractConstituencyTree(text1);
        List<String> constTreeText2 = TreeKernel.extractConstituencyTree(text2);


    }

    public static void main(String[] argv) {
        //Parameters

    }
}
