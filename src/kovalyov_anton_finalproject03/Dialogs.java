/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

import javafx.scene.control.Alert;

/**
 * A simple class for showing a dialog with input parameters
 * @author Anton
 */
public class Dialogs {
    
    /**
     * The method that shows an error dialog
     * @param header
     * @param content
     */
    public static void showErrorAlert(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }
}
