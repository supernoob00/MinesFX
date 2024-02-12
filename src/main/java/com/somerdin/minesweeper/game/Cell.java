package com.somerdin.minesweeper.game;

public class Cell {
    private CellStatus cellStatus;
    private BombStatus bombStatus;

    public Cell(CellStatus cellStatus) {
        this.cellStatus = cellStatus;
        this.bombStatus = BombStatus.NONE;
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
        if (status == BombStatus.DETONATED && cellStatus != CellStatus.REVEALED) {
            throw new IllegalArgumentException("Bomb status cannot be set to detonated if cell is not revealed.");
        }
        bombStatus = status;
    }

    public CellStatus getCellStatus() {
        return cellStatus;
    }

    public void setCellStatus(CellStatus status) {
        cellStatus = status;
    }
}
