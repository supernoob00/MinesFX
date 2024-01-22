package com.somerdin.minesweeper;

import com.somerdin.minesweeper.game.Minefield;
import com.somerdin.minesweeper.gui.BoardImages;
import com.somerdin.minesweeper.gui.GameBoard;
import com.somerdin.minesweeper.gui.GameWindow;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;

public class MinesweeperApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Minefield minefield = new Minefield(new File("/home/sam/repos/minesweeper/src/main/resources/mines.txt"));
        GameBoard gameBoard = new GameBoard(minefield);

        GameWindow gameWindow = new GameWindow(stage, gameBoard);

        Scene scene = new Scene(gameWindow.getBorderPane());

        stage.setTitle("Minesweeper");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.setUserAgentStylesheet(MinesweeperApplication.class.getResource("/themes/cupertino-light.css").toExternalForm());
        Application.launch();
    }
}