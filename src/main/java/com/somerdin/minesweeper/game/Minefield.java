package com.somerdin.minesweeper.game;

import javafx.beans.property.*;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Queue;

public class Minefield {
    private static final int BOMB_CELL = -1;

    // controls whether first tile clicked always has zero neighbors
    private boolean isStartZero = true;

    private final IntegerProperty bombCount = new SimpleIntegerProperty();
    private final IntegerProperty flaggedCount = new SimpleIntegerProperty();
    private final IntegerProperty revealCount = new SimpleIntegerProperty();
    private final BooleanProperty firstMove = new SimpleBooleanProperty(true);
    private final ObjectProperty<GameResult> result = new SimpleObjectProperty<>();

    private Cell[][] grid;
    private int[][] neighborCounts; // number of neighbors that contain bomb (0-8, or BOMB_CELL if tile itself is a bomb)

    private int percentBomb;

    public Minefield(int rows,
                     int cols,
                     int percent,
                     boolean startZero,
                     int fillBlocks) {

    }

    /* create a randomly-generated minefield of given size and bomb density */
    public Minefield(Difficulty difficulty) {
        startNewGame(difficulty);
    }

    private Minefield() {

    }

    public void startNewGame(Difficulty difficulty) {
        bombCount.set(0);
        revealCount.set(0);
        firstMove.set(true);
        flaggedCountProperty().set(0);
        result.set(GameResult.IN_PROGRESS);

        percentBomb = difficulty.bombPercent();

        int tiles = difficulty.rows() * difficulty.cols();
        int bombsToPlace = (int) (tiles * (percentBomb / 100D));
        System.out.println("BOMBS TO PLACE " + bombsToPlace);
        bombsToPlace = Math.clamp(bombsToPlace, 1, tiles - 9);

        // set class fields
        grid = new Cell[difficulty.rows()][difficulty.cols()];
        neighborCounts = new int[difficulty.rows()][difficulty.cols()];

        // mark bomb neighbor values matrix with bombs, then shuffle
        for (int i = 0; i < bombsToPlace; i++) {
            int row = i / difficulty.cols();
            int col = i % difficulty.cols();

            neighborCounts[row][col] = BOMB_CELL;
        }
        shuffle2DArray(neighborCounts);

        // fill grid with either empty or bomb cells, using neighbor values
        // matrix as the guide
        for (int i = 0; i < difficulty.rows(); i++) {
            for (int j = 0; j < difficulty.cols(); j++) {
                grid[i][j] = new Cell(CellStatus.HIDDEN);

                if (neighborCounts[i][j] == BOMB_CELL) {
                    addBomb(i, j);
                }
            }
        }
    }

    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    public ObjectProperty<GameResult> gameResultProperty() {
        return result;
    }

    public GameResult getGameResult() {
        return result.get();
    }

    public IntegerProperty bombCountProperty() {
        return bombCount;
    }

    public int getBombCount() {
        return bombCount.get();
    }

    public IntegerProperty flaggedCountProperty() {
        return flaggedCount;
    }

    public int getFlaggedCount() {
        return flaggedCount.get();
    }

    public BooleanProperty firstMoveProperty() {
        return firstMove;
    }

    public boolean getFirstMove() {
        return firstMove.get();
    }

