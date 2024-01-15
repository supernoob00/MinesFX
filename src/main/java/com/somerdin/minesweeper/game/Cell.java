package com.somerdin.minesweeper.game;

public class Cell {
    private boolean isExploded;

    private CellStatus cellStatus;
    private BombStatus bombStatus;

    public Cell(boolean isBomb, CellStatus cs) {
        bombStatus = isBomb ? BombStatus.UNDETONATED : BombStatus.NONE;
        cellStatus = cs;
    }

    public boolean isBomb() {
        return bombStatus != BombStatus.NONE;
    }

    public void setBomb(boolean bomb) {
        bombStatus = bomb ? BombStatus.UNDETONATED : BombStatus.NONE;
    }

    public BombStatus getBombStatus() {
        return bombStatus;
    }

    public void setBombStatus(BombStatus bs) {
        bombStatus = bs;
    }

    public CellStatus getCellStatus() {
        return cellStatus;
    }

    public void setCellStatus(CellStatus newState) {
        cellStatus = newState;
    }
}
