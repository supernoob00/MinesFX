package com.somerdin.minesweeper.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;

import java.io.File;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);

        // text formatter to only allow numeric inputs to spinner fields
        // TODO: add value binding, which is the integer value of the string
        Supplier<TextFormatter<String>> formatterFactory = () -> {
            return new TextFormatter<>(change -> {
                if (change.getControlNewText().matches("[0-9]*")) {
                    return change;
                }
                return null;
            });
        };

        Spinner<Integer> rowSpinner = new Spinner<>(1, 99, 8);
        rowSpinner.setEditable(true);
        rowSpinner.getEditor().setTextFormatter(formatterFactory.get());

        Spinner<Integer> colSpinner = new Spinner<>(1, 99, 8);
        colSpinner.setEditable(true);
        colSpinner.getEditor().setTextFormatter(formatterFactory.get());

        Button startButton = new Button("New Game");
        startButton.setOnAction(ev -> {
            board.startNewGame(rowSpinner.getValue(), colSpinner.getValue());
        });

        vBox.getChildren().addAll(
                rowSpinner,
                colSpinner,
                startButton
        );

        Stage customDifficultyOptions = new Stage();
        customDifficultyOptions.setTitle("Custom Difficulty");
        customDifficultyOptions.setAlwaysOnTop(true);
        customDifficultyOptions.setScene(new Scene(vBox));
        return customDifficultyOptions;
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
