package com.abcoder.salati.reminder.habit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abcoder.salati.data.database.AppDatabase;

public class HabitSystemEventReceiver
        extends BroadcastReceiver {

    private static final String TAG =
            "HabitSystemReceiver";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        if (intent == null
                || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();

        if (!isSupportedAction(action)) {
            return;
        }

        PendingResult pendingResult = goAsync();

        Context appContext =
                context.getApplicationContext();

        AppDatabase.databaseExecutor.execute(() -> {
            try {
                HabitReminderRescheduler
                        .rescheduleBlocking(
                                appContext,
                                action
                        );

            } catch (Exception exception) {
                Log.e(
                        TAG,
                        "Could not restore habit reminders",
                        exception
                );

            } finally {
                pendingResult.finish();
            }
        });
    }

    private boolean isSupportedAction(
            String action
    ) {
        return Intent.ACTION_BOOT_COMPLETED
                .equals(action)
                || Intent.ACTION_TIME_CHANGED
                .equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED
                .equals(action)
                || Intent.ACTION_MY_PACKAGE_REPLACED
                .equals(action);
    }
}