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
    private void recognize() {
        ImageView temp = new ImageView(selectedImageView.getImage());
        temp.setFitWidth(ed.getImageWidth());
        temp.setFitHeight(ed.getImageHeight());
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(0, 0, ed.getImageWidth(), ed.getImageHeight()));
        WritableImage wi = new WritableImage(ed.getImageWidth(), ed.getImageHeight());
        temp.snapshot(parameters, wi);
        
        BufferedImage bfImage = ImageIo.toGray(ImageToBufferedImage.getBufferedImage(wi));
        double[][] imageArray = new double[1][];
        byte[] imageByteArray = ImageIo.getGrayByteImageArray1DFromBufferedImage(bfImage);
        imageArray[0] = ArrayOperations.scale1DbyteTo1Ddouble(imageByteArray);

        Algebra a = new Algebra();
        DenseDoubleMatrix2D imageMatrix = new DenseDoubleMatrix2D(imageArray);
        DoubleMatrix2D weightsMatrix = a.mult(imageMatrix, a.transpose(new DenseDoubleMatrix2D(ed.getEigenFaces())));
        double[][] weights = weightsMatrix.toArray();
        double[] distances = new double[ed.getFaces().size()];
        
        for (int i = 0; i < distances.length; i++) {
            distances[i] = MathOperations.getEuclidianDistance(weights[0], ed.getFaces().get(i).getWeights());
        }
        int minDistanceIndex = MathOperations.getMinIndexFromDoubleArray(distances);
        System.out.println("Threshold " + ed.getThreshold() + ", image closest distance " + distances[minDistanceIndex]);

        if (distances[minDistanceIndex] > ed.getThreshold()) {
            tName.setText("Unknown");
            recognizedImageView.setImage(selectedImageView.getImage());
        }
        else {
            tName.setText(ed.getFaces().get(minDistanceIndex).getName());
            recognizedImageView.setImage(new Image("file:///" + ed.getFaces().get(minDistanceIndex).getLocation()));
        }
    }
    
    @FXML
    private void loadImage() {
        ImageChooser chooser = new ImageChooser();
        chooser.newImage();
        selectedImageView.setImage(chooser.getImage());
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
