/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject02;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;


/**
 * The controller associated with the only view of our application. The
 * application logic is implemented here. It handles the button for
 * starting/stopping the camera, the acquired video stream, the relative
 * controls and the histogram creation.
 * 
 * @author Anton (The methods startCamera, grabFrame and mat2image were taken 
 * from https://github.com/opencv-java/video-basics)
 * @version 1.1 (2015-10-20)
 * @since 1.0 (2013-11-20)
 * 		
 */
public class FXMLCameraController {
    // the FXML button
    @FXML
    private Button button;
    // the FXML grayscale checkbox
    @FXML
    private CheckBox grayscale;
    // the FXML HBox for showing the captured images
    @FXML
    private HBox hbCaptured;
    // the FXML HBox for the video and selected image
    @FXML
    private HBox hbMain;
    // the FXML ImageView for the video
    @FXML
    private ImageView currentFrame;
    // the FXML ImageView to show the editable image
    @FXML
    private ImageView editableView;
    // the FXML HBox for the selected image to edit and all buttons
    @FXML
    private HBox hbSelected;
    // the FXML Group containing the editable ImageView and crop rectangle
    @FXML
    private Group group;
    // the FXML TextField to set the width of the image to save
    @FXML
    private TextField tfWidth;
    // the FXML TextField to set the height of the image to save
    @FXML
    private TextField tfHeight;
    // the timer for the video
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture;
    // a flag to change the button behavior
    private boolean cameraActive;
    // the ImageView to save the image that has been selected from the captured 
    // images
    private ImageView selectedView = new ImageView();
    // A stack containing the editable image and all the cropped versions of it
    private final Stack<Image> stack = new Stack<>();
    
    /**
     * Initialize method, automatically called by @{link FXMLLoader}
     */
    public void initialize() {
        // Initialize camera tab
        capture = new VideoCapture();
        cameraActive = false;
        hbMain.getChildren().remove(hbSelected);
        // Add the rectangle for cropping to the Group (activated by pressing 
        // and dragging)
        RubberBandSelection.addRubberBandSelection(group, 599, 449);
    }

    /**
     * The action triggered by pushing the start camera button on the GUI
     */
    @FXML
    protected void startCamera() {		
        if (!this.cameraActive) {
            // start the video capture
            this.capture.open(0);

            // is the video stream available?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        Image imageToShow = grabFrame();
                        currentFrame.setImage(imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.button.setText("Stop Camera");
            }
            else {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        }
        else {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.button.setText("Start Camera");

            // stop the timer
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }

            catch (InterruptedException e) {
                // log the exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }

            // release the camera
            this.capture.release();
            // clean the frame
            currentFrame.setImage(null);
        }
    }
    
    /**
     * The action triggered by pushing the Capture Image button on the GUI
     */
    @FXML
    protected void capture() {
        // capture and show the image in hbCaptured
        ImageView capturedView = new ImageView(grabFrame());
        capturedView.setFitHeight(210);
        capturedView.setPreserveRatio(true);
        capturedView.setEffect(innerShadow(Color.DODGERBLUE, 0));
        hbCaptured.getChildren().add(capturedView);
        // set mouse clicked action to highlight the selected image and show it 
        // in hbSelected
        capturedView.setOnMouseClicked(e -> {
            // clear the stack that might contain previously saved images
            stack.clear();
            // Make sure the image is not already selected
            if (selectedView != capturedView) {
                if (selectedView.getImage() != null) {
                    selectedView.setEffect(innerShadow(Color.DODGERBLUE, 0));
                }
                capturedView.setEffect(innerShadow(Color.DODGERBLUE, 20));
                selectedView = capturedView;
                
                editableView.setImage(capturedView.getImage());
                if (!hbMain.getChildren().contains(hbSelected)) {
                    hbMain.getChildren().add(hbSelected);
                }
            }
            // If it's an already selected image unselect it and remove the 
            // hbSelected HBox
            else {
                if (hbMain.getChildren().contains(hbSelected)) {
                    hbMain.getChildren().remove(hbSelected);
                }
                
                capturedView.setEffect(innerShadow(null, 0));
                selectedView = new ImageView();
            }
        });
        
        // Highlight a captured image when the mouse enters it
        capturedView.setOnMouseEntered(e -> {
            InnerShadow innerShadow = (InnerShadow)capturedView.getEffect();
            if (innerShadow.getRadius() != 20) {
                capturedView.setEffect(innerShadow(Color.DODGERBLUE, 15));
            }
        });
        
        // Remove highlight from a captured image when the mouse exits it
        capturedView.setOnMouseExited(e -> {
            InnerShadow innerShadow = (InnerShadow)capturedView.getEffect();
            if (innerShadow.getRadius() != 20) {
                capturedView.setEffect(innerShadow(null, 0));
            }
        });
    }
    
    /**
     * Create the highlights for the selected image
     * 
     * @param color
     * @param radius
     * 
     * @return the {@link InnerShadow} to show
     */
    protected InnerShadow innerShadow(Color color, double radius) {
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setColor(color);
        innerShadow.setRadius(radius);
        return innerShadow;
    }
    
    /**
     * The action triggered by pushing the Clear All button on the GUI
     */
    @FXML
    protected void clearAllImages() {
        hbCaptured.getChildren().clear();
    }
    
    /**
     * The action triggered by pushing the Save Image button on the GUI
     */
    @FXML
    protected void saveImage() {
        ImageView temp = new ImageView(editableView.getImage());
        int width, height;
        try {
            // ayyempt to get user input for the width and height of the image 
            // to be saved
            width = Integer.parseInt(tfWidth.getText());
            height = Integer.parseInt(tfHeight.getText());
            saveImageWithSelectedSize(temp, width, height);
        }
        catch (Exception ex) {
            // save the image as 128 * 128 pixels if user input is bad
            saveImageWithSelectedSize(temp, 128, 128);
        }
    }
    
    protected void saveImageWithSelectedSize(ImageView temp, int width, int height) {
        temp.setFitWidth(width);
        temp.setFitHeight(height);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(0, 0, width, height));
        WritableImage wi = new WritableImage(width, height);
        temp.snapshot(parameters, wi);
        // convert the image to BufferedImage and use the ImageChooser class to 
        // save it
        BufferedImage bfImage = ImageToBufferedImage.getBufferedImage(wi);
        ImageChooser chooser = new ImageChooser();
        chooser.saveImage(bfImage);
    }
    
