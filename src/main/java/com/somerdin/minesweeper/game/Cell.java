package com.somerdin.minesweeper.game;

public class Cell {
    private boolean isBomb;
    private CellState state;

    public Cell(boolean bomb, CellState cs) {
        isBomb = bomb;
        state = cs;
    }

    public boolean isBomb() {
        return isBomb;
    }

    public void setBomb(boolean bomb) {
        isBomb = bomb;
    }

    public CellState getState() {
        return state;
    }

    public void setState(CellState newState) {
        state = newState;
    }
}
