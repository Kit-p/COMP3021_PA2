package castle.comp3021.assignment.gui;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Timer for counting time elapsed
 */
public class DurationTimer {
    /**
     * default time in seconds during each round
     */
    private static int defaultEachRound = 30;

    @NotNull
    private Timer flowTimer = new Timer(true);


    private final List<Runnable> onTickCallbacks = new ArrayList<>();

    private final List<Runnable> onTimeUpCallbacks = new ArrayList<>();

    /**
     * time elapsed
     */
    private int ticksElapsed;


    public static void setDefaultEachRound(int duration) {
        defaultEachRound = duration;
    }


    public static int getDefaultEachRound() {
        return defaultEachRound;
    }


    DurationTimer() {
        ticksElapsed = 0;
    }

    /**
     *  Registers a callback to be run when a tick has passed.
     * @param cb
     */
    void registerTickCallback(@NotNull final Runnable cb) {
        onTickCallbacks.add(cb);
    }

    /**
     *  Registers a callback to be run when time is up.
     * @param cb
     */
    void registerTimeUpCallback(@NotNull final Runnable cb) {
        onTimeUpCallbacks.add(cb);
    }


    /**
     * start the timer, timer should increase 1 every 1 second
     * Hint:
     *  - You may need to use {@link Timer#scheduleAtFixedRate(TimerTask, long, long)}
     */
    void start() {
        //TODO
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ticksElapsed++;
                onTickCallbacks.forEach(Runnable::run);
                onTimeUpCallbacks.forEach(Runnable::run);
            }
        };
        flowTimer.scheduleAtFixedRate(timerTask, 1000L, 1000L);
    }

    /**
     * Stop the timer
     */
    void stop() {
        //TODO
    }

}
