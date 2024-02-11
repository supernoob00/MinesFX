package com.somerdin.minesweeper.gui;

import com.somerdin.minesweeper.game.*;
import com.somerdin.minesweeper.style.BoardGraphics;
import com.somerdin.minesweeper.style.ColorTheme;
import javafx.beans.property.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

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

    private final ZoomCanvas canvas;
    private final GraphicsContext g;
    private final Minefield minefield;
    private final GameTimer gameTimer;

    private BoardGraphics boardTheme = BoardGraphics.DEFAULT;
    private ColorTheme colorTheme = ColorTheme.DEFAULT;

    private DoubleProperty tileLength = new SimpleDoubleProperty();
    private DoubleProperty padding = new SimpleDoubleProperty(16);
    private DoubleProperty gap = new SimpleDoubleProperty(4);

    private BooleanProperty inProgress = new SimpleBooleanProperty();

    private Cell hoverCell;
    private Cell pressedCell;

    public GameBoard(Minefield field, GameTimer timer) {
        this.gameTimer = timer;
        this.minefield = field;

        this.canvas = new ZoomCanvas(0, 0);

        this.g = canvas.getGraphicsContext2D();


        addCanvasMouseListeners();

        // TODO: create a DelayedChangeListener class to avoid repeated computation
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> {
            tileLength.set(tileLength());
            setNewGameZoomBounds();
        });
        canvas.heightProperty().addListener((observable, oldValue, newValue) -> {
            tileLength.set(tileLength());
            setNewGameZoomBounds();
        });

        canvas.redrawPendingProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                draw();
                canvas.redrawPendingProperty().set(false);
            }
        }));

        canvas.resize(500, 500);

        img1 = new SVGImage(boardTheme.getURL(Tile.ONE), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        img2 = new SVGImage(boardTheme.getURL(Tile.TWO), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        img3 = new SVGImage(boardTheme.getURL(Tile.THREE), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        img4 = new SVGImage(boardTheme.getURL(Tile.FOUR), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        img5 = new SVGImage(boardTheme.getURL(Tile.FIVE), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        img6 = new SVGImage(boardTheme.getURL(Tile.SIX), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        img7 = new SVGImage(boardTheme.getURL(Tile.SEVEN), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        img8 = new SVGImage(boardTheme.getURL(Tile.EIGHT), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        imgMine = new SVGImage(boardTheme.getURL(Tile.MINE), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        imgExploded = new SVGImage(boardTheme.getURL(Tile.EXPLODED), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        imgFlag = new SVGImage(boardTheme.getURL(Tile.FLAG), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());
        imgMaybe = new SVGImage(boardTheme.getURL(Tile.MAYBE), tileLength, canvas.zoomScaleProperty(), boardTheme.getScaleFactor());

        canvas.redrawPendingProperty().set(true);
    }

    public ZoomCanvas getCanvas() {
        return canvas;
    }

    public void startNewGame(Difficulty difficulty) {
        minefield.startNewGame(difficulty);

        inProgress.set(false);
        tileLength.set(tileLength());
        setNewGameZoomBounds();

        canvas.redrawPendingProperty().set(true);
    }

    public void setTheme(BoardGraphics theme) {
        boardTheme = theme;

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

    private void draw() {
        if (!gameTimer.isPaused()) {
            g.setFill(colorTheme.getGapColor());
            g.clearRect(0, 0, width(), height());

            for (int i = 0; i < rows(); i++) {
                for (int j = 0; j < cols(); j++) {
                    System.out.println("X: " + canvas.getZoomBoundsX());
                    drawTile(canvas.getZoomBoundsX(), canvas.getZoomBoundsY(), i, j);
                }
            }
        } else {
            drawPaused();
        }
        Rectangle zoomArea = canvas.getZoomArea();
        g.strokeRect(zoomArea.getX(),
                zoomArea.getY(),
                zoomArea.getWidth(),
                zoomArea.getHeight());
        g.strokeRect(canvas.getZoomBoundsX(), canvas.getZoomBoundsY(), canvas.getZoomBoundsWidth(), canvas.getZoomBoundsHeight());
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

        System.out.println("X: " + x);

        canvas.fillRectWithZoom(x, y, tileLength.get(), tileLength.get());

        Image image = getTileImage(row, col);
        if (image != null) {
            System.out.println(canvas.getZoomScale());
            canvas.drawImageWithZoom(
                    image,
                    x + 0.125 * tileLength.get(),
                    y + 0.125 * tileLength.get());
        }
    }

    private void drawPaused() {
        g.setFill(colorTheme.getPausedColor());
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.setFont(new Font(24));
        g.setFill(Color.BLACK);
        g.fillText("Paused", canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    private Image getTileImage(int row, int col) {
        Cell cell = minefield.getCell(row, col);

        switch (cell.getCellStatus()) {
            case HIDDEN:
                return null;
            case FLAGGED:
                return imgFlag.getFXImage();
            case FLAGGED_QUESTION:
                return imgMaybe.getFXImage();
            case REVEALED:
                if (cell.isBomb()) {
                    if (cell.getBombStatus() == BombStatus.UNDETONATED) {
                        return imgMine.getFXImage();
                    }
                    return imgExploded.getFXImage();
                }

                int count = minefield.neighborCount(row, col);
                if (count == 0) {
                    return null;
                }
                return getNeighborCountImage(count);
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

    /* other methods should use class field value to avoid unnecessary calculation */
    private double tileLength() {
        if (width() / cols() < height() / rows()) {
            double totalGap = (cols() + 1) * gap.get() + (2 * padding.get());
            return (width() - totalGap) / cols();
        }
        double totalGap = (rows() + 1) * gap.get() + (2 * padding.get());
        return (height() - totalGap) / rows();
    }

    private int getRow(double mouseY) {
        mouseY = canvas.zoomToNonZoomY(mouseY);

        double tileGapSize = tileLength.get() + gap.get();
        double row = (mouseY - canvas.getZoomBoundsY() - padding.get()) / tileGapSize;
        if (row < 0 || row >= rows()) {
            return -1;
        }
        return (int) row;
    }

    private int getCol(double mouseX) {
        mouseX = canvas.zoomToNonZoomX(mouseX);

        double tileGapSize = tileLength.get() + gap.get();
        double col = (mouseX - canvas.getZoomBoundsX() - padding.get()) / tileGapSize;
        if (col < 0 || col >= cols()) {
            return -1;
        }
        return (int) col;
    }

    private double cellCornerX(int col) {
        return gap.get() + padding.get() + col * (tileLength.get() + gap.get());
    }

    private double cellCornerY(int row) {
        return gap.get() + padding.get() + row * (tileLength.get() + gap.get());
    }

    private void setNewGameZoomBounds() {
        double gridWidth = (tileLength.get() + gap.get()) * cols() + gap.get() + (2 * padding.get());
        double gridHeight = (tileLength.get() + gap.get()) * rows() + gap.get() + (2 * padding.get());

        double x = Math.max(0, (canvas.getWidth() - gridWidth) / 2);
        double y = Math.max(0, (canvas.getHeight() - gridHeight) / 2);

        canvas.setZoomBounds(x, y, gridWidth, gridHeight);
        canvas.setZoomArea(x, y, gridWidth, gridHeight);
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
            canvas.redrawPendingProperty().set(true);
        });

        canvas.setOnMouseDragged(ev -> {
            if (!isGameInteractive()) {
                return;
            }

            int row = getRow(ev.getY());
            int col = getCol(ev.getX());

            if (row == -1 || col == -1) {
                hoverCell = null;
                pressedCell = null;
            } else if (!isCellSelectable(row, col)
                    || minefield.getCell(row, col) != pressedCell) {
                pressedCell = null;
                hoverCell = minefield.getCell(row, col);
            }
            canvas.redrawPendingProperty().set(true);
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
            canvas.redrawPendingProperty().set(true);
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
            canvas.redrawPendingProperty().set(true);
        });
        canvas.setOnMouseExited(ev -> {
            hoverCell = null;
            pressedCell = null;
            canvas.redrawPendingProperty().set(true);
        });
        canvas.setOnMouseDragExited(ev -> {
            hoverCell = null;
            pressedCell = null;
            canvas.redrawPendingProperty().set(true);
        });
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
}
