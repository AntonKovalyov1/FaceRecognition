/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject02;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author Anton
 */
public class FinalProjectGUI extends Stage {
    
    public FinalProjectGUI() {
        initialize();
    }
    
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
            // Get the training controller
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
            
            tp.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
                if (newTab == tabRecog) {
                    recogController.setSelectedImageView(cameraController.getEditableView());
                    recogController.setEd(trainingController.getEd());
                    recogController.setRecognizedImageView(null);
                    recogController.settName("");
                }
            });
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
