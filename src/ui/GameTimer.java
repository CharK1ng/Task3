package ui;

import javax.swing.*;

public class GameTimer {
    private Timer timer;
    private int remainingTimeMillis; // Оставшееся время в миллисекундах

    public GameTimer(int delayMillis, Runnable onTimeout) {
        remainingTimeMillis = delayMillis;
        timer = new Timer(1000, e -> { // Обновление каждую секунду
            remainingTimeMillis -= 1000;
            if (remainingTimeMillis <= 0) {
                onTimeout.run();
                timer.stop();
            }
        });
        timer.setRepeats(true);
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public int getRemainingTimeMillis() {
        return remainingTimeMillis;
    }

    public String getRemainingTimeFormatted() {
        int minutes = remainingTimeMillis / 60000;
        int seconds = (remainingTimeMillis % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }
}