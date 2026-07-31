package com.abcoder.salati.reminder.prayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abcoder.salati.data.database.AppDatabase;

public class PrayerSystemEventReceiver
        extends BroadcastReceiver {

    private static final String TAG =
            "PrayerSystemReceiver";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        if (!isSupportedAction(action)) {
            return;
        }

        Log.i(
                TAG,
                "Received system event: " + action
        );

        /*
         * BroadcastReceiver.onReceive() must finish quickly.
         * goAsync() lets us perform the short Room query and
         * alarm recreation on a background executor.
         */
        PendingResult pendingResult = goAsync();

        Context applicationContext =
                context.getApplicationContext();

        AppDatabase.databaseExecutor.execute(() -> {
            try {
                PrayerReminderRescheduler
                        .rescheduleBlocking(
                                applicationContext,
                                action
                        );

            } catch (Exception exception) {
                Log.e(
                        TAG,
                        "Could not restore prayer reminders",
                        exception
                );

            } finally {
                pendingResult.finish();
            }
        });
    }

    private boolean isSupportedAction(String action) {
        return Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED
                .equals(action);
    }
}