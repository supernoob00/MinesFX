package com.somerdin.minesweeper.gui;

import com.somerdin.minesweeper.game.*;
import javafx.beans.property.*;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.Optional;

public class GameBoard {
    private final SVGImage DEFAULT_1;
    private final SVGImage DEFAULT_2;
    private final SVGImage DEFAULT_3;
    private final SVGImage DEFAULT_4;
    private final SVGImage DEFAULT_5;
    private final SVGImage DEFAULT_6;
    private final SVGImage DEFAULT_7;
    private final SVGImage DEFAULT_8;
    private final SVGImage DEFAULT_MINE;
    private final SVGImage DEFAULT_EXPLODED;
    private final SVGImage DEFAULT_FLAG;
    private final SVGImage DEFAULT_MAYBE;

    private Color bombColor = Color.RED;
    private Color tileColor = Color.LIGHTGRAY;
    private Color revealedTileColor = Color.WHITESMOKE;
    private Color gapColor = Color.GREY;

    private final ResizableCanvas canvas;
    private final GraphicsContext g;

    private double gap;

    // TODO: add padding logic to draw methods
    private double padding;
    private DoubleProperty tileLength = new SimpleDoubleProperty();
    private BooleanProperty isRunning = new SimpleBooleanProperty();

    private IntegerProperty flagsPlaced = new SimpleIntegerProperty();
    private IntegerProperty bombCount = new SimpleIntegerProperty();

    private Minefield minefield;

    public GameBoard(Minefield field) {
        minefield = field;

        bombCount.set(minefield.getBombCount());

        gap = 4;
        canvas = new ResizableCanvas();

        // TODO: create a DelayedChangeListener class to avoid repeated computation
        canvas.widthProperty().addListener((observable, oldVal, newVal) -> {
            tileLength.set(tileLength());
        });
        canvas.heightProperty().addListener((observable, oldVal, newVal) -> {
            tileLength.set(tileLength());
        });

        // make sure closure captures class variable, not argument variable
        canvas.setOnMouseClicked(ev -> {
            int row = getRow(ev.getY());
            int col = getCol(ev.getX());

            if (row == -1 || col == -1
                    || minefield.getResult() != GameResult.IN_PROGRESS) {
                return;
            }

            Cell selectedCell = minefield.getCell(row, col);
            switch (ev.getButton()) {
                case PRIMARY:
                    if (selectedCell.getCellStatus() != CellStatus.REVEALED) {
                        minefield.chooseCell(row, col);
                        draw();

                        if (!isRunning.get()) {
                            isRunning.set(true);
                        }

                        switch (minefield.getResult()) {
                            case GAME_WON, GAME_LOST -> isRunningProperty().set(false);
                        }
                    }
                    break;
                case SECONDARY:
                    if (selectedCell.getCellStatus() != CellStatus.REVEALED) {
                        CellStatus oldStatus = selectedCell.getCellStatus();
                        minefield.toggleFlag(row, col);
                        if (selectedCell.getCellStatus() == CellStatus.FLAGGED) {
                            flagsPlaced.set(flagsPlaced.get() + 1);
                        } else if (oldStatus == CellStatus.FLAGGED) {
                            flagsPlaced.set(flagsPlaced.get() - 1);
                        }
                        System.out.println(flagsPlaced.get());
                        draw();
                    }
                    break;
                default:
                    break;
            }
        });

        canvas.resize(500, 500);
        g = canvas.getGraphicsContext2D();

        DEFAULT_1 = new SVGImage(BoardImages.DEFAULT_1, tileLength);
        DEFAULT_2 = new SVGImage(BoardImages.DEFAULT_2, tileLength);
        DEFAULT_3 = new SVGImage(BoardImages.DEFAULT_3, tileLength);
        DEFAULT_4 = new SVGImage(BoardImages.DEFAULT_4, tileLength);
        DEFAULT_5 = new SVGImage(BoardImages.DEFAULT_5, tileLength);
        DEFAULT_6 = new SVGImage(BoardImages.DEFAULT_6, tileLength);
        DEFAULT_7 = new SVGImage(BoardImages.DEFAULT_7, tileLength);
        DEFAULT_8 = new SVGImage(BoardImages.DEFAULT_8, tileLength);
        DEFAULT_MINE = new SVGImage(BoardImages.DEFAULT_MINE, tileLength);
        DEFAULT_EXPLODED = new SVGImage(BoardImages.DEFAULT_EXPLODED, tileLength);
        DEFAULT_FLAG = new SVGImage(BoardImages.DEFAULT_FLAG, tileLength);
        DEFAULT_MAYBE = new SVGImage(BoardImages.DEFAULT_MAYBE, tileLength);

        draw();
    }

