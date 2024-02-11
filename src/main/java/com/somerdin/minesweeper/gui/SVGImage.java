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
    private static WritableImage writeSVGToImage(SVGDocument svg, int width, int height) {
        WritableImage image = new WritableImage((int) width, (int) height);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = bufferedImage.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        svg.render(null, gr, new ViewBox(0, 0, width, height));
        gr.dispose();
        return SwingFXUtils.toFXImage(bufferedImage, image);
    }

    private SVGDocument svg;
    private WritableImage image;

    private ObservableNumberValue size;
    private ObservableNumberValue scale;

    private int lastSize;
    private double lastScale;

    private double scaleFactor;

    /**
     *
     * @param url the file URL of the SVG to load
     * @param boundDimension an observable value that propagates any changes
     * @param scale the scale factor to apply to the bound dimension
     */
    public SVGImage(URL url,
                    ObservableNumberValue boundDimension,
                    ObservableNumberValue boundScale,
                    double scale) {
        if (scale > 1 || scale <= 0) {
            throw new IllegalArgumentException("Scale factor must be between 0 and 1");
        }
        SVGLoader loader = new SVGLoader();

        this.scaleFactor = scale;
        this.svg = loader.load(url);

        this.size = boundDimension;
        this.scale = boundScale;

        this.lastSize = boundDimension.intValue();
        this.lastScale = boundScale.doubleValue();

        this.image = writeSVGToImage(svg, lastSize, lastSize);
    }

    public SVGDocument getSvg() {
        return svg;
    }

    public WritableImage getFXImage() {
        if (lastSize != size.intValue() || lastScale != scale.doubleValue()) {
            int calculatedSize = (int) getCalculatedSize();
            image = writeSVGToImage(svg, calculatedSize, calculatedSize);

            lastSize = size.intValue();
            lastScale = scale.doubleValue();
        }
        return image;
    }

    /* gets the true size of currently cached JavaFX image */
    public double getCalculatedSize() {
        return size.doubleValue() * scale.doubleValue() * scaleFactor;
    }

    /* sets the scale factor */
    public void setScaleFactor(double newFactor) {
        scaleFactor = newFactor;
    }

    /* replaces current SVG with one specified from file URL, then replaces current cached JavaFX image */
    public void setSvg(URL url) {
        SVGLoader loader = new SVGLoader();
        svg = loader.load(url);

        int calculatedSize = (int) getCalculatedSize();
        image = writeSVGToImage(svg, calculatedSize, calculatedSize);
    }
}
