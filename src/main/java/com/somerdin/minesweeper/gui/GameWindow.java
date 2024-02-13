package com.somerdin.minesweeper.gui;

import com.somerdin.minesweeper.Application;
import com.somerdin.minesweeper.game.Difficulty;
import com.somerdin.minesweeper.style.WindowGraphic;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.util.function.Supplier;

public class GameWindow {
    public static final String LIGHT_MODE_URL = Application.class.getResource("/themes/cupertino-light.css").toExternalForm();
    public static final String DARK_MODE_URL = Application.class.getResource("/themes/dracula.css").toExternalForm();

    private final BooleanProperty darkMode = new SimpleBooleanProperty();

    private final Pane rootNode;
    private final Stage parentStage;
    private final GameBoard gameBoard;
    private final GameTimer gameTimer;

    public GameWindow(Stage parentStage, GameBoard gameBoard, GameTimer gameTimer) {
        this.parentStage = parentStage;
        this.gameBoard = gameBoard;
        this.gameTimer = gameTimer;

        // top of root BorderPane is the menu bar and its center is everything else
        BorderPane root = new BorderPane();
        root.setTop(menuBar());

        // everything that's not the top menu bar
        BorderPane content = new BorderPane();
        content.setTop(toolBar());
        content.setCenter(centerPane());
        content.setBottom(currentGameInfo());
        root.setCenter(content);

        this.rootNode = root;

        setTheme(Application.PREFERENCES.getBoolean(Application.DARK_MODE_PREF, false));
    }

    /* start new game with specified settings */
    public void startGame(Difficulty difficulty) {
        gameTimer.stop();
        gameTimer.reset();
        gameTimer.pausedProperty().set(false);
        gameTimer.isRunningProperty().set(false);
        gameBoard.startNewGame(difficulty);
    }

    public void startGameAfterConfirmation(Difficulty difficulty) {
        if (gameBoard.inProgressProperty().get()) {
            Alert alert = new Alert(
                    Alert.AlertType.NONE,
                    "Are you sure you want to start a new game?",
                    ButtonType.NO,
                    ButtonType.OK);
            alert.setTitle("Confirmation");
            alert.showAndWait().ifPresent((response) -> {
                if (response == ButtonType.OK) {
                    startGame(difficulty);
                }
            });
        } else {
            startGame(difficulty);
        }
    }

    public Pane getRootNode() {
        return rootNode;
    }

    public void setTheme(boolean dark) {
        if (!dark) {
            javafx.application.Application.setUserAgentStylesheet(LIGHT_MODE_URL);
        } else {
            javafx.application.Application.setUserAgentStylesheet(DARK_MODE_URL);
        }
        darkMode.set(dark);
    }

    private ToolBar toolBar() {
        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(restartButton(), pauseButton());
        return toolBar;
    }

    /* Info about current game, shown directly next to grid */
    private HBox currentGameInfo() {
        // game timer text
        HBox timerText = gameTimer();

        // container for game time and flag/bomb count text
        HBox textContainer = new HBox(timerText, flagInfo());
        textContainer.setAlignment(Pos.CENTER);
        textContainer.setSpacing(36);

        HBox hBox = new HBox();
        hBox.getChildren().addAll(textContainer);
        hBox.setAlignment(Pos.CENTER);
        hBox.setStyle("-fx-font-size: 24px");
        hBox.setPadding(new Insets(12, 12, 12, 12));
        return hBox;
    }

    private HBox gameTimer() {
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

        ImageView timeIcon = new StaticSVGImage(WindowGraphic.TIME_ICON, 36).getImageView();
        makeImageDarkModeAware(timeIcon);

        HBox hBox = new HBox(timeIcon, text);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(6);
        return hBox;
    }

    private HBox flagInfo() {
        Text flagsPlacedText = new Text();
        Text separator = new Text("/");
        Text bombCountText = new Text();
        flagsPlacedText.textProperty().bind(
                gameBoard.flaggedCountProperty().asString());
        bombCountText.textProperty().bind(
                gameBoard.bombCountProperty().asString());
        HBox flagInfoContainer = new HBox(flagsPlacedText, separator, bombCountText);
        flagInfoContainer.setAlignment(Pos.CENTER);

        ImageView flagIcon = new StaticSVGImage(WindowGraphic.FLAG_ICON, 32).getImageView();
        makeImageDarkModeAware(flagIcon);

        HBox hBox = new HBox(flagIcon, flagInfoContainer);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(6);
        return hBox;
    }

