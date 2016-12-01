/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject02;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;
import org.opencv.core.Core;

/**
 * The main class to load the project 2 GUI
 * @author Anton
 */
public class Kovalyov_Anton_FinalProject02 extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage = new FinalProjectGUI();
    }

    public static void main(String[] args) {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        launch(args);
    }
}
