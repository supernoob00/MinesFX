package com.somerdin.minesweeper.gui;

import com.somerdin.minesweeper.game.Cell;
import com.somerdin.minesweeper.game.CellState;
import com.somerdin.minesweeper.game.GameResult;
import com.somerdin.minesweeper.game.Minefield;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class GameBoard {
    private Color bombColor = Color.RED;
    private Color tileColor = Color.LIGHTBLUE;
    private Color revealedTileColor = Color.GREEN;
    private Color gapColor = Color.GREY;

    private final ResizableCanvas canvas;
    private final GraphicsContext g;

    private double gap;

    // TODO: add padding logic to draw methods
    private double padding;
    private double tileLength;

    private Minefield minefield;

    public GameBoard(Minefield field) {
        this.minefield = field;

        gap = 4;
        canvas = new ResizableCanvas();

        // make sure closure captures class variable, not argument variable
        canvas.setOnMouseClicked(ev -> {
            int row = getRow(ev.getY());
            int col = getCol(ev.getX());

            if (row == -1 || col == -1
                    || minefield.getResult() != GameResult.IN_PROGRESS) {
                return;
            }

            switch (ev.getButton()) {
                case PRIMARY:
                    if (minefield.getCell(row, col).getState() != CellState.REVEALED) {
                        minefield.chooseCell(row, col);
                        draw();
                    }
                    break;
                case SECONDARY:
                    minefield.getCell(row, col).setState(CellState.FLAGGED);
                    draw();
                    break;
                default:
                    break;
            }
        });

        canvas.resize(500, 500);

        g = canvas.getGraphicsContext2D();
        draw();
    }

    public ResizableCanvas getCanvas() {
        return canvas;
    }

    public void startNewGame() {
        minefield = new Minefield(8, 8, 0.2);
        drawResized();
    }

    public void draw() {
        System.out.println(minefield);
        g.setFill(gapColor);
        g.fillRect(0, 0, width(), height());

        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                drawTile(i, j);
            }
        }
    }

    public void drawResized() {
        tileLength = tileLength();
        draw();
    }

    private void drawTile(int row, int col) {
        Cell cell = minefield.getCell(row, col);
        if (cell.getState() == CellState.REVEALED) {
            if (cell.isBomb()) {
                g.setFill(bombColor);
            } else {
                g.setFill(revealedTileColor);
            }
        } else {
            g.setFill(tileColor);
        }

        double x = cellCornerX(col);
        double y = cellCornerY(row);

        g.fillRect(x, y, tileLength, tileLength);

        int neighborCount = minefield.neighborCount(row, col);
        if (cell.getState() == CellState.REVEALED && neighborCount > 0) {
            g.setFill(Color.BLACK);
            g.fillText(String.valueOf(neighborCount), x, y + tileLength);
        }
    }

    private double width() {
        return canvas.getWidth();
    }

    private double height() {
        return canvas.getHeight();
    }

    private int rows() {
        return minefield.rowCount();
    }

    private int cols() {
        return minefield.colCount();
    }

    // TODO: clean this up
    /* other methods should use class field value to avoid unnecessary calculation */
    private double tileLength() {
        if (width() < height()) {
            double totalGap = (cols() + 1) * gap;
            return (width() - totalGap) / cols();
        }
        double totalGap = (rows() + 1) * gap;
        return (height() - totalGap) / rows();
    }

    private int getRow(double mouseX) {
        double val = tileLength + gap;
        int row = (int) ((mouseX - padding) / val);
        if (row < 0 || row > rows() - 1) {
            return -1;
        }
        return row;
    }

    private int getCol(double mouseY) {
        double val = tileLength + gap;
        int col = (int) ((mouseY - padding) / val);
        if (col < 0 || col > cols() - 1) {
            return -1;
        }
        return col;
    }

    private double cellCornerX(int col) {
        return padding + gap + col * (tileLength + gap);
    }

    private double cellCornerY(int row) {
        return padding + gap + row * (tileLength + gap);
    }
}
