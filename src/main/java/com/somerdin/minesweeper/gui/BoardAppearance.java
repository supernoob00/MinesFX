package com.somerdin.minesweeper.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.image.Image;

import java.net.URL;

public class BoardAppearance {
    public static enum Theme {
        DEFAULT(
                Theme.class.getResource("/classic/1mines.svg"),
                Theme.class.getResource("/classic/2mines.svg"),
                Theme.class.getResource("/classic/3mines.svg"),
                Theme.class.getResource("/classic/4mines.svg"),
                Theme.class.getResource("/classic/5mines.svg"),
                Theme.class.getResource("/classic/6mines.svg"),
                Theme.class.getResource("/classic/7mines.svg"),
                Theme.class.getResource("/classic/8mines.svg"),
                Theme.class.getResource("/classic/exploded.svg"),
                Theme.class.getResource("/classic/flag.svg"),
                Theme.class.getResource("/classic/maybe.svg"),
                Theme.class.getResource("/classic/mine.svg"),
                0.75
        );

        private final URL url1;
        private final URL url2;
        private final URL url3;
        private final URL url4;
        private final URL url5;
        private final URL url6;
        private final URL url7;
        private final URL url8;
        private final URL urlExploded;
        private final URL urlFlag;
        private final URL urlMaybe;
        private final URL urlMine;

        private final double scaleFactor;

        private Theme(URL url1,
                      URL url2,
                      URL url3,
                      URL url4,
                      URL url5,
                      URL url6,
                      URL url7,
                      URL url8,
                      URL urlExploded,
                      URL urlFlag,
                      URL urlMaybe,
                      URL urlMine,
                      double scaleFactor) {
            this.url1 = url1;
            this.url2 = url2;
            this.url3 = url3;
            this.url4 = url4;
            this.url5 = url5;
            this.url6 = url6;
            this.url7 = url7;
            this.url8 = url8;
            this.urlExploded = urlExploded;
            this.urlFlag = urlFlag;
            this.urlMaybe = urlMaybe;
            this.urlMine = urlMine;
            this.scaleFactor = scaleFactor;
        }

        public URL getURL(Tile tile) {
            return switch (tile) {
                case ONE -> url1;
                case TWO -> url2;
                case THREE -> url3;
                case FOUR -> url4;
                case FIVE -> url5;
                case SIX -> url6;
                case SEVEN -> url7;
                case EIGHT -> url8;
                case EXPLODED -> urlExploded;
                case FLAG -> urlFlag;
                case MAYBE -> urlMaybe;
                case MINE -> urlMine;
            };
        }

        public double getScaleFactor() {
            return scaleFactor;
        }
    }

    public static enum Tile {
        ONE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        EXPLODED,
        FLAG,
        MAYBE,
        MINE
    }

    private Theme currentTheme;

    private final SVGImage img1;
    private final SVGImage img2;
    private final SVGImage img3;
    private final SVGImage img4;
    private final SVGImage img5;
    private final SVGImage img6;
    private final SVGImage img7;
    private final SVGImage img8;
    private final SVGImage imgMine;
    private final SVGImage imgExploded;
    private final SVGImage imgFlag;
    private final SVGImage imgMaybe;

    public BoardAppearance(Theme theme,
                           DoubleProperty boundDimension,
                           DoubleProperty boundScale,
                           double scale) {
        this.currentTheme = theme;

        img1 = new SVGImage(theme.getURL(Tile.ONE), boundDimension, boundScale, scale);
        img2 = new SVGImage(theme.getURL(Tile.TWO), boundDimension, boundScale, scale);
        img3 = new SVGImage(theme.getURL(Tile.THREE), boundDimension, boundScale, scale);
        img4 = new SVGImage(theme.getURL(Tile.FOUR), boundDimension, boundScale, scale);
        img5 = new SVGImage(theme.getURL(Tile.FIVE), boundDimension, boundScale, scale);
        img6 = new SVGImage(theme.getURL(Tile.SIX), boundDimension, boundScale, scale);
        img7 = new SVGImage(theme.getURL(Tile.SEVEN), boundDimension, boundScale, scale);
        img8 = new SVGImage(theme.getURL(Tile.EIGHT), boundDimension, boundScale, scale);
        imgMine = new SVGImage(theme.getURL(Tile.MINE), boundDimension, boundScale, scale);
        imgExploded = new SVGImage(theme.getURL(Tile.EXPLODED), boundDimension, boundScale, scale);
        imgFlag = new SVGImage(theme.getURL(Tile.FLAG), boundDimension, boundScale, scale);
        imgMaybe = new SVGImage(theme.getURL(Tile.MAYBE), boundDimension, boundScale, scale);
    }

    public Image getImage(Tile tile) {
        return switch (tile) {
            case ONE -> img1.getFXImage();
            case TWO -> img2.getFXImage();
            case THREE -> img3.getFXImage();
            case FOUR -> img4.getFXImage();
            case FIVE -> img5.getFXImage();
            case SIX -> img6.getFXImage();
            case SEVEN -> img7.getFXImage();
            case EIGHT -> img8.getFXImage();
            case EXPLODED -> imgExploded.getFXImage();
            case FLAG -> imgFlag.getFXImage();
            case MAYBE -> imgMaybe.getFXImage();
            case MINE -> imgMine.getFXImage();
        };
    }

    public Image getNumberImage(int number) {
        return switch (number) {
            case 1 -> img1.getFXImage();
            case 2 -> img2.getFXImage();
            case 3 -> img3.getFXImage();
            case 4 -> img4.getFXImage();
            case 5 -> img5.getFXImage();
            case 6 -> img6.getFXImage();
            case 7 -> img7.getFXImage();
            case 8 -> img8.getFXImage();
            default -> throw new IllegalArgumentException("Invalid number");
        };
    }

    public void setTheme(Theme theme) {
        if (theme != currentTheme) {
            currentTheme = theme;

            img1.setSvg(theme.getURL(Tile.ONE));
            img2.setSvg(theme.getURL(Tile.TWO));
            img3.setSvg(theme.getURL(Tile.THREE));
            img4.setSvg(theme.getURL(Tile.FOUR));
            img5.setSvg(theme.getURL(Tile.FIVE));
            img6.setSvg(theme.getURL(Tile.SIX));
            img7.setSvg(theme.getURL(Tile.SEVEN));
            img8.setSvg(theme.getURL(Tile.EIGHT));
            imgExploded.setSvg(theme.getURL(Tile.EXPLODED));
            imgFlag.setSvg(theme.getURL(Tile.FLAG));
            imgMaybe.setSvg(theme.getURL(Tile.MAYBE));
            imgMine.setSvg(theme.getURL(Tile.MINE));
        }
    }
}
