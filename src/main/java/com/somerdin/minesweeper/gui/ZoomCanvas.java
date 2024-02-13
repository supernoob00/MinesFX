package com.somerdin.minesweeper.gui;

import com.github.weisj.jsvg.nodes.Rect;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.util.*;

public class ZoomCanvas extends Canvas {
    // the size ratio of the updated zoom bounds to old bounds whenever a scroll event occurs
    public static final double ZOOM_RATIO = 0.9;

    private final Rectangle zoomBounds = new Rectangle(0, 0, getWidth(), getHeight());
    private final Rectangle zoomArea = new Rectangle(0, 0, getWidth(), getHeight());

    // TODO: scale should only depend on x or y coordinate ratio
    private final SimpleDoubleProperty zoomScale = new SimpleDoubleProperty(1);
    private final SimpleBooleanProperty redrawPendingProperty = new SimpleBooleanProperty();

    private double lastMouseX;
    private double lastMouseY;

    public ZoomCanvas(double initialWidth,
                      double initialHeight,
                      Rectangle initialZoomBounds,
                      Rectangle initialZoomArea) {
        if (!initialZoomBounds.contains(initialZoomArea.getX(), initialZoomArea.getY())) {
            throw new IllegalArgumentException("Zoom area must be within zoom bounds.");
        }
    }