    public int neighborCount(int row, int col) {
        int bombCount = 0;

        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (validCell(i, j) && grid[i][j].isBomb()) {
                    bombCount++;
                }
            }
        }
        return bombCount;
    }

    public int rowCount() {
        return grid.length;
    }

    public int colCount() {
        return grid[0].length;
    }

    public int getPercentBomb() {
        return percentBomb;
    }

    public Difficulty getDifficulty() {
        return new Difficulty(rowCount(), colCount(), percentBomb);
    }

    public void addBomb(int row, int col) {
        assert !grid[row][col].isBomb();

        grid[row][col].setBomb(true);
        neighborCounts[row][col] = BOMB_CELL;

        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (validCell(i, j) && neighborCounts[i][j] != BOMB_CELL){
                    neighborCounts[i][j]++;
                }
            }
        }
        bombCount.set(bombCount.get() + 1);
    }

    public void removeBomb(int row, int col) {
        assert grid[row][col].isBomb();

        neighborCounts[row][col] = 1;

        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (validCell(i, j)){
                    if (neighborCounts[i][j] != BOMB_CELL) {
                        neighborCounts[i][j]--;
                    } else {
                        neighborCounts[row][col]++;
                    }
                }
            }
        }

        grid[row][col].setBomb(false);
        bombCount.set(bombCount.get() - 1);
    }

    public void chooseCell(int row, int col) {
        if (!validCell(row, col)) {
            throw new IllegalArgumentException("Invalid row and/or col.");
        }

        Cell selected = grid[row][col];

        // TODO: find cleaner way to express logic
        if (firstMove.get()) {
            if (isStartZero) {
                moveStartingNeighbors(row, col);
            } else if (selected.isBomb()) {
                moveBombToFirstEmpty(row, col);
            }
            firstMove.set(false);
        } else if (selected.isBomb()) {
            revealAll();
            selected.setBombStatus(BombStatus.DETONATED);
            result.set(GameResult.GAME_LOST);
            return;
        }

        revealArea(row, col);
        if (revealCount.get() == rowCount() * colCount() - bombCount.get()) {
            flagAllBombs();
            result.set(GameResult.GAME_WON);
        }
    }

    public void toggleFlag(int row, int col) {
        assert grid[row][col].getCellStatus() != CellStatus.REVEALED;

        Cell cell = grid[row][col];
        switch (cell.getCellStatus()) {
            case HIDDEN -> {
                cell.setCellStatus(CellStatus.FLAGGED);
                flaggedCount.set(flaggedCount.get() + 1);
            }
            case FLAGGED -> {
                cell.setCellStatus(CellStatus.FLAGGED_QUESTION);
                flaggedCount.set(flaggedCount.get() - 1);
            }
            case FLAGGED_QUESTION -> cell.setCellStatus(CellStatus.HIDDEN);
            default -> throw new IllegalStateException("Can't toggle flag for this cell");
        }
    }

    @Override
    public String toString() {
        StringBuilder board = new StringBuilder();

        for (int i = 0; i < rowCount(); i++) {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < colCount(); j++) {
                row.append(getCellChar(i, j));
            }
            board.append(row);
            board.append(System.lineSeparator());
        }
        return board.toString();
    }

    private void revealCell(int row, int col) {
        Cell cell = grid[row][col];

        assert cell.getCellStatus() != CellStatus.REVEALED;
        assert !cell.isBomb();

        if (cell.getCellStatus() == CellStatus.FLAGGED) {
            flaggedCount.set(flaggedCount.get() - 1);
        }

        cell.setCellStatus(CellStatus.REVEALED);
        revealCount.set(revealCount.get() + 1);
    }

    /* returns true if cells are neighbors; cells cannot be the same */
    private boolean areNeighbors(int i1, int j1, int i2, int j2) {
        int rowDiff = i1 - i2;
        int colDiff = j1 - j2;
        return rowDiff >= -1 && rowDiff <= 1 && colDiff >= -1 && colDiff <= 1;
    }

    private void flagAllBombs() {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                Cell cell = grid[i][j];
                if (cell.isBomb()) {
                    cell.setCellStatus(CellStatus.FLAGGED);
                    flaggedCount.set(flaggedCount.get() + 1);
                }
            }
        }
    }

    /* moves bomb at specified cell to first empty cell from top left */
    private void moveBombToFirstEmpty(int row, int col) {
        assert grid[row][col].isBomb();

        outer:
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                if (!grid[i][j].isBomb()) {
                    grid[row][col].setBomb(false);
                    grid[i][j].setBomb(true);

                    break outer;
                }
            }
        }
    }

    private void setRandomNonNeighborCellBomb(int row, int col) {
        int i = (int) (Math.random() * rowCount());
        int j = (int) (Math.random() * colCount());

        int count = rowCount() * colCount();

        for (int k = 0; k < count; k++) {
            if (!grid[i][j].isBomb()
                    && !areNeighbors(row, col, i, j)) {
                addBomb(i, j);
                break;
            }
            j = (j + 1) % colCount();
            if (j == 0) {
                i = (i + 1) % rowCount();
            }
        }
    }

    private void moveStartingNeighbors(int row, int col) {
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (validCell(i, j) && grid[i][j].isBomb()){
                    removeBomb(i, j);
                    setRandomNonNeighborCellBomb(row, col);
                }
            }
        }
    }

    private void revealAll() {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                revealCell(i, j);
            }
        }
    }

    private void revealArea(int row, int col) {
        assert neighborCounts[row][col] != -1;

        if (neighborCounts[row][col] != 0) {
            revealCell(row, col);
            return;
        }

        Queue<Integer> rowQueue = new ArrayDeque<>();
        Queue<Integer> colQueue = new ArrayDeque<>();

        rowQueue.add(row);
        colQueue.add(col);

        while (!rowQueue.isEmpty()) {
            row = rowQueue.remove();
            col = colQueue.remove();

            for (int i = row - 1; i <= row + 1; i++) {
                for (int j = col - 1; j <= col + 1; j++) {
                    Cell cell;
                    if (validCell(i, j)
                            && (cell = grid[i][j]).getCellStatus() != CellStatus.REVEALED
                            && !cell.isBomb()){
                        revealCell(i, j);

                        if (neighborCounts[i][j] == 0) {
                            rowQueue.add(i);
                            colQueue.add(j);
                        }
                    }
                }
            }
        }
    }

    private boolean validCell(int row, int col) {
        return row >= 0 && row < rowCount() && col >= 0 && col < colCount();
    }

    private char getCellChar(int row, int col) {
        Cell cell = grid[row][col];
        char c;

        if (cell.getCellStatus() == CellStatus.HIDDEN) {
            c = '#';
        } else if (cell.getCellStatus() == CellStatus.FLAGGED) {
            c = 'F';
        } else { // cell state is REVEALED
            if (cell.isBomb()) {
                c = 'B';
            } else {
                c = (char) (neighborCounts[row][col] + '0');
            }
        }
        return c;
    }

    private static void shuffle2DArray(int[][] arr) {
        int m = arr.length;
        int n = arr[0].length;

        int randRowRange, randColRange;
        randRowRange = m;

        for (int i = 0; i < m; i++) {
            randColRange = n;
            for (int j = 0; j < n; j++) {
                int randRow = i + (int) (Math.random() * randRowRange);
                int randCol = j + (int) (Math.random() * randColRange);

                int temp = arr[i][j];
                arr[i][j] = arr[randRow][randCol];
                arr[randRow][randCol] = temp;

                randColRange--;
            }
            randRowRange--;
        }
    }

    /* Utility class for serializing and deserializing a Minefield */
    public static class MinefieldSerializer {
        private static final char ROW_DIVIDER = '\n';
        private static final char HIDDEN_EMPTY = '#';
        private static final char REVEALED_EMPTY = 'X';
        private static final char FLAGGED_EMPTY  = 'f';
        private static final char QUESTION_EMPTY = 'q';
        private static final char HIDDEN_BOMB = 'b';
        private static final char REVEALED_BOMB = 'B';
        private static final char FLAGGED_BOMB = 'F';
        private static final char QUESTION_BOMB = 'Q';
        private static final char DETONATED_BOMB = 'E';

        /* create a minefield from text representation */
        public static Minefield fromFile(File file) throws IOException {
            Minefield minefield = new Minefield();

            minefield.firstMove.set(true);
            minefield.result.set(GameResult.IN_PROGRESS);

            // use two readers; one to determine dimensions of board to create,
            // and other to determine how to fill the cells
            try (BufferedReader dimensionReader = new BufferedReader(new FileReader(file));
                 BufferedReader cellReader = new BufferedReader(new FileReader(file));) {
                int rows = 0, cols = 0;

                String line = dimensionReader.readLine();
                rows++;
                cols = line.length();

                while (dimensionReader.readLine() != null) {
                    rows++;
                }

                minefield.grid = new Cell[rows][cols];
                minefield.neighborCounts = new int[rows][cols];

                int c;
                int i = 0, j = 0;
                while ((c = cellReader.read()) != -1) {
                    switch (c) {
                        case ROW_DIVIDER -> {
                            j = -1;
                            i++;
                        }
                        case REVEALED_BOMB -> {
                            minefield.grid[i][j] = new Cell(CellStatus.REVEALED);
                            minefield.addBomb(i, j);
                        }
                        case HIDDEN_BOMB -> {
                            minefield.grid[i][j] = new Cell(CellStatus.HIDDEN);
                            minefield.addBomb(i, j);
                        }
                        case FLAGGED_BOMB -> {
                            minefield.grid[i][j] = new Cell(CellStatus.FLAGGED);
                            minefield.addBomb(i, j);
                        }
                        case QUESTION_BOMB -> {
                            minefield.grid[i][j] = new Cell(CellStatus.FLAGGED_QUESTION);
                            minefield.addBomb(i, j);
                        }
                        case DETONATED_BOMB -> {
                            Cell cell = new Cell(CellStatus.REVEALED);
                            minefield.grid[i][j] = cell;
                            minefield.addBomb(i, j);
                            cell.setBombStatus(BombStatus.DETONATED);
                        }
                        case HIDDEN_EMPTY -> minefield.grid[i][j] = new Cell(CellStatus.HIDDEN);
                        case REVEALED_EMPTY -> minefield.grid[i][j] = new Cell(CellStatus.REVEALED);
                        case FLAGGED_EMPTY -> minefield.grid[i][j] = new Cell(CellStatus.FLAGGED);
                        case QUESTION_EMPTY -> minefield.grid[i][j] = new Cell(CellStatus.FLAGGED_QUESTION);
                        default -> throw new IllegalStateException("Unexpected character");
                    }
                    j++;
                }
            }
            return minefield;
        }

        public static void writeToFile(File file, Minefield minefield) throws IOException {
            Cell[][] grid = minefield.grid;

            try (FileWriter out = new FileWriter(file)) {
                for (int i = 0; i < grid.length; i++) {
                    for (int j = 0; j < grid[0].length; j++) {
                        Cell cell = grid[i][j];
                        switch (cell.getCellStatus()) {
                            case HIDDEN -> {
                                switch (cell.getBombStatus()) {
                                    case NONE -> out.append(HIDDEN_EMPTY);
                                    case UNDETONATED -> out.append(HIDDEN_BOMB);
                                    default -> throw new IllegalArgumentException("Bomb should not be detonated.");
                                }
                            }
                            case REVEALED -> {
                                switch (cell.getBombStatus()) {
                                    case NONE -> out.append(REVEALED_EMPTY);
                                    case UNDETONATED -> out.append(REVEALED_BOMB);
                                    case DETONATED -> out.append(DETONATED_BOMB);
                                }
                            }
                            case FLAGGED -> {
                                switch (cell.getBombStatus()) {
                                    case NONE -> out.append(FLAGGED_EMPTY);
                                    case UNDETONATED -> out.append(FLAGGED_BOMB);
                                    default -> throw new IllegalArgumentException("Bomb should not be detonated.");
                                }
                            }
                            case FLAGGED_QUESTION -> {
                                switch (cell.getBombStatus()) {
                                    case NONE -> out.append(QUESTION_EMPTY);
                                    case UNDETONATED -> out.append(QUESTION_BOMB);
                                    default -> throw new IllegalArgumentException("Bomb should not be detonated.");
                                }
                            }
                        }
                    }
                    out.append(ROW_DIVIDER);
                }
            }
        }
    }
}
