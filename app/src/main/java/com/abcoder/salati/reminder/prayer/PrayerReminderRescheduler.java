package com.abcoder.salati.reminder.prayer;

import android.content.Context;
import android.util.Log;

import androidx.annotation.WorkerThread;

import java.util.List;


import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.database.AppDatabase;
import com.abcoder.salati.data.entity.PrayerReminderSetting;
import com.abcoder.salati.data.repository.PrayerRepository;

public final class PrayerReminderRescheduler {

    private static final String TAG =
            "PrayerRescheduler";

    private PrayerReminderRescheduler() {
        // Prevent instantiation.
    }

    /**
     * Starts rescheduling work on the database executor.
     */
    public static void rescheduleAsync(
            Context context,
            String reason
    ) {
        Context applicationContext =
                context.getApplicationContext();

        AppDatabase.databaseExecutor.execute(() ->
                rescheduleBlocking(
                        applicationContext,
                        reason
                )
        );
    }

    /**
     * Must be called from a background thread.
     */
    @WorkerThread
    public static void rescheduleBlocking(
            Context context,
            String reason
    ) {
        SalatiApplication application =
                (SalatiApplication)
                        context.getApplicationContext();

        PrayerRepository repository =
                application.getPrayerRepository();

        /*
         * Ensures five settings exist. Existing customized
         * settings are not overwritten.
         */
        repository
                .initializeDefaultReminderSettingsBlocking();

        List<PrayerReminderSetting> settings =
                repository.getReminderSettingsBlocking();

        PrayerReminderScheduler.rescheduleAll(
                context,
                settings
        );

        Log.i(
                TAG,
                "Rescheduled "
                        + settings.size()
                        + " prayer settings. Reason: "
                        + reason
        );
    }
}