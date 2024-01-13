package com.somerdin.minesweeper.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Pane;

public class GameWindow {
    private GameBoard board;
    private BorderPane borderPane;

    public GameWindow(GameBoard gameBoard) {
        board = gameBoard;

        borderPane = new BorderPane();
        borderPane.setTop(menuBar());
        borderPane.setCenter(centerPane());
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    private MenuBar menuBar() {
        Menu optionsMenu = new Menu("Options");
        Menu newGame = new Menu("New Game");

        MenuItem item = new MenuItem("New");
        item.setOnAction(ev -> {
            board.startNewGame();
        });

        newGame.getItems().add(item);

        MenuBar menuBar = new MenuBar(optionsMenu, newGame);
        return menuBar;
    }

    private Pane centerPane() {
        ResizableCanvas canvas = board.getCanvas();

        Pane centerPane = new Pane(canvas);
        centerPane.widthProperty().addListener(
                (o, oldWidth, newWidth) -> {
                    canvas.resize(newWidth.doubleValue(), canvas.getHeight());
                    board.drawResized();
                });
        centerPane.heightProperty().addListener(
                (o, oldVal, newHeight) -> {
                    canvas.resize(canvas.getWidth(), newHeight.doubleValue());
                    board.drawResized();
                });

        centerPane.resize(500, 500);
        return centerPane;
    }
}
