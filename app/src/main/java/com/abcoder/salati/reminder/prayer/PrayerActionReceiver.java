package com.abcoder.salati.reminder.prayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.database.AppDatabase;
import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.data.repository.PrayerRepository;

public class PrayerActionReceiver
        extends BroadcastReceiver {

    private static final String TAG =
            "PrayerActionReceiver";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        if (intent == null
                || !PrayerReminderContract
                .ACTION_RECORD_PRAYER
                .equals(intent.getAction())) {
            return;
        }

        String recordDate = intent.getStringExtra(
                PrayerReminderContract.EXTRA_RECORD_DATE
        );

        String prayerTypeValue =
                intent.getStringExtra(
                        PrayerReminderContract
                                .EXTRA_PRAYER_TYPE
                );

        String prayerStatusValue =
                intent.getStringExtra(
                        PrayerReminderContract
                                .EXTRA_PRAYER_STATUS
                );

        int notificationId = intent.getIntExtra(
                PrayerReminderContract
                        .EXTRA_NOTIFICATION_ID,
                -1
        );

        if (recordDate == null
                || prayerTypeValue == null
                || prayerStatusValue == null
                || notificationId < 0) {
            Log.e(
                    TAG,
                    "Prayer action contained invalid extras"
            );
            return;
        }

        final PrayerType prayerType;
        final PrayerStatus prayerStatus;

        try {
            prayerType =
                    PrayerType.valueOf(prayerTypeValue);

            prayerStatus =
                    PrayerStatus.valueOf(
                            prayerStatusValue
                    );

        } catch (IllegalArgumentException exception) {
            Log.e(
                    TAG,
                    "Prayer action contained invalid values",
                    exception
            );
            return;
        }

        PendingResult pendingResult = goAsync();

        AppDatabase.databaseExecutor.execute(() -> {
            try {
                SalatiApplication application =
                        (SalatiApplication)
                                context
                                        .getApplicationContext();

                PrayerRepository prayerRepository =
                        application
                                .getPrayerRepository();

                prayerRepository
                        .setPrayerStatusBlocking(
                                recordDate,
                                prayerType,
                                prayerStatus,
                                AnswerSource.NOTIFICATION
                        );

                NotificationManagerCompat
                        .from(context)
                        .cancel(notificationId);

            } catch (Exception exception) {
                Log.e(
                        TAG,
                        "Could not save prayer status",
                        exception
                );

            } finally {
                pendingResult.finish();
            }
        });
    }
}