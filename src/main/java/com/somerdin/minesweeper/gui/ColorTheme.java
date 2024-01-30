package com.somerdin.minesweeper.gui;

import javafx.scene.paint.Color;

public enum ColorTheme {
    DEFAULT(Color.RED,
            Color.LIGHTGRAY,
            Color.WHITESMOKE,
            Color.TRANSPARENT,
            Color.WHEAT,
            Color.GOLD,
            Color.BLUE),
    SPECIAL(Color.TRANSPARENT,
            Color.TRANSPARENT,
            Color.TRANSPARENT,
            Color.GREY,
            Color.WHEAT,
            Color.GOLD,
            Color.BLUE);

    private final Color bombColor;
    private final Color tileColor;
    private final Color revealedTileColor;
    private final Color gapColor;
    private final Color pausedColor;
    private final Color hoverColor;
    private final Color selectColor;

    ColorTheme(Color bombColor,
               Color tileColor,
               Color revealedTileColor,
               Color gapColor,
               Color pausedColor,
               Color hoverColor,
               Color selectColor) {
        this.bombColor = bombColor;
        this.tileColor = tileColor;
        this.revealedTileColor = revealedTileColor;
        this.gapColor = gapColor;
        this.pausedColor = pausedColor;
        this.hoverColor = hoverColor;
        this.selectColor = selectColor;
    }

    public Color getBombColor() {
        return bombColor;
    }

    public Color getTileColor() {
        return tileColor;
    }

    public Color getRevealedTileColor() {
        return revealedTileColor;
    }

    public Color getGapColor() {
        return gapColor;
    }

    public Color getPausedColor() {
        return pausedColor;
    }

    public Color getHoverColor() {
        return hoverColor;
    }

    public Color getSelectColor() {
        return selectColor;
    }
}
