package com.abcoder.salati.reminder.prayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.database.AppDatabase;
import com.abcoder.salati.data.entity.PrayerReminderSetting;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.data.repository.PrayerRepository;

public class PrayerAlarmReceiver
        extends BroadcastReceiver {

    private static final String TAG =
            "PrayerAlarmReceiver";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        if (intent == null
                || !PrayerReminderContract
                .ACTION_SHOW_REMINDER
                .equals(intent.getAction())) {
            return;
        }

        String recordDate =
                intent.getStringExtra(
                        PrayerReminderContract
                                .EXTRA_RECORD_DATE
                );

        String prayerTypeValue =
                intent.getStringExtra(
                        PrayerReminderContract
                                .EXTRA_PRAYER_TYPE
                );

        int notificationId =
                intent.getIntExtra(
                        PrayerReminderContract
                                .EXTRA_NOTIFICATION_ID,
                        -1
                );

        long triggerAtMillis =
                intent.getLongExtra(
                        PrayerReminderContract
                                .EXTRA_TRIGGER_AT,
                        -1L
                );



        if (recordDate == null
                || prayerTypeValue == null
                || notificationId < 0
                || triggerAtMillis < 0L) {
            Log.e(
                    TAG,
                    "Prayer alarm contained invalid extras"
            );
            return;
        }

        final PrayerType prayerType;

        try {
            prayerType =
                    PrayerType.valueOf(prayerTypeValue);

        } catch (IllegalArgumentException exception) {
            Log.e(
                    TAG,
                    "Unknown prayer type: "
                            + prayerTypeValue,
                    exception
            );
            return;
        }

        PendingResult pendingResult = goAsync();

        Context applicationContext =
                context.getApplicationContext();

        AppDatabase.databaseExecutor.execute(() -> {
            try {
                SalatiApplication application =
                        (SalatiApplication)
                                applicationContext;

                PrayerRepository repository =
                        application
                                .getPrayerRepository();




                /*
                 * Read the latest saved setting in case the user
                 * changed or disabled it after the alarm was set.
                 */
                PrayerReminderSetting currentSetting =
                        repository
                                .getReminderSettingBlocking(
                                        prayerType
                                );

                if (currentSetting == null
                        || !currentSetting.enabled) {
                    return;
                }

                repository.ensurePrayerRecordBlocking(
                        recordDate,
                        prayerType,
                        triggerAtMillis,
                        notificationId
                );

                PrayerNotificationHelper
                        .showPrayerReminder(
                                applicationContext,
                                recordDate,
                                prayerType,
                                notificationId
                        );

                /*
                 * This was a one-time alarm. Install the next
                 * future occurrence now.
                 */
                PrayerReminderScheduler.scheduleNext(
                        applicationContext,
                        currentSetting
                );

            } catch (Exception exception) {
                Log.e(
                        TAG,
                        "Could not process prayer alarm",
                        exception
                );

            } finally {
                pendingResult.finish();
            }
        });
    }
}