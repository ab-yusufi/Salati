package com.abcoder.salati.util;

import android.os.Handler;
import android.os.Looper;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

public final class DayRolloverScheduler {

    private static final long SAFETY_DELAY_MILLIS =
            1_000L;

    private final Handler handler;
    private final Runnable callback;
    private final Runnable midnightRunnable;

    private boolean running;

    public DayRolloverScheduler(
            Runnable callback
    ) {
        this.callback =
                Objects.requireNonNull(callback);

        handler =
                new Handler(
                        Looper.getMainLooper()
                );

        midnightRunnable = () -> {
            if (!running) {
                return;
            }

            this.callback.run();
            scheduleNextMidnight();
        };
    }

    public void start() {
        running = true;

        handler.removeCallbacks(
                midnightRunnable
        );

        scheduleNextMidnight();
    }

    public void stop() {
        running = false;

        handler.removeCallbacks(
                midnightRunnable
        );
    }

    private void scheduleNextMidnight() {
        if (!running) {
            return;
        }

        ZonedDateTime now =
                ZonedDateTime.now();

        ZonedDateTime nextDayStart =
                now.toLocalDate()
                        .plusDays(1)
                        .atStartOfDay(
                                now.getZone()
                        );

        long delayMillis =
                Duration.between(
                                now,
                                nextDayStart
                        )
                        .toMillis()
                        + SAFETY_DELAY_MILLIS;

        handler.postDelayed(
                midnightRunnable,
                Math.max(
                        SAFETY_DELAY_MILLIS,
                        delayMillis
                )
        );
    }
}