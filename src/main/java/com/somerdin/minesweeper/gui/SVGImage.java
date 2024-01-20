package com.somerdin.minesweeper.gui;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import javafx.beans.value.ObservableNumberValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.image.BufferedImage;

// TODO: only works for square images
/**
 * Implementation of a JavaFX image, backed by an SVG, that resizes
 * automatically when its bound value changes
 */
public class SVGImage {
    private SVGDocument svg;
    private WritableImage image;

    private ObservableNumberValue observable;
    private int lastSize;
    private double scaleFactor;

    // TODO: change parameter to URL instead of svg document
    public SVGImage(SVGDocument svgDocument,
                    ObservableNumberValue observableSize,
                    double scale) {
        if (scale > 1 || scale <= 0) {
            throw new IllegalArgumentException("Scale factor must be between 0 and 1");
        }
        scaleFactor = scale;
        svg = svgDocument;

        image = new WritableImage(observableSize.intValue(), observableSize.intValue());
        observable = observableSize;
        lastSize = observableSize.intValue();

        resize(lastSize, lastSize);
    }

    public SVGImage(SVGDocument svgDocument,
                    ObservableNumberValue observableSize) {
        this(svgDocument, observableSize, 1);
    }

    public SVGDocument getSvg() {
        return svg;
    }

    public WritableImage getFXImage() {
        if (lastSize != observable.intValue()) {
            int newValue = observable.intValue();
            resize(newValue, newValue);

            lastSize = observable.intValue();
        }
        return image;
    }

    public double getSize() {
        return observable.doubleValue() * scaleFactor;
    }

    private void resize(int width, int height) {
        int adjustedWidth = (int) (width * scaleFactor);
        int adjustedHeight = (int) (height * scaleFactor);

        System.out.println(scaleFactor);

        System.out.println(height);
        System.out.println(width);

        System.out.println(adjustedHeight);
        System.out.println(adjustedWidth);

        image = new WritableImage(adjustedWidth, adjustedHeight);

        BufferedImage bufImg = new BufferedImage(
                adjustedWidth, adjustedHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = bufImg.createGraphics();
        gr.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        svg.render(null, gr, new ViewBox(
                0,
                0,
                adjustedWidth,
                adjustedHeight));
        gr.dispose();
        SwingFXUtils.toFXImage(bufImg, image);
    }
}
