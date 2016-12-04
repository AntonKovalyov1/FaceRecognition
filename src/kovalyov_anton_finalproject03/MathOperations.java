/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

/**
 * The class to compute euclidian distances and min and max from arrays
 * @author Anton
 */
public class MathOperations {
    
    public static double getEuclidianDistance(double[] x1, double[] x2) {
        double sum = 0;
        for (int i = 0; i < x2.length; i++) {
            sum += Math.pow(x2[i] - x1[i], 2);
        }
        
        return Math.pow(sum, 0.5);
    }
    
    public static double getMinFromDoubleArray(double[] x) {
        double min = x[0];
        for (int i = 1; i < x.length; i++) {
            if (min > x[i])
                min = x[i];
        }
        return min;
    }
    
    public static double getMaxFromDoubleArray(double[] x) {
        double max = x[0];
        for (int i = 1; i < x.length; i++) {
            if (max < x[i])
                max = x[i];
        }
        return max;
    }
    
        public static int getMinIndexFromDoubleArray(double[] x) {
        int min = 0;
        for (int i = 1; i < x.length; i++) {
            if (x[min] > x[i])
                min = i;
        }
        return min;
    }
    
    public static int getMaxIndexFromDoubleArray(double[] x) {
        int max = 0;
        for (int i = 1; i < x.length; i++) {
            if (x[max] < x[i])
                max = i;
        }
        return max;
    }
}
