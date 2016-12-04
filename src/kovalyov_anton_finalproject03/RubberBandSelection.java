/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kovalyov_anton_finalproject03;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 *
 * @author Anton (using http://stackoverflow.com/questions/30993681/how-to-make-a-javafx-image-crop-app)
 */
public class RubberBandSelection {

    private static DragContext dragContext = new DragContext();

    public static void addRubberBandSelection(Group group, int boundX, int BoundY) {

        Rectangle rect = new Rectangle(0, 0, 0, 0);
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.6));

        group.setOnMousePressed(event -> {
            if( event.isSecondaryButtonDown())
                return;

            // remove old rect
            rect.setX(0);
            rect.setY(0);
            rect.setWidth(0);
            rect.setHeight(0);

            group.getChildren().remove(rect);

            // prepare new drag operation
            dragContext.mouseAnchorX = event.getX();
            dragContext.mouseAnchorY = event.getY();

            rect.setX(dragContext.mouseAnchorX);
            rect.setY(dragContext.mouseAnchorY);
            rect.setWidth(0);
            rect.setHeight(0);
            
            // add new rect
            group.getChildren().add(rect);
        });

        group.setOnMouseDragged(event -> {
            if( event.isSecondaryButtonDown())
                return;

            double offsetX = event.getX() - dragContext.mouseAnchorX;
            double offsetY = event.getY() - dragContext.mouseAnchorY;
            
            // Make sure the rectangle doesn't go out of the desired bounds
            if (offsetX > 0) {
                rect.setX(dragContext.mouseAnchorX);
                if (event.getX() <= 599) {
                    rect.setWidth(offsetX);
                }
                else
                    rect.setWidth(599 - dragContext.mouseAnchorX);
            }
            else {
                rect.setX(event.getX());
                if (event.getX() >= 1) {
                    rect.setWidth(dragContext.mouseAnchorX - event.getX());
                }
                else {
                    rect.setWidth(dragContext.mouseAnchorX - 1);
                    rect.setX(1);
                }
            }
            
            if (offsetY >= 1) {
                rect.setY(dragContext.mouseAnchorY);
                if (event.getY() <= 449) {
                    rect.setHeight(offsetY);
                }
                else
                    rect.setHeight(449 - dragContext.mouseAnchorY);
            }
            else {
                rect.setY(event.getY());
                if (event.getY() >= 1) {
                    rect.setHeight(dragContext.mouseAnchorY - event.getY());
                }
                else {
                    rect.setHeight(dragContext.mouseAnchorY - 1);
                    rect.setY(1);
                }
            }
        });
    }

    private static class DragContext {
        public double mouseAnchorX;
        public double mouseAnchorY;
    }
}
