package com.somerdin.minesweeper.gui;

import com.somerdin.minesweeper.game.*;
import javafx.beans.property.*;
import javafx.scene.Cursor;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Optional;

public class GameBoard {
    private final SVGImage img1;
    private final SVGImage img2;
    private final SVGImage img3;
    private final SVGImage img4;
    private final SVGImage img5;
    private final SVGImage img6;
    private final SVGImage img7;
    private final SVGImage img8;
    private final SVGImage imgMine;
    private final SVGImage imgExploded;
    private final SVGImage imgFlag;
    private final SVGImage imgMaybe;

    private double scaleFactor = 0.75;

    public BoardTheme currentTheme = BoardTheme.DEFAULT;
    private ColorTheme colorTheme = ColorTheme.DEFAULT;

    private final ResizableCanvas canvas;
    private final GraphicsContext g;

    private double gap;

    // TODO: add padding logic to draw methods
    private double padding;
    private DoubleProperty tileLength = new SimpleDoubleProperty();
    private BooleanProperty inProgress = new SimpleBooleanProperty();

    private GameTimer gameTimer;
    private Minefield minefield;

    public GameBoard(Minefield field, GameTimer timer) {
        gameTimer = timer;
        minefield = field;

        gap = 4;
        canvas = new ResizableCanvas();

        // TODO: create a DelayedChangeListener class to avoid repeated computation
        canvas.widthProperty().addListener((observable, oldVal, newVal) -> {
            tileLength.set(tileLength());
        });
        canvas.heightProperty().addListener((observable, oldVal, newVal) -> {
            tileLength.set(tileLength());
        });

        canvas.setCursor(Cursor.HAND);
        timer.pausedProperty().addListener((observable, oldValue, newValue) -> {
            canvas.setCursor(newValue ? Cursor.DEFAULT : Cursor.HAND);
        });

        // make sure closure captures class variable, not argument variable
        canvas.setOnMouseClicked(ev -> {
            int row = getRow(ev.getY());
            int col = getCol(ev.getX());

            if (row == -1 || col == -1
                    || minefield.gameResultProperty().get() != GameResult.IN_PROGRESS
                    || gameTimer.isPaused()) {
                return;
            }

            Cell selectedCell = minefield.getCell(row, col);
            switch (ev.getButton()) {
                case PRIMARY:
                    inProgress.set(true);

                    if (selectedCell.getCellStatus() != CellStatus.REVEALED) {
                        minefield.chooseCell(row, col);

                        if (!gameTimer.isRunning()) {
                            gameTimer.start();
                        }

                        switch (minefield.getGameResult()) {
                            case GAME_WON, GAME_LOST -> {
                                inProgress.set(false);
                                gameTimer.stop();
                            }
                        }
                        draw();
                    }
                    break;
                case SECONDARY:
                    if (selectedCell.getCellStatus() != CellStatus.REVEALED) {
                        CellStatus oldStatus = selectedCell.getCellStatus();
                        minefield.toggleFlag(row, col);
                        draw();
                    }
                    break;
                default:
                    break;
            }
        });

        canvas.resize(500, 500);
        g = canvas.getGraphicsContext2D();

        img1 = new SVGImage(currentTheme.getURL(Tile.ONE), tileLength, scaleFactor);
        img2 = new SVGImage(currentTheme.getURL(Tile.TWO), tileLength, scaleFactor);
        img3 = new SVGImage(currentTheme.getURL(Tile.THREE), tileLength, scaleFactor);
        img4 = new SVGImage(currentTheme.getURL(Tile.FOUR), tileLength, scaleFactor);
        img5 = new SVGImage(currentTheme.getURL(Tile.FIVE), tileLength, scaleFactor);
        img6 = new SVGImage(currentTheme.getURL(Tile.SIX), tileLength, scaleFactor);
        img7 = new SVGImage(currentTheme.getURL(Tile.SEVEN), tileLength, scaleFactor);
        img8 = new SVGImage(currentTheme.getURL(Tile.EIGHT), tileLength, scaleFactor);
        imgMine = new SVGImage(currentTheme.getURL(Tile.MINE), tileLength, scaleFactor);
        imgExploded = new SVGImage(currentTheme.getURL(Tile.EXPLODED), tileLength, scaleFactor);
        imgFlag = new SVGImage(currentTheme.getURL(Tile.FLAG), tileLength, scaleFactor);
        imgMaybe = new SVGImage(currentTheme.getURL(Tile.MAYBE), tileLength, scaleFactor);

        draw();
    }

