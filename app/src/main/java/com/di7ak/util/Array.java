package com.di7ak.util;

public class Array {

    public static int maxIndex(float[] source) {
        int maxIndex = 0;
        for (int i = 1; i < source.length; i++){
            float newnumber = source[i];
            if ((newnumber > source[maxIndex])){
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public static int maxIndex(int[] source) {
        int maxIndex = 0;
        for (int i = 1; i < source.length; i++){
            int newnumber = source[i];
            if ((newnumber > source[maxIndex])){
                maxIndex = i;
            }
        }
        return maxIndex;
    }

}
