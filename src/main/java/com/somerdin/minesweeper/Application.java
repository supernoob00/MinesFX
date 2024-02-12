package com.somerdin.minesweeper;

import com.somerdin.minesweeper.game.Difficulty;
import com.somerdin.minesweeper.game.Minefield;
import com.somerdin.minesweeper.gui.GameBoard;
import com.somerdin.minesweeper.gui.GameTimer;
import com.somerdin.minesweeper.gui.GameWindow;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

public class Application extends javafx.application.Application {
    public static final Preferences PREFERENCES = Preferences.userRoot().node(Application.class.getName());

    public static final String SAVED_BOARD_PREF = "saved_board";
    public static final String DARK_MODE_PREF = "dark_mode";
    public static final String DIFFICULTY_PREF = "difficulty";
    public static final String FULL_SCREEN_PREF = "full_screen";

    @Override
    public void start(Stage stage) throws IOException {
        String difficultyPref = PREFERENCES.get(DIFFICULTY_PREF, Difficulty.EASY.toString());

        Minefield minefield;

        if (difficultyPref.equals(Difficulty.EASY.toString())) {
            minefield = new Minefield(Difficulty.EASY);
        } else if (difficultyPref.equals(Difficulty.MEDIUM.toString())) {
            minefield = new Minefield(Difficulty.MEDIUM);
        } else if (difficultyPref.equals(Difficulty.HARD.toString())) {
            minefield = new Minefield(Difficulty.HARD);
        } else {
            minefield = new Minefield(Difficulty.EASY);
        }

        GameTimer timer = new GameTimer();
        GameBoard gameBoard = new GameBoard(minefield, timer);

        GameWindow gameWindow = new GameWindow(stage, gameBoard, timer);

        Scene scene = new Scene(gameWindow.getBorderPane());

        stage.setTitle("Minesweeper");
        stage.setScene(scene);
        stage.setOnCloseRequest(ev -> {

        });
        stage.show();
    }

    public static void main(String[] args) {
        javafx.application.Application.setUserAgentStylesheet(GameWindow.LIGHT_MODE_URL);
        javafx.application.Application.launch();
    }
}