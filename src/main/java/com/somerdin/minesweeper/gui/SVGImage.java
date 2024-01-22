package com.somerdin.minesweeper.gui;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;
import javafx.beans.value.ObservableNumberValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

// TODO: only works for square images
/**
 * Implementation of a JavaFX image, backed by an SVG file, that resizes
 * automatically when a given bound dimension value changes. An SVG is used to
 * allow the image to have arbitrarily high resolution; this is necessary
 * because JavaFX does not natively support SVGs
 */
public class SVGImage {
    private SVGDocument svg;
    private WritableImage image;

    private ObservableNumberValue observable;
    private int lastSize;
    private double scaleFactor;

    /**
     *
     * @param url the file URL of the SVG to load
     * @param boundDimension an observable value that propagates any changes
     * @param scale the scale factor to apply to the bound dimension
     */
    public SVGImage(URL url,
                    ObservableNumberValue boundDimension,
                    double scale) {
        if (scale > 1 || scale <= 0) {
            throw new IllegalArgumentException("Scale factor must be between 0 and 1");
        }
        SVGLoader loader = new SVGLoader();

        scaleFactor = scale;
        svg = loader.load(url);

        image = new WritableImage(boundDimension.intValue(), boundDimension.intValue());
        observable = boundDimension;
        lastSize = boundDimension.intValue();

        convertSVG(lastSize, lastSize);
    }

    public SVGImage(URL url, ObservableNumberValue boundDimension) {
        this(url, boundDimension, 1);
    }

    public SVGDocument getSvg() {
        return svg;
    }

    public WritableImage getFXImage() {
        if (lastSize != observable.intValue()) {
            int newValue = observable.intValue();
            convertSVG(newValue, newValue);

            lastSize = observable.intValue();
        }
        return image;
    }

    /* gets the size of currently cached JavaFX image */
    public double getSize() {
        return observable.doubleValue() * scaleFactor;
    }

    /* sets the
    public void setScaleFactor(double newFactor) {
        scaleFactor = newFactor;
    }

    /* replaces current SVG with one specified from file URL, then replaces current cached JavaFX image */
    public void setSvg(URL url) {
        SVGLoader loader = new SVGLoader();
        svg = loader.load(url);
        convertSVG(getSize(), getSize());
    }

    private void convertSVG(double width, double height) {
        int adjustedWidth = (int) (width * scaleFactor);
        int adjustedHeight = (int) (height * scaleFactor);

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
