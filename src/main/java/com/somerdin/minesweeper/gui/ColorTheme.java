package com.somerdin.minesweeper.gui;

import javafx.scene.paint.Color;

public enum ColorTheme {
    DEFAULT(Color.RED, Color.LIGHTGRAY, Color.WHITESMOKE, Color.GREY, Color.WHEAT),
    SPECIAL(Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.GREY, Color.WHEAT);

    private final Color bombColor;
    private final Color tileColor;
    private final Color revealedTileColor;
    private final Color gapColor;
    private final Color pausedColor;

    ColorTheme(Color bombColor, Color tileColor, Color revealedTileColor, Color gapColor, Color pausedColor) {
        this.bombColor = bombColor;
        this.tileColor = tileColor;
        this.revealedTileColor = revealedTileColor;
        this.gapColor = gapColor;
        this.pausedColor = pausedColor;
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
}
