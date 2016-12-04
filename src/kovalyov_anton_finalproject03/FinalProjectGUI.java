/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The main GUI that connects all tabs
 * 
 * @author Anton
 */
public class FinalProjectGUI extends Stage {
    
    public FinalProjectGUI() {
        initialize();
    }
    
    /**
     * Initialize the GUI with the three FXML nodes
     */
    private void initialize() {
        try {
            // Get the camera tab
            FXMLLoader loaderCamera = new FXMLLoader(getClass().getResource("FXMLCameraDocument.fxml"));
            BorderPane bpCamera = (BorderPane)loaderCamera.load();
            Tab tabCamera = new Tab("Camera");
            tabCamera.setClosable(false);
            tabCamera.setContent(bpCamera);
            // Get the camera controller
            FXMLCameraController cameraController = loaderCamera.getController();
            // Get the training tab
            FXMLLoader loaderTraining = new FXMLLoader(getClass().getResource("FXMLTrainingDocument.fxml"));
            BorderPane bpTraining = (BorderPane)loaderTraining.load();
            Tab tabTraining = new Tab("Training");
            tabTraining.setClosable(false);
            tabTraining.setContent(bpTraining);
            // Get the training controller
            FXMLTrainingController trainingController = loaderTraining.getController();
            // Get the recognition tab
            FXMLLoader loaderRecog = new FXMLLoader(getClass().getResource("FXMLRecognitionDocument.fxml"));
            BorderPane bpRecog = (BorderPane)loaderRecog.load();
            Tab tabRecog = new Tab("Recognition");
            tabRecog.setClosable(false);
            tabRecog.setContent(bpRecog);
            // Get the recognition controller
            FXMLRecognitionController recogController = loaderRecog.getController();
            // Save the new list of training locations once the user closes the stage
            // and make sure the camera is off using the camera and training controllers
            setOnCloseRequest(e -> {
                // Save the possibly updated training locations
                try {
                    new FileOutputStream("trainingLocations.dat").close();
                    ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("trainingLocations.dat", true)));
                    output.writeObject(trainingController.getListOfTrainingLocations());
                    output.close();
                } 
                catch (IOException ex) {
                    ex.printStackTrace();
                }
                // Make sure the camera is off
                if (cameraController.isCameraActive())
                    cameraController.startCamera();
                // It sounds suspicious but this actually turns off the camera if
                // it is on
            });        
            close();

            // Add tabs to a TabPane and display
            TabPane tp = new TabPane();
            tp.getTabs().addAll(tabCamera, tabTraining, tabRecog);
            Scene scene = new Scene(tp);
            setTitle("Face Recognition with Java FX");
            setScene(scene);
            setMaximized(true);
            show();
            // Get the recognize button from the camera controller to connect it
            // with the recognize button in the Recognition controller
            Button btRecognize = cameraController.getBtRecognize();
            // Make sure the selected EigenDecomposition object from the training
            // tab is used by the recognition tab
            SingleSelectionModel<Tab> selectionModel = tp.getSelectionModel();
            tabRecog.selectedProperty().addListener(e -> {
                recogController.setEd(trainingController.getEd());
            });
            // set action on the recognize button from the camera controller
            btRecognize.setOnAction(e -> {
                // send the image from editableView in the camera controller to the recognition tab
                Image image;
                try {
                    image = cameraController.getEditableView().getImage();
                    // get the according width and height from the EigenDecomposition
                    int width = trainingController.getEd().getImageWidth();
                    int height = trainingController.getEd().getImageHeight();
                    // scale the image and temporarly save it, saving it and loading it back
                    // slightly improves recognition, since the images in the database
                    // underwent the same process
                    image = ImageOperations.scaleImageToSelectedSize(image, width, height);
                    File imageFile = new File(FileOperations.getFileName("temp", "jpg"));
                    ImageIo.writeImage(ImageOperations.imageToBufferedImage(image), "JPG", imageFile.getPath());
                    image = new Image(imageFile.toURI().toString());
                    // set the image in the recognition view and recognize the face
                    recogController.setSelectedImage(image);
                    selectionModel.select(2);
                    recogController.recognize();
                    // delete the temp jpg file
                    imageFile.delete();
                }
                // The training set is not used or it's corrupted
                catch (Exception ex) {
                    selectionModel.select(2);
                    Dialogs.showErrorAlert("Recognition not possible", "The training set is either not selected or corrupted");
                }
            });
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
