/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 *
 * @author Anton
 */
public class ImageOperations {
    
    /**
     * This is the right way to convert an Image to a BufferedImage that I found
     * in http://stackoverflow.com/questions/30993681/how-to-make-a-javafx-image-crop-app
     * @param image
     * @return
     */
    public static BufferedImage imageToBufferedImage(Image image) {
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(image, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), 
                bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);
        
        graphics.dispose();
        
        return bufImageRGB;
    }
    
        public static Image scaleImageToSelectedSize(Image image, int width, int height) {
        ImageView temp = new ImageView(image);
        temp.setFitWidth(width);
        temp.setFitHeight(height);
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(0, 0, width, height));
        WritableImage wi = new WritableImage(width, height);
        wi = temp.snapshot(parameters, wi);
        // convert the image to BufferedImage and use the ImageChooser class to 
        // save it
        return wi;
    }
}
