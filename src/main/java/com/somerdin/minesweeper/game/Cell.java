package com.somerdin.minesweeper.game;

public class Cell {
    private boolean isExploded;

    private CellStatus cellStatus;
    private BombStatus bombStatus;

    public Cell(CellStatus cellStatus, boolean bombStatus) {
        this.cellStatus = cellStatus;
        this.bombStatus = bombStatus ? BombStatus.UNDETONATED : BombStatus.NONE;
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

    public void setBombStatus(BombStatus status) {
        bombStatus = status;
    }

    public CellStatus getCellStatus() {
        return cellStatus;
    }

    public void setCellStatus(CellStatus status) {
        cellStatus = status;
    }
}
