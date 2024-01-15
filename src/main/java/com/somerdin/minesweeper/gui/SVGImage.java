package com.somerdin.minesweeper.gui;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import javafx.beans.value.ObservableNumberValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Implementation of a JavaFX image, backed by an SVG, that resizes
 * automatically when its bound value changes
 */
public class SVGImage {
    private SVGDocument svg;
    private WritableImage image;

    private ObservableNumberValue observable;
    private int lastSize;

    // TODO: change parameter to URL instead of svg document
    public SVGImage(SVGDocument svgDocument, ObservableNumberValue observableSize) {
        svg = svgDocument;

        image = new WritableImage(observableSize.intValue(), observableSize.intValue());
        observable = observableSize;
        lastSize = observableSize.intValue();

        resize(lastSize, lastSize);
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

    private void resize(int width, int height) {
        image = new WritableImage(width, height);

        System.out.println("New SVG size " + svg.size());

        BufferedImage bufImg = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = bufImg.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        svg.render(null, gr, new ViewBox(width, height));
        gr.dispose();

        SwingFXUtils.toFXImage(bufImg, image);
    }
}
