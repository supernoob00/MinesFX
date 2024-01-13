package com.somerdin.minesweeper.game;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Queue;

public class Minefield {
    private static final int BOMB_CELL = -1;

    private int bombCount;
    private int revealCount;

    private Cell[][] grid;
    private int[][] bombValues; // number of neighbors that contain bomb (0-8, or BOMB_CELL if tile itself is a bomb)

    private boolean firstMove;
    private GameResult result;

    // TODO: ensure minimum row count, col count, bomb ratio
    /* create a randomly-generated minefield of given size and bomb density */
    public Minefield(int rows, int cols, double bombRatio) {
        firstMove = true;
        result = GameResult.IN_PROGRESS;

        int tiles = rows * cols;
        int bombsToPlace = (int) (tiles * bombRatio);

        // set class fields
        grid = new Cell[rows][cols];
        bombValues = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(false, CellState.HIDDEN);

                double rand = Math.random();
                if (bombCount < bombsToPlace && rand <= ((double) bombsToPlace / tiles)) {
                    addBomb(i, j);
                }
            }
        }
    }

    /* create a minefield from text representation */
    public Minefield(File file) {
        firstMove = true;
        result = GameResult.IN_PROGRESS;

        try (BufferedReader in = new BufferedReader(new FileReader(file));
             BufferedReader in2 = new BufferedReader(new FileReader(file));) {
            int rows = 0, cols = 0;

            String line = in.readLine();
            rows++;
            cols = line.length();

            while (in.readLine() != null) {
                rows++;
            }

            grid = new Cell[rows][cols];
            bombValues = new int[rows][cols];

            int c;
            int i = 0, j = 0;
            while ((c = in2.read()) != -1) {
                switch (c) {
                    case '\n': // new row marker
                        j = -1;
                        i++;
                        break;
                    case 'B': // revealed bomb
                        grid[i][j] = new Cell(false, CellState.REVEALED);
                        addBomb(i, j);
                        break;
                    case 'b': // hidden bomb
                        grid[i][j] = new Cell(false, CellState.HIDDEN);
                        addBomb(i, j);
                        break;
                    case '#': // hidden, no bomb
                        grid[i][j] = new Cell(false, CellState.HIDDEN);
                        break;
                    case 'X': // revealed, no bomb
                        grid[i][j] = new Cell(false, CellState.REVEALED);
                        break;
                    default:
                        break;
                }
                j++;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error in reading file");
        }
    }

    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    public GameResult getResult() {
        return result;
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

    public void addBomb(int row, int col) {
        assert !grid[row][col].isBomb();

        grid[row][col].setBomb(true);
        bombValues[row][col] = BOMB_CELL;

        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (validCell(i, j) && bombValues[i][j] != BOMB_CELL){
                    bombValues[i][j]++;
                }
            }
        }
        bombCount++;
    }

    public void chooseCell(int row, int col) {
        if (!validCell(row, col)) {
            throw new IllegalArgumentException("Invalid row and/or col.");
        }

        Cell selected = grid[row][col];

        if (firstMove) {
            if (selected.isBomb()) {
                moveBombToFirstEmpty(row, col);
            }
            revealArea(row, col);
            firstMove = false;
        } else {
            if (selected.isBomb()) {
                // TODO: logic for blow up
                revealAll();
                result = GameResult.GAME_LOST;
            } else {
                selected.setState(CellState.REVEALED);
            }
        }
    }

    private void revealCell(int row, int col) {
        Cell cell = grid[row][col];

        assert cell.getState() != CellState.REVEALED;
        assert !cell.isBomb();

        cell.setState(CellState.REVEALED);
        revealCount--;
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

    private void revealAll() {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                grid[i][j].setState(CellState.REVEALED);
            }
        }
    }

    private void revealArea(int row, int col) {
        assert bombValues[row][col] != -1;

        if (bombValues[row][col] != 0) {
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
                            && (cell = grid[i][j]).getState() != CellState.REVEALED
                            && !cell.isBomb()){
                        revealCell(i, j);

                        if (bombValues[i][j] == 0) {
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

        if (cell.getState() == CellState.HIDDEN) {
            c = '#';
        } else if (cell.getState() == CellState.FLAGGED) {
            c = 'F';
        } else { // cell state is REVEALED
            if (cell.isBomb()) {
                c = 'B';
            } else {
                c = (char) (bombValues[row][col] + '0');
            }
        }
        return c;
    }

    // TODO: remove test main
    public static void main(String[] args) {
        File text = new File("/home/sam/repos/minesweeper/src/main/resources/com/somerdin/minesweeper/mines.txt");
        Minefield minefield1 = new Minefield(text);
        System.out.println(minefield1);
        System.out.println(minefield1);
    }
}