    public ResizableCanvas getCanvas() {
        return canvas;
    }

    public void startNewGame(int rows, int cols, int percent) {
        minefield = new Minefield(rows, cols, percent);
        flagsPlaced.set(0);
        bombCount.set(minefield.getBombCount());
        tileLength.set(tileLength());
        draw();
    }

    public void draw() {
        g.setFill(gapColor);
        g.fillRect(0, 0, width(), height());

        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                drawTile(i, j);
            }
        }
    }

    public BooleanProperty isRunningProperty() {
        return isRunning;
    }

    public IntegerProperty flagsPlacedProperty() {
        return flagsPlaced;
    }

    public IntegerProperty bombCountProperty() {
        return bombCount;
    }

    private ObservableDoubleValue widthObservable() {
        return canvas.widthProperty();
    }

    private ObservableDoubleValue heightObservable() {
        return canvas.heightProperty();
    }

    private void drawTile(int row, int col) {
        Cell cell = minefield.getCell(row, col);
        if (cell.getCellStatus() == CellStatus.REVEALED) {
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

        g.fillRect(x, y, tileLength.get(), tileLength.get());

        Optional<Image> img = getTileImage(row, col);
        if (img.isPresent()) {
            System.out.println("Width " + img.get().getWidth());
            System.out.println("Height " + img.get().getHeight());
            g.drawImage(img.get(), x, y);
        }
    }

    private Optional<Image> getTileImage(int row, int col) {
        Cell cell = minefield.getCell(row, col);

        switch (cell.getCellStatus()) {
            case HIDDEN:
                return Optional.empty();
            case FLAGGED:
                return Optional.of(DEFAULT_FLAG.getFXImage());
            case FLAGGED_QUESTION:
                return Optional.of(DEFAULT_MAYBE.getFXImage());
            case REVEALED:
                if (cell.isBomb()) {
                    if (cell.getBombStatus() == BombStatus.UNDETONATED) {
                        return Optional.of(DEFAULT_MINE.getFXImage());
                    }
                    return Optional.of(DEFAULT_EXPLODED.getFXImage());
                }

                int count = minefield.neighborCount(row, col);
                if (count == 0) {
                    return Optional.empty();
                }
                return Optional.of(getNeighborCountImage(count));
            default:
                throw new IllegalStateException();
        }
    }

    private Image getNeighborCountImage(int neighborCount) {
        return switch (neighborCount) {
            case 1 -> DEFAULT_1.getFXImage();
            case 2 -> DEFAULT_2.getFXImage();
            case 3 -> DEFAULT_3.getFXImage();
            case 4 -> DEFAULT_4.getFXImage();
            case 5 -> DEFAULT_5.getFXImage();
            case 6 -> DEFAULT_6.getFXImage();
            case 7 -> DEFAULT_7.getFXImage();
            case 8 -> DEFAULT_8.getFXImage();
            default -> throw new IllegalArgumentException();
        };
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
        if (width() / cols() < height() / rows()) {
            double totalGap = (cols() + 1) * gap;
            return (width() - totalGap) / cols();
        }
        double totalGap = (rows() + 1) * gap;
        return (height() - totalGap) / rows();
    }

    private int getRow(double mouseX) {
        double val = tileLength.get() + gap;
        int row = (int) ((mouseX - padding) / val);
        if (row < 0 || row > rows() - 1) {
            return -1;
        }
        return row;
    }

    private int getCol(double mouseY) {
        double val = tileLength.get() + gap;
        int col = (int) ((mouseY - padding) / val);
        if (col < 0 || col > cols() - 1) {
            return -1;
        }
        return col;
    }

    private double cellCornerX(int col) {
        return padding + gap + col * (tileLength.get() + gap);
    }

    private double cellCornerY(int row) {
        return padding + gap + row * (tileLength.get() + gap);
    }
}
