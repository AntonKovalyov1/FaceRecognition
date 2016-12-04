/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

/**
 *
 * @author Anton
 */
public class ImageChooser {
    
    private String fileName;
    private Image image;
    private BufferedImage bfImage;
            
    public ImageChooser() {
        
    }
    
    public void newImage() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("View Pictures");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home")));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png"));

        //Handle the queryImage
        File file = fileChooser.showOpenDialog(null);
        if (file != null && file.isFile()) {
            try {
                image = new Image(file.toURI().toURL().toExternalForm());
                fileName = file.getName();
                setBfImage(ImageIo.readImage(file.getPath()));
            } 
            catch (Exception ex) {

            }
        }
    }
    
    public void saveImage(BufferedImage bfImage) {
        final FileChooser filechooser = new FileChooser();
        filechooser.setTitle("Save Image");
        filechooser.setInitialDirectory(new File(System.getProperty("user.home")));
        filechooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JPG", "*.jpg"), new FileChooser.ExtensionFilter("PNG", "*.png"));
        File saveFile = filechooser.showSaveDialog(null);
        if (saveFile != null) {
            try {
                ImageIo.writeImage(bfImage, "jpg" , saveFile.getPath());
            } 
            catch (Exception ex) {
                System.out.println("Image not saved.");
            }
        }
    }

    /**
     * @return the image
     */
    public Image getImage() {
        return image;
    }

    /**
     * @param image the image to set
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * @return the bfImage
     */
    public BufferedImage getBfImage() {
        return bfImage;
    }

    /**
     * @param bfImage the bfImage to set
     */
    public void setBfImage(BufferedImage bfImage) {
        this.bfImage = bfImage;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }
}
