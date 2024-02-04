module com.somerdin.minesweeper {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.swing;
    requires com.github.weisj.jsvg;

    exports com.somerdin.minesweeper.gui;
    exports com.somerdin.minesweeper.game;
    exports com.somerdin.minesweeper;
    exports com.somerdin.minesweeper.style;
}