    private Button pauseButton() {
        Button pauseButton = new Button("Pause");

        pauseButton.disableProperty().bind(gameBoard.inProgressProperty().not());

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
        Button restartButton = new Button("New Game");
        restartButton.setOnAction(ev -> startGameAfterConfirmation(gameBoard.getDifficulty()));
        restartButton.setFocusTraversable(false);
        return restartButton;
    }

    private MenuBar menuBar() {
        CheckMenuItem changeTheme = new CheckMenuItem("Dark Mode");
        changeTheme.setOnAction(ev -> {
            setTheme(!darkMode.get());
            Application.PREFERENCES.putBoolean(Application.DARK_MODE_PREF, darkMode.get());
        });

        Menu optionsMenu = new Menu("Options");

        optionsMenu.getItems().addAll(difficultyMenu(), changeTheme);

        Menu gameMenu = new Menu("Game");

        MenuBar menuBar = new MenuBar(optionsMenu, gameMenu);

        return menuBar;
    }

    private RadioMenuItem difficultyMenuItem(String name, Difficulty difficulty) {
        RadioMenuItem item = new RadioMenuItem(name);
        item.setOnAction(ev -> {
            startGameAfterConfirmation(difficulty);
            Application.PREFERENCES.put(Application.DIFFICULTY_PREF, difficulty.toString());
        });
        return item;
    }

    private Menu difficultyMenu() {
        Menu difficultyMenu = new Menu("Difficulty");

        // create radio menu items group
        RadioMenuItem easyMenuItem = difficultyMenuItem("Easy", Difficulty.EASY);
        RadioMenuItem mediumMenuItem = difficultyMenuItem("Medium", Difficulty.MEDIUM);
        RadioMenuItem hardMenuItem = difficultyMenuItem("Hard", Difficulty.HARD);
        RadioMenuItem customMenuItem = new RadioMenuItem("Custom...");

        ToggleGroup difficultyGroup = new ToggleGroup();
        difficultyGroup.getToggles().addAll(
                easyMenuItem, mediumMenuItem, hardMenuItem, customMenuItem);

        String prefDifficulty = Application.PREFERENCES.get(Application.DIFFICULTY_PREF, Difficulty.EASY.toString());
        if (prefDifficulty.equals(Difficulty.EASY.toString())) {
            difficultyGroup.selectToggle(easyMenuItem);
        } else if (prefDifficulty.equals(Difficulty.MEDIUM.toString())) {
            difficultyGroup.selectToggle(mediumMenuItem);
        } else if (prefDifficulty.equals(Difficulty.HARD.toString())) {
            difficultyGroup.selectToggle(hardMenuItem);
        }

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
                formatterFactory.get(), Difficulty.MIN_ROWS, Difficulty.MAX_ROWS);
        Spinner<Integer> colSpinner = integerSpinner(
                formatterFactory.get(), Difficulty.MIN_COLS, Difficulty.MAX_COLS);
        Spinner<Integer> bombSpinner = integerSpinner(
                formatterFactory.get(), Difficulty.MIN_BOMB_PERCENT, Difficulty.MAX_BOMB_PERCENT
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
        ZoomCanvas canvas = gameBoard.getCanvas();

        StackPane centerPane = new StackPane(canvas);
//        centerPane.setStyle("-fx-border-color: beige; -fx-border-width: 6px");
        centerPane.setAlignment(Pos.CENTER);

        parentStage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
            canvas.redrawPendingProperty().set(true);
        });
        return centerPane;
    }

    private void makeImageDarkModeAware(ImageView img) {
        darkMode.addListener((observable, oldValue, newValue) -> {
            ColorAdjust adjust = new ColorAdjust();
            if (newValue) {
                adjust.setBrightness(1);
            } else {
                adjust.setBrightness(0);
            }
            img.setEffect(adjust);
        });
    }
}
