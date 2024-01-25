package com.somerdin.minesweeper.gui;

import com.somerdin.minesweeper.MinesweeperApplication;
import com.somerdin.minesweeper.game.Difficulty;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.util.function.Supplier;

public class GameWindow {
    public static final String LIGHT_MODE = MinesweeperApplication.class.getResource("/themes/cupertino-light.css").toExternalForm();
    public static final String DARK_MODE = MinesweeperApplication.class.getResource("/themes/dracula.css").toExternalForm();

    private static final int MIN_SIZE = 4;
    private static final int MAX_SIZE = 80;

    private static final int MIN_BOMB_PERCENT = 1;
    private static final int MAX_BOMB_PERCENT = 99;

    private BooleanProperty darkMode = new SimpleBooleanProperty();

    private GameBoard board;
    private BorderPane borderPane;
    private GameTimer gameTimer;

    private Stage parentStage;

    public GameWindow(Stage parent, GameBoard gameBoard, GameTimer timer) {
        parentStage = parent;

        board = gameBoard;
        gameTimer = timer;

        // use a border pane as the root node; top is the menu bar and center
        // is everything else
        borderPane = new BorderPane();
        borderPane.setTop(menuBar());

        // everything that's not the top menu bar
        HBox content = new HBox();

        Pane centerPane = centerPane();
        VBox currentGameInfo = currentGameInfo();

        Region leftPadding = new Region();
        leftPadding.prefWidthProperty().bind(currentGameInfo.widthProperty());

        content.getChildren().addAll(leftPadding, centerPane, currentGameInfo);
        HBox.setHgrow(centerPane, Priority.ALWAYS);
        HBox.setHgrow(currentGameInfo, Priority.ALWAYS);

        borderPane.setCenter(content);
    }

    /* start new game with specified settings */
    public void startGame(Difficulty difficulty) {
        gameTimer.stop();
        gameTimer.reset();
        gameTimer.pausedProperty().set(false);
        gameTimer.isRunningProperty().set(false);
        board.startNewGame(difficulty);
    }

