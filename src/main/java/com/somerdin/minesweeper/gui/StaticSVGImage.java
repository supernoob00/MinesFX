package com.somerdin.minesweeper.gui;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

public class StaticSVGImage {
    private SVGDocument svg;
    private WritableImage image;

    public StaticSVGImage(URL url, int size) {
        SVGLoader loader = new SVGLoader();
        svg = loader.load(url);
        image = new WritableImage(size, size);
        convertSVG(size, size);
    }

    public WritableImage getImage() {
        return image;
    }

    public ImageView getImageView() {
        return new ImageView(image);
    }

    private void convertSVG(int width, int height) {
        BufferedImage bufImg = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = bufImg.createGraphics();
        gr.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        svg.render(null, gr, new ViewBox(
                0,
                0,
                width,
                height));
        gr.dispose();
        SwingFXUtils.toFXImage(bufImg, image);
    }
}