    public ResizableCanvas getCanvas() {
        return canvas;
    }

    public void startNewGame(Difficulty difficulty) {
        minefield.startNewGame(difficulty);

        inProgress.set(false);
        tileLength.set(tileLength());

        draw();
    }

    public void draw() {
        if (!gameTimer.isPaused()) {
            g.setFill(colorTheme.getGapColor());
            g.fillRect(0, 0, width(), height());

            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    drawTile(i, j);
                }
            }
        } else {
            drawPaused();
        }
    }

    public void setTheme(BoardTheme theme) {
        currentTheme = theme;

        img1.setSvg(theme.getURL(Tile.ONE));
        img2.setSvg(theme.getURL(Tile.TWO));
        img3.setSvg(theme.getURL(Tile.THREE));
        img4.setSvg(theme.getURL(Tile.FOUR));
        img5.setSvg(theme.getURL(Tile.FIVE));
        img6.setSvg(theme.getURL(Tile.SIX));
        img7.setSvg(theme.getURL(Tile.SEVEN));
        img8.setSvg(theme.getURL(Tile.EIGHT));
        imgExploded.setSvg(theme.getURL(Tile.EXPLODED));
        imgFlag.setSvg(theme.getURL(Tile.FLAG));
        imgMaybe.setSvg(theme.getURL(Tile.MAYBE));
        imgMine.setSvg(theme.getURL(Tile.MINE));
    }

    public int getRowCount() {
        return minefield.rowCount();
    }

    public int getColCount() {
        return minefield.colCount();
    }

    public int getPercentBomb() {
        return minefield.getPercentBomb();
    }

    public IntegerProperty flaggedCountProperty() {
        return minefield.flaggedCountProperty();
    }

    public IntegerProperty bombCountProperty() {
        return minefield.bombCountProperty();
    }

    public BooleanProperty inProgressProperty() {
        return inProgress;
    }

    public BooleanProperty isFirstMoveProperty() {
        return minefield.firstMoveProperty();
    }

    public DoubleProperty widthProperty() {
        return canvas.widthProperty();
    }

    public DoubleProperty heightProperty() {
        return canvas.heightProperty();
    }

    private void drawTile(int row, int col) {
        Cell cell = minefield.getCell(row, col);
        if (cell.getCellStatus() == CellStatus.REVEALED) {
            if (cell.getBombStatus() == BombStatus.DETONATED) {
                g.setFill(colorTheme.getBombColor());
            } else {
                g.setFill(colorTheme.getRevealedTileColor());
            }
        } else {
            g.setFill(colorTheme.getTileColor());
        }

        double x = cellCornerX(col);
        double y = cellCornerY(row);

        g.fillRoundRect(x, y, tileLength.get(), tileLength.get(), 10, 10);

        Optional<Image> img = getTileImage(row, col);
        img.ifPresent(image -> g.drawImage(
                image,
                x + (tileLength.get() - image.getWidth()) / 2,
                y + (tileLength.get() - image.getHeight()) / 2));
    }

    private void drawPaused() {
        g.setFill(colorTheme.getPausedColor());
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.setFont(new Font(24));
        g.setFill(Color.BLACK);
        g.fillText("Paused", canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    private Optional<Image> getTileImage(int row, int col) {
        Cell cell = minefield.getCell(row, col);

        switch (cell.getCellStatus()) {
            case HIDDEN:
                return Optional.empty();
            case FLAGGED:
                return Optional.of(imgFlag.getFXImage());
            case FLAGGED_QUESTION:
                return Optional.of(imgMaybe.getFXImage());
            case REVEALED:
                if (cell.isBomb()) {
                    if (cell.getBombStatus() == BombStatus.UNDETONATED) {
                        return Optional.of(imgMine.getFXImage());
                    }
                    return Optional.of(imgExploded.getFXImage());
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
            case 1 -> img1.getFXImage();
            case 2 -> img2.getFXImage();
            case 3 -> img3.getFXImage();
            case 4 -> img4.getFXImage();
            case 5 -> img5.getFXImage();
            case 6 -> img6.getFXImage();
            case 7 -> img7.getFXImage();
            case 8 -> img8.getFXImage();
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
