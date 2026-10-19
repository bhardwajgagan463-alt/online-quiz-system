package util;

import javax.swing.SwingUtilities;

/** Multithreading (syllabus): the quiz countdown runs on its own thread. */
public class TimerThread extends Thread {      // inheritance: extends Thread

    /** Interface (syllabus): the screen implements this to receive timer events. */
    public interface TimerListener {
        void onTick(int secondsLeft);
        void onTimeUp();
    }

    private final int totalSeconds;
    private final TimerListener listener;
    private volatile boolean running = true;   // volatile: the UI thread can stop us safely

    public TimerThread(int totalSeconds, TimerListener listener) {
        super("quiz-timer");
        this.totalSeconds = totalSeconds;
        this.listener = listener;
        setDaemon(true);                       // never keeps the application alive
    }

    @Override
    public void run() {
        long endTime = System.currentTimeMillis() + totalSeconds * 1000L;
        int lastShown = -1;

        while (running) {
            long remainingMs = endTime - System.currentTimeMillis();
            if (remainingMs <= 0) {
                break;
            }
            int secondsLeft = (int) Math.ceil(remainingMs / 1000.0);
            if (secondsLeft != lastShown) {
                lastShown = secondsLeft;
                final int shown = secondsLeft;
                // Swing is single-threaded: UI updates must run on the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> listener.onTick(shown));
            }
            try {
                Thread.sleep(200);             // check 5 times a second, update once a second
            } catch (InterruptedException e) {
                return;                        // stopTimer() interrupts us
            }
        }

        if (running) {                         // time really ran out (not stopped by the user)
            running = false;
            SwingUtilities.invokeLater(() -> {
                listener.onTick(0);
                listener.onTimeUp();
            });
        }
    }

    public void stopTimer() {
        running = false;
        interrupt();
    }
}