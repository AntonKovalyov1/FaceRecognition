/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject02;

/**
 * A class for scaling arrays, getting an average array and the mean subtracted array
 * @author Anton
 */
public class ArrayOperations {
    
    /**
     * The method to scale a 2d double array to a 2d byte array (useful when 
     * displaying the eigenfaces as images)
     * @param doubleArray
     * @return 
     */
    public static byte[][] scale2DdoubleTo2Dbyte(double[][] doubleArray) {
        byte[][] byteArray = new byte[doubleArray.length][doubleArray[0].length];
        // Scale each row from the double2DArray
        for (int i = 0; i < byteArray.length; i++) {
            // For each row get the min and max values
            double min = doubleArray[i][0], max = doubleArray[i][0];
            for (int j = 1; j < doubleArray[i].length; j++) {
                if (min > doubleArray[i][j])
                    min = doubleArray[i][j];
                if (max < doubleArray[i][j])
                    max = doubleArray[i][j];
            }
            // Apply the formula for scaling to 0-255 integers
            double range = max - min;
            for (int k = 0; k < doubleArray[i].length; k++) {
                byteArray[i][k] = (byte)ImageIo.clip((doubleArray[i][k] - min) * 255.0 / range);
            }
        }
        return byteArray;
    }
    
    /**
     * The method to scale from a byte2DArray to a double2DArray for later matrix
     * processing
     * @param byteArray
     * @return 
     */
    public static double[][] scale2DbyteTo2Ddouble(byte[][] byteArray) {
        double[][] doubleArray = new double[byteArray.length][byteArray[0].length];
        // scale each row
        for (int i = 0; i < doubleArray.length; i++) {
            doubleArray[i] = scale1DbyteTo1Ddouble(byteArray[i]);
        }
        return doubleArray;
    }
    
    /**
     * The method to scale a row of bytes to doubles
     * @param byteArray
     * @return 
     */
    public static double[] scale1DbyteTo1Ddouble(byte[] byteArray) {
        double[] doubleArray = new double[byteArray.length];
        for (int i = 0; i < doubleArray.length; i++) {
                doubleArray[i] = (double)(byteArray[i] & 0xff);
        }
        return doubleArray;
    }
    
    /**
     * The method to get the average double1dArray from a double2dArray using it's columns
     * @param array
     * @return 
     */
    public static double[] getAverageArrayFrom2DArray(double[][] array) {
        double[] avg = new double[array[0].length];
        double sum = 0.0;
        // Get the sum of values from each column and then divide by the number of rows
        for (int i = 0; i < avg.length; i++) {
            for (int j = 0; j < array.length; j++) {
                sum += array[j][i];
                
            }
            avg[i] = sum / array.length;
            sum = 0.0;
        }
        return avg;
    }
    
    /**
     * The method to get the meanSubtracted2DArray
     * @param array
     * @return 
     */
    public static double[][] getMeanSubtracted2DArray(double[][] array) {
        // Get the average array
        double[] avg = getAverageArrayFrom2DArray(array);
        double[][] meanSubtractedArray = new double[array.length][array[0].length];
        // Subtract the avg column by column
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                meanSubtractedArray[i][j] = array[i][j] - avg[j];
            }
        }
        return meanSubtractedArray;
    }
}
