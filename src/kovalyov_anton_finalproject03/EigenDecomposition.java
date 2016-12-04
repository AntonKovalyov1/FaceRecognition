/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import java.awt.image.BufferedImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that creates an object containing the matrix of eigenfaces (double values),
 * the eigenFacesScaled (byte values, used to display eigenfaces as images), the
 * faces list (contains the location, name and weights of each image in the used 
 * directory) and finally all the images width and height (the dimensions must be equal
 * for each image from the used directory).
 * @author Anton
 */
public class EigenDecomposition implements java.io.Serializable {
    // the list of faces from the Face class
    private List<Face> faces = new ArrayList<>();
    // the double2DArray of eigenfaces used to get the weights
    private double[][] eigenfaces;
    // the byte2DArray used to display the eigenfaces as images
    private byte[][] eigenfacesScaled;
    // the width of the images used
    private int imageWidth;
    // the height of the images used
    private int imageHeight;
    // the threshold for face recognition
    private double threshold;
    // the mean image double array from all the images
    private double[] meanImageArray;

    public EigenDecomposition() {
        
    }
    
    /**
     * the method that does all the math required
     * @param directory the location of the images directory
     * @param type the type of images to use (jpg)
     * @param n is the number of eigenfaces
     */
    public void run(String directory, String type,  int n) {
        // the list of files of the given type in the directory that are retrieved
        // using the FileOperations class
        List<File> files = new ArrayList<>();
        FileOperations.getFilesRecursively(directory, type, files);
        // Make sure n is not less than one or more than the number of images, if
        // it is set it to the number of images
        if (n < 1 || n > files.size())
            n = files.size();
        // Get a sample BufferedImage from the images directory to know the images dimensions
        BufferedImage bfSample = ImageIo.readImage(files.get(0).getPath());
        imageWidth = bfSample.getWidth();
        imageHeight = bfSample.getHeight();
        // Create the byte2DArray containing byteArrays of each image as rows
        int imageSize = (int)(imageWidth * imageHeight);
        byte[][] byteArray = new byte[files.size()][imageSize];
        for (int i = 0; i < files.size(); i++) {
            byteArray[i] = ImageIo.getGrayByteImageArray1DFromBufferedImage(ImageIo.toGray(ImageIo.readImage(files.get(i).getPath())));
        }
        // Get the double2DArray using the ArrayOperations class
        double[][] doubleArray = ArrayOperations.scale2DbyteTo2Ddouble(byteArray);
        // get the mean image array
        meanImageArray = ArrayOperations.getAverageArrayFrom2DArray(doubleArray);
        // Get the meanSubtractedArray using the ArrayOperations class
        double[][] meanSubtractedArray = ArrayOperations.getMeanSubtracted2DArray(doubleArray);
        // Finally everything is ready for Eigen Decomposition using the Colt library
        Algebra a = new Algebra();
        DoubleMatrix2D correlationMatrix = a.mult(new DenseDoubleMatrix2D(meanSubtractedArray), a.transpose(new DenseDoubleMatrix2D(meanSubtractedArray))); 
        DoubleMatrix2D imagesMatrix = new DenseDoubleMatrix2D(meanSubtractedArray);
        EigenvalueDecomposition eigenDecomp = new EigenvalueDecomposition(correlationMatrix);
	DoubleMatrix2D eigenVectorMatrix = eigenDecomp.getV();
	DoubleMatrix1D eigenValues = eigenDecomp.getRealEigenvalues();
        // Get the top n eigen vectors according to the highest eigen values
        double[][] topNEigenvectors = getTopNEigenvectors(n, eigenVectorMatrix, eigenValues);
        // Create the Eigenfaces matrix used for face recognition
        DoubleMatrix2D eigenfacesMatrix = a.mult(a.transpose(new DenseDoubleMatrix2D(topNEigenvectors)), imagesMatrix);
        eigenfaces = eigenfacesMatrix.toArray();
        // Create the scaled Eigenfaces array
        eigenfacesScaled = ArrayOperations.scale2DdoubleTo2Dbyte(eigenfaces);
        // Get the weights 2d array where each row is of size n and it corresponds
        // to an image from the used directory
        DoubleMatrix2D weightsMatrix = a.mult(new DenseDoubleMatrix2D(meanSubtractedArray), a.transpose(eigenfacesMatrix));
        double[][] weights2DArray = weightsMatrix.toArray();
        // And the last step is to populate the faces list with faces objects that
        // contain the names, locations and weights of each image
        for (int i = 0; i < doubleArray.length; i++) {
            // Instantiate the Face class and set its location, name and corresponding
            // weights for each image
            Face face = new Face();
            face.setLocation(files.get(i).getPath());
            face.setName(files.get(i).getParentFile().getName());
            face.setWeights(weights2DArray[i]);
            faces.add(face);
        }
        // Compute the threshold
        computeThreshold();
    }
    
