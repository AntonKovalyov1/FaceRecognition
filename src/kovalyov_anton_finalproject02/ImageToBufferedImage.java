/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject02;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 *
 * @author Anton 
 */
public class ImageToBufferedImage {
    
    /**
     * This is the right way to convert an Image to a BufferedImage that I found
     * in http://stackoverflow.com/questions/30993681/how-to-make-a-javafx-image-crop-app
     * @param image
     * @return 
     */
    public static BufferedImage getBufferedImage(Image image) {
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(image, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), 
                bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);
        
        graphics.dispose();
        
        return bufImageRGB;
    }
}
