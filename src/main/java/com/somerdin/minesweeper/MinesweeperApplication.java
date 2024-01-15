package com.somerdin.minesweeper;

import com.somerdin.minesweeper.game.Minefield;
import com.somerdin.minesweeper.gui.GameBoard;
import com.somerdin.minesweeper.gui.GameWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MinesweeperApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Minefield minefield = new Minefield(new File("/home/sam/repos/minesweeper/src/main/resources/mines.txt"));
        GameBoard gameBoard = new GameBoard(minefield);
        GameWindow gameWindow = new GameWindow(gameBoard);

        Scene scene = new Scene(gameWindow.getBorderPane());

        stage.setTitle("Minesweeper");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch();
    }
}