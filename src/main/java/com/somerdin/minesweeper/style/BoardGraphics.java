package com.somerdin.minesweeper.style;

import com.somerdin.minesweeper.gui.Tile;

import java.net.URL;

public enum BoardGraphics {
    DEFAULT(
        BoardGraphics.class.getResource("/classic/1mines.svg"),
        BoardGraphics.class.getResource("/classic/2mines.svg"),
        BoardGraphics.class.getResource("/classic/3mines.svg"),
        BoardGraphics.class.getResource("/classic/4mines.svg"),
        BoardGraphics.class.getResource("/classic/5mines.svg"),
        BoardGraphics.class.getResource("/classic/6mines.svg"),
        BoardGraphics.class.getResource("/classic/7mines.svg"),
        BoardGraphics.class.getResource("/classic/8mines.svg"),
        BoardGraphics.class.getResource("/classic/exploded.svg"),
        BoardGraphics.class.getResource("/classic/flag.svg"),
        BoardGraphics.class.getResource("/classic/maybe.svg"),
        BoardGraphics.class.getResource("/classic/mine.svg"),
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

    private BoardGraphics(URL url1,
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
