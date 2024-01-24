package com.somerdin.minesweeper;

import com.somerdin.minesweeper.game.Minefield;
import com.somerdin.minesweeper.gui.BoardImages;
import com.somerdin.minesweeper.gui.GameBoard;
import com.somerdin.minesweeper.gui.GameTimer;
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
        GameTimer timer = new GameTimer();
        GameBoard gameBoard = new GameBoard(minefield, timer);

        GameWindow gameWindow = new GameWindow(stage, gameBoard, timer);

        Scene scene = new Scene(gameWindow.getBorderPane());

        stage.setTitle("Minesweeper");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Application.setUserAgentStylesheet(GameWindow.LIGHT_MODE);
        Application.launch();
    }
}