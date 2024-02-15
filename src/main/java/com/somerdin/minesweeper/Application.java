package com.somerdin.minesweeper;

import com.somerdin.minesweeper.game.Difficulty;
import com.somerdin.minesweeper.game.Minefield;
import com.somerdin.minesweeper.gui.GameBoard;
import com.somerdin.minesweeper.gui.GameTimer;
import com.somerdin.minesweeper.gui.GameWindow;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.prefs.Preferences;

public class Application extends javafx.application.Application {
    public static final String ROOT = System.getProperty("user.home");
    public static final String GAME_PATH =  ROOT + "/minesweeper";
    public static final String SAVED_GAME_PATH = GAME_PATH + "/game_info.mines";

    public static final Preferences PREFERENCES = Preferences.userRoot().node(Application.class.getName());

    public static final String SAVED_BOARD_PROP = "saved_board";

    public static final String DARK_MODE_PREF = "dark_mode";
    public static final String DIFFICULTY_PREF = "difficulty";
    public static final String FULL_SCREEN_PREF = "full_screen";

    public static final String EASY_DIFFICULTY_VALUE = "EASY";
    public static final String MEDIUM_DIFFICULTY_VALUE = "MEDIUM";
    public static final String HARD_DIFFICULTY_VALUE = "HARD";
    public static final String CUSTOM_DIFFICULTY_VALUE = "CUSTOM";

    @Override
    public void start(Stage stage) throws IOException {
        File gameDir = new File(GAME_PATH);
        if (!gameDir.exists()) {
            System.out.println(gameDir.mkdir());
        }

        File savedGame = new File(SAVED_GAME_PATH);
        savedGame.createNewFile();

        String difficultyPref = PREFERENCES.get(DIFFICULTY_PREF, Difficulty.EASY.toString());
        Minefield minefield;
        if (difficultyPref.equals(Difficulty.EASY.toString())) {
            System.out.println("EASY");
            minefield = new Minefield(Difficulty.EASY);
        } else if (difficultyPref.equals(Difficulty.MEDIUM.toString())) {
            minefield = new Minefield(Difficulty.MEDIUM);
        } else if (difficultyPref.equals(Difficulty.HARD.toString())) {
            minefield = new Minefield(Difficulty.HARD);
        } else {
            System.out.println("FROM SAVED GAME");
            if (savedGame.canRead()) {
                minefield = Minefield.MinefieldSerializer.fromFile(savedGame);
            } else {
                minefield = new Minefield(Difficulty.EASY);
            }
        }

        GameTimer timer = new GameTimer();
        GameBoard gameBoard = new GameBoard(minefield, timer);

        GameWindow gameWindow = new GameWindow(stage, gameBoard, timer);

        Scene scene = new Scene(gameWindow.getRootNode());

        stage.setTitle("Minesweeper");
        stage.setScene(scene);
        stage.setOnCloseRequest(ev -> {
            if (savedGame.canWrite()) {
                try {
                    Minefield.MinefieldSerializer.writeToFile(savedGame, minefield);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        stage.show();
    }

    public static void main(String[] args) {
        javafx.application.Application.setUserAgentStylesheet(GameWindow.LIGHT_MODE_URL);
        javafx.application.Application.launch();
    }
}