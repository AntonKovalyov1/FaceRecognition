/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.embed.swing.SwingFXUtils;
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
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;


/**
 * The controller for image operations with the addition of face detection
 * 
 * @author Anton (The methods startCamera, grabFrame and mat2image were taken 
 * from https://github.com/opencv-java/video-basics).
 * The new addition is face detection, I used http://opencv-java-tutorials.readthedocs.io/en/latest/06-face-detection-and-tracking.html
 * as reference.		
 */
public class FXMLCameraController {
    // the FXML button
    @FXML
    private Button button;
    // the FXML grayscale checkbox
    @FXML
    private CheckBox grayscale;
    // the FXML CheckBox for face detection
    @FXML
    private CheckBox faceDetection;
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
    @FXML
    private Button btRecognize;
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
    // face cascade classifier for loading the xml for face detection
    private final CascadeClassifier faceCascade = new CascadeClassifier();
    // absolute face size for face detection
    private int absoluteFaceSize = 0;
    // The list containing all of the detected faces for a current frame
    private final List<Image> faceDetectedImages = new ArrayList<>();
    
    /**
     * Initialize method, automatically called by @{link FXMLLoader}
     */
    public void initialize() {
        // load the classifier
        faceCascade.load("resources/haarcascades/haarcascade_frontalface_alt.xml");
        // set face detection
        faceDetection.setSelected(true);
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
            // clear the list of detected faces
            faceDetectedImages.clear();
        }
    }
    
    /**
     * The action triggered by pushing the Capture Image button on the GUI
     */
    @FXML
    protected void capture() {
        // show the image/images in hbCaptured depending whether face detection is selected
        if (faceDetection.isSelected()) {
            // Face detection allows to capture more than one face, display them
            for (int i = 0; i < faceDetectedImages.size(); i++) {
                ImageView capturedView = new ImageView(faceDetectedImages.get(i));
                // add the selection actions to each captured face
                setSelectionActions(capturedView);
                hbCaptured.getChildren().add(capturedView);
            }
        }
        // Display the frame without face detection
        else {
            ImageView capturedView = new ImageView(grabFrame());
            setSelectionActions(capturedView);
            hbCaptured.getChildren().add(capturedView);
        }
    }
    
    /**
     * The method to set selection actions to each captured image
     */
    private void setSelectionActions(ImageView capturedView) {
        capturedView.setFitHeight(210);
        capturedView.setPreserveRatio(true);
        capturedView.setEffect(innerShadow(Color.DODGERBLUE, 0));
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
                // Add the selected image to stack
                stack.add(capturedView.getImage());
                editableView.setImage(stack.peek());
                // Check if the grayscale checkbox was selected
                grayOrColor();
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
        int width, height;
        try {
            // atempt to get user input for the width and height of the image 
            // to be saved
            width = Integer.parseInt(tfWidth.getText());
            height = Integer.parseInt(tfHeight.getText());
            saveImageWithSelectedSize(width, height);
        }
        catch (Exception ex) {
            // save the image as 128 * 128 pixels if user input is bad
            saveImageWithSelectedSize(128, 128);
        }
    }
    
    /**
     * The method to scale and save an image
     * @param width
     * @param height
     */
    protected void saveImageWithSelectedSize(int width, int height) {
        Image scaledImage = ImageOperations.scaleImageToSelectedSize(editableView.getImage(), width, height);
        // convert the image to BufferedImage and use the ImageChooser class to save it
        BufferedImage bfImage = ImageOperations.imageToBufferedImage(scaledImage);
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
        editableView.setImage(stack.peek());
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
        // Show the cropped image on the editableView
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D( bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
        WritableImage wi = new WritableImage((int)bounds.getWidth(), (int)bounds.getHeight());
        wi = editableView.snapshot(parameters, wi);
        // It is important to push the color version of the image to stack for the undo crop functionality
        stack.push(wi);
        // Remove the rectangle created by the user from the group
        group.getChildren().remove(1);
        // Check if the grayscale checkbox was selected and display the image in gray or color
        grayOrColor();
    }
    
     /**
     * The action triggered by pushing the Undo Crop button on the GUI
     */
    @FXML
    protected void undoCrop() {
        if (stack.size() > 1) {
           stack.pop();
           grayOrColor();
        } 
    }
    
    /**
     * The method for setting the editableView image to gray or color
     */
    @FXML
    private void grayOrColor() {
        // The stack contains all of the color versions of the image cropped or not
        if (grayscale.isSelected()) {
            Image image = stack.peek();
            BufferedImage bf = ImageIo.toGray(ImageOperations.imageToBufferedImage(image));
            editableView.setImage(SwingFXUtils.toFXImage(bf, null));
        }
        else
            editableView.setImage(stack.peek());
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
                    if (faceDetection.isSelected()) {
                        detectAndDisplay(frame);
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
     * The method for face detection and displaying the image with one or more red
     * rectangles containing detected faces
     */
    private void detectAndDisplay(Mat frame) {
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();
        // convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);
        // compute minimum face size
        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.cols();
            if (Math.round(height * 0.15f) > 0) {
               this.absoluteFaceSize = Math.round(height * 0.15f);
            }
        }
        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                        new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
        // Get the rectangles
        Rect[] rect = faces.toArray();
        // Clear the face images list
        faceDetectedImages.clear();
        // Add the current frame face images to list
        for (int i = 0; i < rect.length; i++) {
            // The default rectangle is a square, by trial and error I tried to approximate it
            // to a more face like shape
            rect[i] = new Rect(rect[i].x + 20, rect[i].y, rect[i].width - 40, rect[i].height - 10);
            Imgproc.rectangle(frame, new Point(rect[i].x, rect[i].y), new Point(rect[i].x +
                    rect[i].width, rect[i].y + rect[i].height), new Scalar(0, 0, 255), 2);
            rect[i] = new Rect(rect[i].x + 2, rect[i].y + 2, rect[i].width - 4, rect[i].height - 4);
            Mat faceMat = new Mat(frame, rect[i]);
            // add each face image to the list
            faceDetectedImages.add(mat2Image(faceMat));
        }
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
        // encode the frame in the buffer, according to the JPG format
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

    /**
     * @return the btRecognize
     */
    public Button getBtRecognize() {
        return btRecognize;
    }

    /**
     * @param btRecognize the btRecognize to set
     */
    public void setBtRecognize(Button btRecognize) {
        this.btRecognize = btRecognize;
    }

    /**
     * @return the stack
     */
    public Stack<Image> getStack() {
        return stack;
    }
}