    /**
     * the method to get the top Eigenvectors according to the highest Eigenvalues
     */
    private double[][] getTopNEigenvectors(int n, DoubleMatrix2D eigenVectorMatrix, DoubleMatrix1D eigenValues) {
        // Convert matrices to arrays
        double[] values = eigenValues.toArray();
        double[][] vectors = eigenVectorMatrix.toArray();
        // instantiate the output array of top n vectors to be sorted in decreasing order
        double[][] nVectors = new double[vectors.length][n];
        // In this algorithm gets the maximum values from the values array,
        // then adds the corresponding vector to the nVectors array for n times
        for (int i = 0; i < n; i++) {
            double max = values[i];
            int indexOfMax = i;
            // Disregard i indices after each iteration
            for (int j = i; j < values.length; j++) {
                if (max < values[j]) {
                    max = values[j];
                    indexOfMax = j;
                }    
            }
            // Once the index of the max value is known add the corresponding vector
            // to the nVectors array as a column and update that column in the vectors
            // array as the i column to avoid repetitions
            for (int k = 0; k < nVectors.length; k++) {
                nVectors[k][i] = vectors[k][indexOfMax];
                vectors[k][indexOfMax] = vectors[k][i];
            }
            // Get rid of the max value in the values array for the next iteration,
            // if the index of max value is the same as the starting one in the i
            // iteration it will simply be disregarded by the second j loop
            values[indexOfMax] = values[i];
        }
        return nVectors;
    }
    /**
     * the method to compute the threshold, which is 0.6 times the maximum of the 
     * minimum euclidian distance of each image in the database from the rest
     */
    private void computeThreshold() {
        // The 2D array where each row is an image's euclidian distances from the rest of images
        double[][] distances = new double[faces.size()][faces.size() - 1];
        // Compute the distances in an efficient way (compute distance x to y and
        // fill it's value for both x to y and y to x distances
        for (int i = 0; i < distances.length; i++) {
            for (int j = i; j < distances[0].length; j++) {
                distances[i][j] = MathOperations.getEuclidianDistance(faces.get(i).getWeights(), faces.get(j + 1).getWeights());
                distances[j + 1][i] = distances[i][j];
            }
        }
        // Get the minimum euclidian distance for each image in the database
        double[] minEuclidianDistances = new double[faces.size()];
        for (int i = 0; i < minEuclidianDistances.length; i++) {
            minEuclidianDistances[i] = MathOperations.getMinFromDoubleArray(distances[i]);
        }
        // Calculate the threshold as 0.6 times the maximum of minimum euclidian distances
        threshold = 0.6 * MathOperations.getMaxFromDoubleArray(minEuclidianDistances);
    }

    /**
     * @return the faces
     */
    public List<Face> getFaces() {
        return faces;
    }

    /**
     * @param Faces the faces to set
     */
    public void setFaces(List<Face> Faces) {
        this.faces = Faces;
    }

    /**
     * @return the eigenFaces
     */
    public double[][] getEigenFaces() {
        return eigenfaces;
    }

    /**
     * @param eigenFaces the eigenFaces to set
     */
    public void setEigenFaces(double[][] eigenFaces) {
        this.eigenfaces = eigenFaces;
    }

    /**
     * @return the eigenfacesScaled
     */
    public byte[][] getEigenFacesScaled() {
        return eigenfacesScaled;
    }

    /**
     * @param eigenFacesScaled the EigenFacesScaled to set
     */
    public void setEigenFacesScaled(byte[][] eigenFacesScaled) {
        this.eigenfacesScaled = eigenFacesScaled;
    }

    /**
     * @return the imageWidth
     */
    public int getImageWidth() {
        return imageWidth;
    }

    /**
     * @param imageWidth the imageWidth to set
     */
    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    /**
     * @return the imageHeight
     */
    public int getImageHeight() {
        return imageHeight;
    }

    /**
     * @param imageHeight the imageHeight to set
     */
    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    /**
     * @param threshold the threshold to set
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * @return the threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * @return the meanImageArray
     */
    public double[] getMeanImageArray() {
        return meanImageArray;
    }

    /**
     * @param meanImageArray the meanImageArray to set
     */
    public void setMeanImageArray(double[] meanImageArray) {
        this.meanImageArray = meanImageArray;
    }
}
