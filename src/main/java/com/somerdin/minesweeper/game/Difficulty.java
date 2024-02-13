package com.somerdin.minesweeper.game;

public record Difficulty(int rows, int cols, int bombPercent) {
    public static final int MIN_BOMB_PERCENT = 1;
    public static final int MAX_BOMB_PERCENT = 99;

    public static final int MIN_ROWS = 4;
    public static final int MAX_ROWS = 80;
    public static final int MIN_COLS = 4;
    public static final int MAX_COLS = 80;

    public static final Difficulty EASY = new Difficulty(8, 8, 16);
    public static final Difficulty MEDIUM = new Difficulty(16, 16, 16);
    public static final Difficulty HARD = new Difficulty(16, 30, 21);

    public Difficulty {
        if (rows < MIN_ROWS || cols < MIN_COLS || rows > MAX_ROWS || cols > MAX_COLS) {
            throw new IllegalArgumentException("Row and column count must be between min and max.");
        }
        if (bombPercent < MIN_BOMB_PERCENT || bombPercent > MAX_BOMB_PERCENT) {
            throw new IllegalArgumentException("Invalid bomb ratio.");
        }
    }
}
