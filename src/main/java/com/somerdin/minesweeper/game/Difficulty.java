package com.somerdin.minesweeper.game;

public record Difficulty(int rows, int cols, int bombPercent) {
    public static final Difficulty EASY = new Difficulty(8, 8, 16);
    public static final Difficulty MEDIUM = new Difficulty(16, 16, 16);
    public static final Difficulty HARD = new Difficulty(16, 30, 21);

    public Difficulty {
        if (rows < 4 || cols < 4 || rows > 100 || cols > 100) {
            throw new IllegalArgumentException("Row and column count must be between 4 and 100");
        }
        if (bombPercent < 0 || bombPercent > 99) {
            throw new IllegalArgumentException("Invalid bomb ratio");
        }
    }
}
