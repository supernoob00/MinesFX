package com.somerdin.minesweeper.gui;

import com.somerdin.minesweeper.game.*;
import javafx.beans.property.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
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

    private final ResizableCanvas canvas;
    private final GraphicsContext g;
    private final Minefield minefield;
    private final GameTimer gameTimer;

    public BoardTheme currentTheme = BoardTheme.DEFAULT;
    private ColorTheme colorTheme = ColorTheme.DEFAULT;

    private DoubleProperty tileLength = new SimpleDoubleProperty();
    private BooleanProperty inProgress = new SimpleBooleanProperty();
    private double gap;
    private double xShift;
    private double yShift;

    private Cell hoverCell;
    private Cell pressedCell;

    public GameBoard(Minefield field, GameTimer timer) {
        gameTimer = timer;
        minefield = field;

        gap = 4;
        canvas = new ResizableCanvas();
        g = canvas.getGraphicsContext2D();

        addCanvasMouseListeners();

        // TODO: create a DelayedChangeListener class to avoid repeated computation
        canvas.widthProperty().addListener((observable, oldVal, newVal) -> {
            tileLength.set(tileLength());
            setXYShift();
            draw();
        });
        canvas.heightProperty().addListener((observable, oldVal, newVal) -> {
            tileLength.set(tileLength());
            setXYShift();
            draw();
        });

        canvas.resize(500, 500);

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
        setXYShift();

        draw();
    }

    public void draw() {
        if (!gameTimer.isPaused()) {
            g.setFill(colorTheme.getGapColor());
            g.clearRect(0, 0, width(), height());

            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    drawTile(xShift, yShift, i, j);
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

    public Difficulty getDifficulty() {
        return minefield.getDifficulty();
    }

    private void drawTile(double xShift, double yShift, int row, int col) {
        Cell cell = minefield.getCell(row, col);

        if (cell == pressedCell) {
            g.setFill(colorTheme.getSelectColor());
        } else if (cell == hoverCell) {
            g.setFill(colorTheme.getHoverColor());
        } else if (cell.getCellStatus() == CellStatus.REVEALED) {
            if (cell.getBombStatus() == BombStatus.DETONATED) {
                g.setFill(colorTheme.getBombColor());
            } else {
                g.setFill(colorTheme.getRevealedTileColor());
            }
        } else {
            g.setFill(colorTheme.getTileColor());
        }

        double x = cellCornerX(col) + xShift;
        double y = cellCornerY(row) + yShift;

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

    private int getRow(double mouseY) {
        double tileGapSize = tileLength.get() + gap;
        double row = (mouseY - yShift) / tileGapSize;
        if (row < 0 || row > rows()) {
            return -1;
        }
        return (int) row;
    }

    private int getCol(double mouseX) {
        double tileGapSize = tileLength.get() + gap;
        double col = (mouseX - xShift) / tileGapSize;
        if (col < 0 || col > cols()) {
            return -1;
        }
        return (int) col;
    }

    private double cellCornerX(int col) {
        return gap + col * (tileLength.get() + gap);
    }

    private double cellCornerY(int row) {
        return gap + row * (tileLength.get() + gap);
    }

    private void setXYShift() {
        double gridWidth = (tileLength.get() + gap) * cols() + gap;
        double gridHeight = (tileLength.get() + gap) * rows() + gap;

        xShift = Math.max(0, (canvas.getWidth() - gridWidth) / 2);
        yShift = Math.max(0, (canvas.getHeight() - gridHeight) / 2);
    }

    private boolean isGameInteractive() {
        return minefield.getGameResult() == GameResult.IN_PROGRESS
                && !gameTimer.isPaused();
    }

    private boolean isCellHoverable(int row, int col) {
        return minefield.getCell(row, col).getCellStatus() != CellStatus.REVEALED;
    }

    private boolean isCellSelectable(int row, int col) {
        return minefield.getCell(row, col).getCellStatus() == CellStatus.HIDDEN;
    }

    private boolean isCellFlaggable(int row, int col) {
        return minefield.getCell(row, col).getCellStatus() != CellStatus.REVEALED;
    }

    private void addCanvasMouseListeners() {
        canvas.setOnMouseMoved(ev -> {
            if (!isGameInteractive()) {
                return;
            }

            int row = getRow(ev.getY());
            int col = getCol(ev.getX());

            if (row == -1 || col == -1 || !isCellHoverable(row, col)) {
                hoverCell = null;
            } else {
                hoverCell = minefield.getCell(row, col);
            }
            draw();
        });

        canvas.setOnMouseDragged(ev -> {
            if (!isGameInteractive()) {
                return;
            }

            int row = getRow(ev.getY());
            int col = getCol(ev.getX());

            if (row == -1 || col == -1) {
                System.out.println("Test");
                hoverCell = null;
                pressedCell = null;
            } else if (!isCellSelectable(row, col)
                    || minefield.getCell(row, col) != pressedCell) {
                pressedCell = null;
                hoverCell = minefield.getCell(row, col);
            }
            draw();
        });

        canvas.setOnMousePressed(ev -> {
            int row = getRow(ev.getY());
            int col = getCol(ev.getX());

            if (!isGameInteractive() || row == -1 || col == -1) {
                return;
            }

            Cell cell = minefield.getCell(row, col);
            if (ev.getButton() == MouseButton.PRIMARY && isCellSelectable(row, col)) {
                pressedCell = cell;
            } else if (ev.getButton() == MouseButton.SECONDARY
                    && cell.getCellStatus() != CellStatus.REVEALED) {
                pressedCell = null;
                minefield.toggleFlag(row, col);
            }
            draw();
        });
        canvas.setOnMouseReleased(ev -> {
            int row = getRow(ev.getY());
            int col = getCol(ev.getX());

            if (row == -1 || col == -1
                    || minefield.gameResultProperty().get() != GameResult.IN_PROGRESS
                    || gameTimer.isPaused()
                    || pressedCell == null) {
                return;
            }

            pressedCell = null;

            Cell cell = minefield.getCell(row, col);

            if (ev.getButton() == MouseButton.PRIMARY && isCellSelectable(row, col)) {
                inProgress.set(true);
                hoverCell = null;

                if (cell.getCellStatus() != CellStatus.REVEALED) {
                    minefield.chooseCell(row, col);

                    if (!gameTimer.isRunning()) {
                        gameTimer.start();
                    }

                    if (minefield.getGameResult() == GameResult.GAME_WON
                            || minefield.getGameResult() == GameResult.GAME_LOST) {
                        inProgress.set(false);
                        gameTimer.stop();
                    }
                }
            }
            draw();
        });
        canvas.setOnMouseExited(ev -> {
            hoverCell = null;
            pressedCell = null;
            draw();
        });
        canvas.setOnMouseDragExited(ev -> {
            hoverCell = null;
            pressedCell = null;
            draw();
        });
    }
}
