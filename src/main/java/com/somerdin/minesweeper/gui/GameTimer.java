package com.somerdin.minesweeper.gui;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;

public class GameTimer extends AnimationTimer {
    private BooleanProperty isRunning;
    private long lastTime;
    private LongProperty elapsedTime;

    public GameTimer() {
        isRunning = new SimpleBooleanProperty();
        isRunning.addListener((observable, oldVal, newVal) -> {
//          assert (newVal != isRunning.get());

            if (newVal) {
                start();
            } else {
                stop();
            }
        });

        elapsedTime = new SimpleLongProperty(0);
    }

    @Override
    public void handle(long now) {
        elapsedTime.set(elapsedTime.get() + now - lastTime);
        lastTime = now;
    }

    @Override
    public void start() {
        isRunning.set(true);
        lastTime = System.nanoTime();
        super.start();
    }

    @Override
    public void stop() {
        isRunning.set(false);
        super.stop();
    }

    /* reset timer to zero */
    public void reset() {
        elapsedTime.set(0);
    }

    public BooleanProperty isRunningProperty() {
        return isRunning;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public LongProperty getElapsedTimeProperty() {
        return elapsedTime;
    }

    public int getElapsedTimeSeconds() {
        return (int) (elapsedTime.get() / 1E9);
    }
}