    /**
     * The action triggered by pushing the Delete Image button on the GUI
     */
    @FXML
    protected void deleteImage() {
        editableView.setImage(null);
        hbCaptured.getChildren().remove(selectedView);
        selectedView.setImage(null);
        hbMain.getChildren().remove(hbSelected);
    }
    
    /**
     * The action triggered by releasing the left mouse button on the Group 
     * containing the editableView
     */
    @FXML
    protected void showCroppedImage() {
        // get the created rectangle by the user used to crop the image
        Rectangle bounds;
        try {
            bounds = (Rectangle)group.getChildren().get(1);
        }
        catch(IndexOutOfBoundsException ex) {
            return;
        }
        // Make sure that the user didn't just click on the image creating an 
        // empty rectangle
        if (bounds.getWidth() == 0 || bounds.getHeight() == 0) {
           group.getChildren().remove(1);
           return;
        }
        stack.add(editableView.getImage());
        // Show the cropped image on the editableView
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D( bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
        WritableImage wi = new WritableImage((int)bounds.getWidth(), (int)bounds.getHeight());
        editableView.snapshot(parameters, wi);
        editableView.setImage(wi);
        // Remove the rectangle created by the user from the group
        group.getChildren().remove(1);
    }
     /**
     * The action triggered by pushing the Undo Crop button on the GUI
     */
    @FXML
    protected void undoCrop() {
        if (!stack.isEmpty()) {
           editableView.setImage(stack.pop());
        } 
    }

    /**
     * Get a frame from the opened video stream (if any)
     * 
     * @return the {@link Image} to show
     */
    private Image grabFrame() {
        // init everything
        Image imageToShow = null;
        Mat frame = new Mat();

        // check if the capture is open
        if (this.capture.isOpened()) {
            try {
                // read the current frame
                this.capture.read(frame);

                // if the frame is not empty, process it
                if (!frame.empty()) {
                    if (grayscale.isSelected()) {
                        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    }

                    // convert the Mat object (OpenCV) to Image (JavaFX)
                    imageToShow = mat2Image(frame);
                }
            }
            catch (Exception e) {
                // log the error
                System.err.println("Exception during the frame elaboration: " + e);
            }
        }
        return imageToShow;
    }

    /**
     * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
     * 
     * @param frame
     *            the {@link Mat} representing the current frame
     * @return the {@link Image} to show
     */
    private Image mat2Image(Mat frame) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".jpg", frame, buffer);
        // build and return an Image created from the image encoded in the buffer
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    /**
     * @return the cameraActive
     */
    public boolean isCameraActive() {
        return cameraActive;
    }

    /**
     * @param cameraActive the cameraActive to set
     */
    public void setCameraActive(boolean cameraActive) {
        this.cameraActive = cameraActive;
    }

    /**
     * @return the editableView
     */
    public ImageView getEditableView() {
        return editableView;
    }
}