    public ZoomCanvas(double initialWidth, double initialHeight) {
        widthProperty().addListener((observable, oldValue, newValue) -> {
            double difference = newValue.doubleValue() - oldValue.doubleValue();
            zoomArea.widthProperty().set(zoomArea.getX() + difference);
        });
        heightProperty().addListener((observable, oldValue, newValue) -> {
            double difference = newValue.doubleValue() - oldValue.doubleValue();
            zoomArea.heightProperty().set(zoomArea.getY() + difference);
        });

        zoomScale.bind(Bindings.divide(zoomBounds.widthProperty(), zoomArea.widthProperty()));

        registerScrollListener();
        registerDragListener();

        resize(initialWidth, initialHeight);
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double maxHeight(double width) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double maxWidth(double height) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double minWidth(double height) {
        return 1D;
    }

    @Override
    public double minHeight(double width) {
        return 1D;
    }

    @Override
    public void resize(double width, double height) {
        this.setWidth(width);
        this.setHeight(height);
    }

    public Rectangle getZoomBounds() {
        return zoomBounds;
    }

    public Rectangle getZoomArea() {
        return zoomArea;
    }

    public DoubleProperty zoomScaleProperty() {
        return zoomScale;
    }

    public double getZoomScale() {
        return zoomScale.get();
    }

    public BooleanProperty redrawPendingProperty() {
        return redrawPendingProperty;
    }

    public double getZoomBoundsX() {
        return zoomBounds.getX();
    }

    public double getZoomBoundsY() {
        return zoomBounds.getY();
    }

    public double getZoomBoundsWidth() {
        return zoomBounds.getWidth();
    }

    public double getZoomBoundsHeight() {
        return zoomBounds.getHeight();
    }

    // TODO: change method name for this
    public double getAdjustedX(double x) {
        double widthRatio = zoomBounds.getWidth() / zoomArea.getWidth();
        return (x - zoomArea.getX()) * widthRatio + zoomBounds.getX();
    }

    public double getAdjustedY(double y) {
        double heightRatio = zoomBounds.getHeight() / zoomArea.getHeight();
        return (y - zoomArea.getY()) * heightRatio + zoomBounds.getY();
    }

    public double zoomToNonZoomX(double zx) {
        return (zx - zoomBounds.getX()) * zoomArea.getWidth() / zoomBounds.getWidth() + zoomArea.getX();
    }

    public double zoomToNonZoomY(double zy) {
        return (zy - zoomBounds.getY()) * zoomArea.getHeight() / zoomBounds.getHeight() + zoomArea.getY();
    }

    public double zoomWidthRatio() {
        return zoomBounds.getWidth() / zoomArea.getWidth();
    }

    public double zoomHeightRatio() {
        return zoomBounds.getHeight() / zoomArea.getHeight();
    }

    public void setZoomBounds(double x, double y, double w, double h) {
        double widthRatio = w / zoomBounds.getWidth();
        double heightRatio = h / zoomBounds.getHeight();



        zoomBounds.setX(x);
        zoomBounds.setY(y);
        zoomBounds.setWidth(w);
        zoomBounds.setHeight(h);

        if (Math.abs(widthRatio - heightRatio) < 0.00001) {
            setZoomArea(
                    zoomBounds.getX() + x * widthRatio,
                    zoomBounds.getY() + y * heightRatio,
                    w * widthRatio,
                    h * heightRatio);
        } else {
            setZoomArea(x, y, w, h);
        }
    }

    public void setZoomArea(double x, double y, double w, double h) {
        zoomArea.setWidth(Math.clamp(w, 50, zoomBounds.getWidth()));
        zoomArea.setHeight(Math.clamp(h, 50, zoomBounds.getHeight()));

        zoomArea.setX(Math.clamp(x, zoomBounds.getX(), zoomBounds.getX() + zoomBounds.getWidth() - zoomArea.getWidth()));
        zoomArea.setY(Math.clamp(y, zoomBounds.getY(), zoomBounds.getY() + zoomBounds.getHeight() - zoomArea.getHeight()));

        redrawPendingProperty.set(true);
    }

    public void fillRectWithZoom(double x, double y, double w, double h) {
        x = (x - zoomArea.getX()) * zoomWidthRatio() + zoomBounds.getX();
        y = (y - zoomArea.getY()) * zoomHeightRatio() + zoomBounds.getY();

        w *= zoomWidthRatio();
        h *= zoomHeightRatio();

        getGraphicsContext2D().fillRect(x, y, w, h);
    }

    public void drawImageWithZoom(Image img, double x, double y) {
        x = (x - zoomArea.getX()) * zoomWidthRatio() + zoomBounds.getX();
        y = (y - zoomArea.getY()) * zoomHeightRatio() + zoomBounds.getY();

        getGraphicsContext2D().drawImage(img, x, y);
    }

    private void registerScrollListener() {
        setOnScroll(event -> {
            if (Math.abs(event.getDeltaY()) < 5) {
                return;
            }

            // mouse position x and y as decimal fractions
            double mouseXRatio = event.getX() / getWidth();
            double mouseYRatio = event.getY() / getHeight();

            double newWidth = 0;
            double newHeight = 0;

            // positive deltaY means scroll wheel was pushed up
            if (event.getDeltaY() > 0) {
                newWidth = zoomArea.getWidth() * ZOOM_RATIO;
                newHeight = zoomArea.getHeight() * ZOOM_RATIO;
            } else {
                newWidth = zoomArea.getWidth() / ZOOM_RATIO;
                newHeight = zoomArea.getHeight() / ZOOM_RATIO;
            }

            double zoomXDelta = (zoomArea.getWidth() - newWidth) * mouseXRatio;
            double zoomYDelta = (zoomArea.getHeight() - newHeight) * mouseYRatio;

            double newZoomX = zoomArea.getX() + zoomXDelta;
            double newZoomY = zoomArea.getY() + zoomYDelta;

            setZoomArea(newZoomX, newZoomY, newWidth, newHeight);
        });
    }

    private void registerDragListener() {
        addEventHandler(MouseEvent.MOUSE_PRESSED, ev -> {
            if (ev.getButton() == MouseButton.MIDDLE) {
                System.out.println("*******************");
                lastMouseX = ev.getX();
                lastMouseY = ev.getY();
            }
        });

        addEventHandler(MouseDragEvent.MOUSE_DRAGGED, ev -> {
            if (ev.getButton() == MouseButton.MIDDLE) {
                double deltaX = 0.75 * (ev.getX() - lastMouseX);
                double deltaY = 0.75 * (ev.getY() - lastMouseY);

                setZoomArea(
                        zoomArea.getX() - deltaX,
                        zoomArea.getY() - deltaY,
                        zoomArea.getWidth(),
                        zoomArea.getHeight());

                lastMouseX = ev.getX();
                lastMouseY = ev.getY();
            }
        });
    }
}