    /* start new game with current settings */
    public void startGame() {
        startGame(new Difficulty(
                board.getRowCount(),
                board.getColCount(),
                board.getPercentBomb()));
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    public void toggleTheme() {
        if (darkMode.get()) {
            Application.setUserAgentStylesheet(LIGHT_MODE);
        } else {
            Application.setUserAgentStylesheet(DARK_MODE);
        }
        darkMode.set(!darkMode.get());
    }

    /* Info about current game, shown directly next to grid */
    private VBox currentGameInfo() {
        // game timer text
        Text timerText = gameTimer();

        // container for game time and flag/bomb count text
        VBox textContainer = new VBox(timerText, flagInfo());
        textContainer.setAlignment(Pos.CENTER);

        // container for buttons
        Button pauseButton = pauseButton();
        Button restartButton = restartButton();
        VBox buttonsContainer = new VBox(pauseButton, restartButton);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(textContainer, buttonsContainer);
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.setPadding(new Insets(16, 16, 16, 16));
        return vBox;
    }

    private Text gameTimer() {
        Text text = new Text();

        NumberStringConverter converter = new NumberStringConverter() {
            @Override
            public String toString(Number value) {
                int totalSeconds = (int) (value.longValue() / 1_000_000_000L);
                int minutes = totalSeconds / 60;
                int seconds = totalSeconds % 60;
                String format = "%02d";
                return String.format(format, minutes)
                        + ":"
                        + String.format(format, seconds);
            }

            @Override
            public Number fromString(String value) {
                return null;
            }
        };
        text.textProperty().bindBidirectional(
                gameTimer.getElapsedTimeProperty(), converter);
        return text;
    }

    private HBox flagInfo() {
        Text flagsPlacedText = new Text();
        Text separator = new Text("/");
        Text bombCountText = new Text();

        flagsPlacedText.textProperty().bind(
                board.flaggedCountProperty().asString());
        bombCountText.textProperty().bind(
                board.bombCountProperty().asString());
        HBox flagInfoContainer = new HBox(flagsPlacedText, separator, bombCountText);
        flagInfoContainer.setAlignment(Pos.CENTER);
        return flagInfoContainer;
    }

    private Button pauseButton() {
        Button pauseButton = new Button("Pause");

        pauseButton.disableProperty().bind(board.inProgressProperty().not());

        pauseButton.textProperty().bind(Bindings.createStringBinding(() -> {
            return gameTimer.isRunning() ? "Pause" : "Resume";
        }, gameTimer.isRunningProperty()));

        pauseButton.setOnAction(ev -> {
            if (gameTimer.isPaused()) {
                gameTimer.resume();
            } else {
                gameTimer.pause();
            }
        });
        pauseButton.setFocusTraversable(false);
        return pauseButton;
    }

    private Button restartButton() {
        Button restartButton = new Button("Restart");
        restartButton.visibleProperty().bind(board.isFirstMoveProperty().not());
        restartButton.setOnAction(ev -> {
            if (board.inProgressProperty().get()) {
                Alert alert = new Alert(
                        Alert.AlertType.NONE,
                        "Are you sure you want to start a new game?",
                        ButtonType.NO,
                        ButtonType.OK);
                alert.setTitle("Confirmation");
                alert.showAndWait().ifPresent((response) -> {
                    if (response == ButtonType.OK) {
                        startGame();
                    }
                });
            } else {
                startGame();
            }
        });
        restartButton.setFocusTraversable(false);
        return restartButton;
    }

    private MenuBar menuBar() {
        CheckMenuItem changeTheme = new CheckMenuItem("Dark Mode");
        changeTheme.setOnAction(ev -> {
            toggleTheme();
        });

        Menu optionsMenu = new Menu("Options");
        optionsMenu.getItems().addAll(difficultyMenu(), changeTheme);

        Menu gameMenu = new Menu("Game");

        MenuBar menuBar = new MenuBar(optionsMenu, gameMenu);

        return menuBar;
    }

    private Menu difficultyMenu() {
        Menu difficultyMenu = new Menu("Difficulty");

        // create radio menu items group
        RadioMenuItem easyMenuItem = new RadioMenuItem("Easy");
        easyMenuItem.setSelected(true);
        RadioMenuItem mediumMenuItem = new RadioMenuItem("Medium");
        RadioMenuItem hardMenuItem = new RadioMenuItem("Hard");
        RadioMenuItem customMenuItem = new RadioMenuItem("Custom...");

        ToggleGroup difficultyGroup = new ToggleGroup();
        difficultyGroup.getToggles().addAll(
                easyMenuItem, mediumMenuItem, hardMenuItem, customMenuItem);


        Stage customDifficulty = customDifficultyOptions();
        customMenuItem.setOnAction(ev -> {
            customDifficulty.show();
        });

        // add all items to difficulty menu
        difficultyMenu.getItems().addAll(
                easyMenuItem,
                mediumMenuItem,
                hardMenuItem,
                customMenuItem);

        return difficultyMenu;
    }

    private Stage customDifficultyOptions() {
        // create custom difficulty menu option and popup
        GridPane vBox = new GridPane();
        vBox.setHgap(24);
        vBox.setVgap(24);
        vBox.setPadding(new Insets(36, 36, 36, 36));

        // text formatter to only allow numeric inputs to spinner fields
        // TODO: add value binding, which is the integer value of the string
        Supplier<TextFormatter<Integer>> formatterFactory = () -> {
            return new TextFormatter<>(change -> {
                if (change.getControlNewText().matches("[0-9]*")) {
                    return change;
                }
                return null;
            });
        };

        Spinner<Integer> rowSpinner = integerSpinner(
                formatterFactory.get(), MIN_SIZE, MAX_SIZE);
        Spinner<Integer> colSpinner = integerSpinner(
                formatterFactory.get(), MIN_SIZE, MAX_SIZE);
        Spinner<Integer> bombSpinner = integerSpinner(
                formatterFactory.get(), MIN_BOMB_PERCENT, MAX_BOMB_PERCENT
        );

        Button startButton = new Button("New Game");

        vBox.add(new Label("Rows"), 0, 0);
        vBox.add(rowSpinner, 1, 0);
        vBox.add(new Label("Columns"), 0, 1);
        vBox.add(colSpinner, 1, 1);
        vBox.add(new Label("Percent Mines"), 0, 2);
        vBox.add(bombSpinner, 1, 2);
        vBox.add(startButton, 0, 3);

        Stage customDifficultyOptions = new Stage();
        customDifficultyOptions.setTitle("Custom Difficulty");
        customDifficultyOptions.setAlwaysOnTop(true);
        customDifficultyOptions.setScene(new Scene(vBox));
        customDifficultyOptions.initOwner(parentStage);
        customDifficultyOptions.initModality(Modality.WINDOW_MODAL);

        startButton.setOnAction(ev -> {
            customDifficultyOptions.close();

            Difficulty difficulty = new Difficulty(
                    rowSpinner.getValue(),
                    colSpinner.getValue(),
                    bombSpinner.getValue());
            startGame(difficulty);
        });

        return customDifficultyOptions;
    }

    private Spinner<Integer> integerSpinner(
            TextFormatter<Integer> formatter,
            int min,
            int max) {
        Spinner<Integer> spinner = new Spinner<>(1, 99, 8);
        spinner.setEditable(true);
        spinner.getEditor().setTextFormatter(formatter);
        spinner.getEditor().setPrefColumnCount(4);
        return spinner;
    }

    private Pane centerPane() {
        ResizableCanvas canvas = board.getCanvas();

        Pane centerPane = new Pane(canvas);
        centerPane.widthProperty().addListener(
                (o, oldWidth, newWidth) -> {
                    double newSize = Math.min(newWidth.doubleValue(), centerPane.getHeight());
                    canvas.resize(newSize, newSize);
                    board.draw();
                });
        centerPane.heightProperty().addListener(
                (o, oldVal, newHeight) -> {
                    double newSize = Math.min(newHeight.doubleValue(), centerPane.getWidth());
                    canvas.resize(newSize, newSize);
                    board.draw();
                });

        centerPane.resize(500, 500);
        return centerPane;
    }
}
