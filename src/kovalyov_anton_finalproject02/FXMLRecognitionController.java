/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject02;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 *
 * @author Anton
 */
public class FXMLRecognitionController implements Initializable {
    
    @FXML
    private ImageView selectedImageView;
    
    @FXML
    private Text tName;
    
    @FXML
    private ImageView recognizedImageView;
    
    @FXML
    private Text tInfo;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
    }

    private EigenDecomposition ed;
    /**
     * @return the selectedImageView
     */
    public ImageView getSelectedImageView() {
        return selectedImageView;
    }

    /**
     * @param selectedImageView the selectedImageView to set
     */
    public void setSelectedImageView(ImageView selectedImageView) {
        this.selectedImageView.setImage(selectedImageView.getImage());
    }
    
    @FXML
    protected void recognize() {
        // Make sure the selected image is not null
        Image image = selectedImageView.getImage();
        if (image == null)
            return;
        try {
            // Make sure the image to be recognized is scaled to the right sizes
            // using a temporary imageview and viewport snapshot
            int width = ed.getImageWidth(), height = ed.getImageHeight();
            ImageView temp = new ImageView(image);
            temp.setFitWidth(width);
            temp.setFitHeight(height);
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            parameters.setViewport(new Rectangle2D(0, 0, width, height));
            WritableImage wi = new WritableImage(width, height);
            temp.snapshot(parameters, wi);
            
            // Get the gray double1Darray from the image
            BufferedImage bfImage = ImageIo.toGray(ImageToBufferedImage.getBufferedImage(wi));
            double[][] imageArray = new double[1][];
            byte[] imageByteArray = ImageIo.getGrayByteImageArray1DFromBufferedImage(bfImage);
            imageArray[0] = ArrayOperations.scale1DbyteTo1Ddouble(imageByteArray);
            
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
            errorAlert("Recognition not possible", "The training set is either not selected or corrupted");
        }
    }
    
    @FXML
    protected void loadImage() {
        ImageChooser chooser = new ImageChooser();
        chooser.newImage();
        if (chooser.getImage() != null)
            selectedImageView.setImage(chooser.getImage());
    }
    
    private void errorAlert(String s1, String s2) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(s1);
        alert.setContentText(s2);

        alert.showAndWait();
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
     * @param recognizedImage
     */
    public void setRecognizedImageView(Image recognizedImage) {
        this.recognizedImageView.setImage(recognizedImage);
    }
}
