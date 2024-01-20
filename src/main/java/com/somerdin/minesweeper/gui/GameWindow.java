package com.somerdin.minesweeper.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.util.function.Supplier;

public class GameWindow {
    private static final int MIN_SIZE = 4;
    private static final int MAX_SIZE = 80;

    private static final int MIN_BOMB_PERCENT = 1;
    private static final int MAX_BOMB_PERCENT = 99;

    private GameBoard board;
    private BorderPane borderPane;
    private GameTimer timer;

    private Stage parentStage;

    public GameWindow(Stage parent, GameBoard gameBoard) {
        parentStage = parent;

        board = gameBoard;
        timer = new GameTimer();

        timer.bind(board.isTimerRunningProperty());

        borderPane = new BorderPane();
        borderPane.setTop(menuBar());
        borderPane.setCenter(centerPane());
        borderPane.setRight(currentGameInfo());
    }

    public void startGame(int rows, int cols, int percent) {
        if (timer.isStarted()) {
            throw new IllegalStateException("Game is already started.");
        }
        timer.stop();
        timer.reset();
        board.startNewGame(rows, cols, percent);
    }

    public BorderPane getBorderPane() {
        return borderPane;
    }

    /* Info about current game, shown directly next to grid */
    private VBox currentGameInfo() {
        Text timerText = gameTimer();
        VBox vBox = new VBox();

        Text flagsPlacedText = new Text();
        Text separator = new Text("/");
        Text bombCountText = new Text();

        flagsPlacedText.textProperty().bind(
                board.flagsPlacedProperty().asString());
        bombCountText.textProperty().bind(
                board.bombCountProperty().asString());

        HBox flagInfoContainer = new HBox(flagsPlacedText, separator, bombCountText);

        vBox.getChildren().addAll(timerText, flagInfoContainer);
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
                timer.getElapsedTimeProperty(), converter);
        return text;
    }

    private MenuBar menuBar() {
        Menu optionsMenu = new Menu("Options");
        optionsMenu.getItems().add(difficultyMenu());

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
        vBox.setHgap(30);
        vBox.setVgap(30);
        vBox.setPadding(new Insets(50, 50, 50, 50));

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
            startGame(rowSpinner.getValue(),
                    colSpinner.getValue(),
                    bombSpinner.getValue());
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
                    canvas.resize(newWidth.doubleValue(), canvas.getHeight());
                    board.draw();
                });
        centerPane.heightProperty().addListener(
                (o, oldVal, newHeight) -> {
                    canvas.resize(canvas.getWidth(), newHeight.doubleValue());
                    board.draw();
                });

        centerPane.resize(500, 500);
        return centerPane;
    }
}
