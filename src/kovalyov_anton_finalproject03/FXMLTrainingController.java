/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 *
 * 
 * @author Anton
 */
public class FXMLTrainingController implements Initializable{
    // the FXML TextField for the number of eigenfaces input
    @FXML
    private TextField tfNumberOfEigenfaces;
    // the FXML TextField for the training directory location
    @FXML
    private TextField tfTrainDirectory;
    // the FXML VBox to display the eigenfaces of a given training set
    @FXML
    private VBox vbDisplayEigenFaces;
    // the FXML ListView of all created and saved training sets locations
    @FXML
    private ListView<String> lvTrainingLocations;
    // the FXML HBox that contains a delete button and info of a selected training set
    @FXML
    private HBox hbSelectedTraining;
    // the FXML Button that deletes a selected training set
    @FXML
    private Button btDeleteTraining;
    // the FXML Text that tells the number of images and eigenfaces a training set contains
    @FXML
    private Text tTrainingInfo;
    // the ObservableList containing the training sets locations.
    private ObservableList<String> trainingLocations = FXCollections.observableArrayList();
    // the EigenDecomposition serializable class contains all the math and necessary info
    // of a given training set
    private EigenDecomposition ed;
    
    /**
     * initialize method, automatically called by @{link FXMLLoader}
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // try loading trainingLocations.dat from the project folder. This File
        // contains the arrayList of training sets to be displayed in the ListView
        try {
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream("trainingLocations.dat")));
            ArrayList<String> list = (ArrayList)input.readObject();
            trainingLocations = FXCollections.observableArrayList(list);
            input.close();
        }
        catch (IOException | ClassNotFoundException ex) {
            // The training file doesn't exist or it's corrupted, thus lvTrainingLocations
            // will be empty and the hbSelectedTraining should not appear
            hbSelectedTraining.getChildren().clear();
        }
        // set the ObservableList trainingLocations into the ListView
        lvTrainingLocations.setItems(trainingLocations);
        // add listener for selected item in the ListView
        lvTrainingLocations.getSelectionModel().selectedItemProperty().addListener(ov -> {
            // Clear the already displayed eigenfaces and clear the saved EigenDecomposition
            vbDisplayEigenFaces.getChildren().clear();
            ed = new EigenDecomposition();
            // If the ListView is not empty load the selected training set
            if (!trainingLocations.isEmpty()) 
                loadTrainingSet(lvTrainingLocations.getSelectionModel().getSelectedItem());
            // If the listview is empty remove the HBox containing the delete button
            // and info of a selected training set
            else {
                hbSelectedTraining.getChildren().clear();
            }
        });
        
        // Select the first item in the listview to be displayed
        if (!trainingLocations.isEmpty()) {
            lvTrainingLocations.getSelectionModel().select(0);
        }
    }
    
    /**
     * The action triggered by pushing the save and train button on the GUI creates
     * a new training set, saves it into a dat file and displays it.
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.IOException
     */
    @FXML
    protected void trainAndSaveDirectory() throws ClassNotFoundException, IOException {
        // the number of eigenfaces to be created
        int n;
        // handle exceptions pertaining to user input
        try {    
            n = Integer.parseInt(tfNumberOfEigenfaces.getText());
        }
        // If n is not an integer set it to -1
        // The EigenDecomposition class takes any integer, if it's not valid it
        // will simply compute n eigenfaces from n images, as it is in this case
        catch (NumberFormatException ex) {
            n = -1;
        }
        // handle all the possible exceptions concerned with the input directory
        try {
            File file = new File(tfTrainDirectory.getText());
            String directory = file.getAbsolutePath();
            // try creating the eigen decomposition from the input directory
            ed = new EigenDecomposition();
            ed.run(directory, "jpg", n);
            // Save the resulting training set as a serializable object in a dat file
            // in the project folder, using the name of the input directory and
            // adding "_Trained.dat" to it
            String trainingSetLocation = FileOperations.getFileName(file.getName() + "_Trained", "dat");
            try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(trainingSetLocation, true)));) {
                output.writeObject(ed);
            }
            // Add the created training set to the observable list and display it
            trainingLocations.add(trainingSetLocation);
            lvTrainingLocations.getSelectionModel().select(trainingLocations.size() - 1);
        }
        catch (NullPointerException ex) {
            Dialogs.showErrorAlert("Bad directory", "Directory doesn't exist");
        }
        catch (IndexOutOfBoundsException ex) {
            Dialogs.showErrorAlert("Bad directory", "Directory contains no jpg images");
        }
        catch (IllegalArgumentException ex) {
            Dialogs.showErrorAlert("Bad directory", "All images in the directory must be of equal size");
        }
    }
    
    /**
     * the method to load a selected training set from the ListView
     */
    private void loadTrainingSet(String location) {
        // Deserialize the EigenDecomposition object
        try {
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(location)));
            ed = (EigenDecomposition)input.readObject();
            // display the eigenfaces in the VBox
            displayEigenFaces();
            // display the info of the loaded training set
            tTrainingInfo.setText("Training set number of images: " + 
                    ed.getFaces().size() + ", number of eigenfaces: " + 
                    ed.getEigenFaces().length + ", the preset threshold: " +
                    ed.getThreshold() + ".");
            // make sure the HBox hbSelectedTraining appears in the GUI
            if (hbSelectedTraining.getChildren().isEmpty())
                hbSelectedTraining.getChildren().addAll(btDeleteTraining, tTrainingInfo);
            // close the ObjectInputStream
            input.close();
        }
        catch (IOException | ClassNotFoundException ex) {
            // File corrupted or doesn't exist.
            tTrainingInfo.setText("Training doesn't exist or it's corrupted.");
        }
    }
    /**
     * the method to display the eigenfaces in the VBox
     */
    private void displayEigenFaces() {
        // Get the EigenFaces scaled to 0-255 byte matrix from the deserialized
        // EigenDecomposition object and display it in the VBox
        for (int i = 0; i < ed.getEigenFacesScaled().length; i++) {
            BufferedImage bfImage = ImageIo.setGrayByteImageArray1DToBufferedImage(ed.getEigenFacesScaled()[i], ed.getImageWidth(), ed.getImageHeight());
            ImageView viewEigenfaces = new ImageView(SwingFXUtils.toFXImage(bfImage, null));
            viewEigenfaces.setFitWidth(300);
            viewEigenfaces.setPreserveRatio(true);
            vbDisplayEigenFaces.getChildren().add(viewEigenfaces);
        }
    }
    
    /**
     * The action triggered by pushing the delete button in the GUI
     */
    @FXML
    private void deleteTrainingSet() {
        // Make sure the ListView contains training sets
        if (!lvTrainingLocations.getSelectionModel().isEmpty()) {
            // Delete the file containing the serialized EigenDecomposition object
            try {
                int selectedIndex = lvTrainingLocations.getSelectionModel().getSelectedIndex();
                File file = new File(trainingLocations.get(selectedIndex));
                file.delete();
                // remove the selected training set from the ObservableList and ListView
                trainingLocations.remove(selectedIndex);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * the method to return an ArrayList of training sets locations to save them 
     * later on when the user exits the GUI
     */
    public ArrayList<String> getListOfTrainingLocations() {
        // Simply get an ArrayList from the ObservableList using iteration
        ArrayList<String> list = new ArrayList<>();
        
        for (int i = 0; i < trainingLocations.size(); i++) {
            list.add(trainingLocations.get(i));
        }
        
        return list;
    }

    /**
     * @return the ed
     * This returns the object containing all the needed info for the Face Recognition tab
     */
    public EigenDecomposition getEd() {
        return ed;
    }
}
