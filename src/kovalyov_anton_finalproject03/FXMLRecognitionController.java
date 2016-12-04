/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

/**
 * The controller for the Recognition GUI and recognizing a new face
 * @author Anton
 */
public class FXMLRecognitionController implements Initializable {
    
    // the FXML ImageView containing the image to be recognized
    @FXML
    private ImageView selectedImageView;
    // the FXML Text containing the name of the recognized person
    @FXML
    private Text tName;
    // the FXML ImageView containing the recognized image
    @FXML
    private ImageView recognizedImageView;
    // the FXML Text containing information about the recognition
    @FXML
    private Text tInfo;
    // the EigenDecomposition object used for recognition
    private EigenDecomposition ed;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }
    
    /**
     * The action triggered by clicking the Recognize button in the GUI
     */
    @FXML
    protected void recognize() {
        // Make sure the selected image is not null
        Image image = selectedImageView.getImage();
        if (image == null)
            return;
        // Attempt to do the recognition
        try {
            // Make sure the image is set to the right dimensions for recognition
            image = ImageOperations.scaleImageToSelectedSize(image, ed.getImageWidth(), ed.getImageHeight());
            // Get the image array as double values
            BufferedImage bfImage = ImageIo.toGray(ImageOperations.imageToBufferedImage(image));
            double[][] imageArray = new double[1][];
            byte[] imageByteArray = ImageIo.getGrayByteImageArray1DFromBufferedImage(bfImage);
            imageArray[0] = ArrayOperations.scale1DbyteTo1Ddouble(imageByteArray);
            // Normalize the image array (subtract the mean image from EigenDecomposition from it)
            for (int i = 0; i < imageArray[0].length; i++) {
                imageArray[0][i] = imageArray[0][i] - ed.getMeanImageArray()[i];
            }
            // Get the weights using the formula image matrix * eigenfaces transposed
            Algebra a = new Algebra();
            DenseDoubleMatrix2D imageMatrix = new DenseDoubleMatrix2D(imageArray);
            DoubleMatrix2D weightsMatrix = a.mult(imageMatrix, a.transpose(new DenseDoubleMatrix2D(ed.getEigenFaces())));
            double[][] weights = weightsMatrix.toArray();
            
            // Compute the euclidian distances of the current image from the images in the database
            double[] distances = new double[ed.getFaces().size()];
            for (int i = 0; i < distances.length; i++) {
                distances[i] = MathOperations.getEuclidianDistance(weights[0], ed.getFaces().get(i).getWeights());
            }
            
            // Get the minimum distance and compare it to threshold
            int minDistanceIndex = MathOperations.getMinIndexFromDoubleArray(distances);
            String closestImageLocation = ed.getFaces().get(minDistanceIndex).getLocation();
            if (distances[minDistanceIndex] > ed.getThreshold()) {
                tName.setText("Unknown");
                recognizedImageView.setImage(selectedImageView.getImage());
            }
            else {
                tName.setText(ed.getFaces().get(minDistanceIndex).getName());
                recognizedImageView.setImage(new Image("file:///" + ed.getFaces().get(minDistanceIndex).getLocation()));
            }
            tInfo.setText("Closest image location is " + closestImageLocation + 
                    ", distance is " + distances[minDistanceIndex] + ", threshold is " + ed.getThreshold());
        }
        // The eigen decomposition is not selected or the training set is corrupted
        catch (Exception ex) {
            recognizedImageView.setImage(null);
            tName.setText("");
            tInfo.setText("");
            Dialogs.showErrorAlert("Recognition not possible", "The training set is either not selected or corrupted");
        }
    }
    
    /**
     * the method to load a new image for recognition
     */
    @FXML
    protected void loadImage() {
        ImageChooser chooser = new ImageChooser();
        chooser.newImage();
        if (chooser.getImage() != null) {
            selectedImageView.setImage(chooser.getImage());
            recognizedImageView.setImage(null);
            tName.setText("");
            tInfo.setText("");
        }
    }

    /**
     * @return the ed
     */
    public EigenDecomposition getEd() {
        return ed;
    }

    /**
     * @param ed the ed to set
     */
    public void setEd(EigenDecomposition ed) {
        this.ed = ed;
    }

    /**
     * @return the tName
     */
    public Text gettName() {
        return tName;
    }

    /**
     * @param text
     */
    public void settName(String text) {
        this.tName.setText(text);
    }

    /**
     * @return the recognizedImageView
     */
    public ImageView getRecognizedImageView() {
        return recognizedImageView;
    }

    /**
     * @param image
     */
    public void setSelectedImage(Image image) {
        this.selectedImageView.setImage(image);
    }
    
    /**
     * @return the selectedImageView
     */
    public ImageView getSelectedImageView() {
        return selectedImageView;
    }
}
