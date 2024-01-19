package com.somerdin.minesweeper.gui;

import javafx.animation.AnimationTimer;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ObservableBooleanValue;

public class GameTimer extends AnimationTimer {
    private boolean isStarted;
    private long lastTime;
    private LongProperty elapsedTime;

    public GameTimer() {
        elapsedTime = new SimpleLongProperty(0);
    }

    @Override
    public void handle(long now) {
        elapsedTime.set(elapsedTime.get() + now - lastTime);
        lastTime = now;
    }

    @Override
    public void start() {
        isStarted = true;
        lastTime = System.nanoTime();
        super.start();
    }

    @Override
    public void stop() {
        isStarted = false;
        super.stop();
    }

    /* reset timer to zero */
    public void reset() {
        elapsedTime.set(0);
    }

    public boolean isStarted() {
        return isStarted;
    }

    public LongProperty getElapsedTimeProperty() {
        return elapsedTime;
    }

    public int getElapsedTimeSeconds() {
        return (int) (elapsedTime.get() / 1E9);
    }

    /**
     * make timer start or stop automatically when observed boolean value
     * is set to true or false, respectively
     * @param boolVal
     */
    public void bind(ObservableBooleanValue boolVal) {
        boolVal.addListener((observable, oldVal, newVal) -> {
            assert (newVal != isStarted);

            if (newVal) {
                start();
            } else {
                stop();
            }
        });
    }
}
