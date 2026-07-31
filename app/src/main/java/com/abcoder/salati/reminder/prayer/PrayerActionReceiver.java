package com.abcoder.salati.reminder.prayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abcoder.salati.R;
import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.database.AppDatabase;
import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.data.repository.PrayerRepository;
import com.abcoder.salati.notification
        .NotificationAcknowledgmentHelper;

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

        String prayerStatusValue =
                intent.getStringExtra(
                        PrayerReminderContract
                                .EXTRA_PRAYER_STATUS
                );

        int notificationId =
                intent.getIntExtra(
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
                    PrayerType.valueOf(
                            prayerTypeValue
                    );

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

        /*
         * Notification actions should only record one of the
         * three completed prayer statuses.
         */
        if (prayerStatus
                == PrayerStatus.UNRECORDED) {

            Log.e(
                    TAG,
                    "Notification cannot clear prayer status"
            );

            return;
        }

        PendingResult pendingResult =
                goAsync();

        Context appContext =
                context.getApplicationContext();

        AppDatabase.databaseExecutor.execute(() -> {
            try {
                SalatiApplication application =
                        (SalatiApplication)
                                appContext;

                PrayerRepository repository =
                        application
                                .getPrayerRepository();

                /*
                 * This blocking call must complete before the
                 * success confirmation is displayed.
                 */
                repository.setPrayerStatusBlocking(
                        recordDate,
                        prayerType,
                        prayerStatus,
                        AnswerSource.NOTIFICATION
                );

                String prayerName =
                        getPrayerName(
                                appContext,
                                prayerType
                        );

                String statusName =
                        getStatusName(
                                appContext,
                                prayerStatus
                        );

                NotificationAcknowledgmentHelper
                        .show(
                                appContext,
                                PrayerReminderContract
                                        .CHANNEL_ID,
                                notificationId,
                                appContext.getString(
                                        R.string
                                                .notification_prayer_saved_title
                                ),
                                appContext.getString(
                                        R.string
                                                .notification_prayer_saved_message,
                                        prayerName,
                                        statusName
                                )
                        );

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

    private static String getPrayerName(
            Context context,
            PrayerType prayerType
    ) {
        switch (prayerType) {
            case FAJR:
                return context.getString(
                        R.string.prayer_fajr
                );

            case DHUHR:
                return context.getString(
                        R.string.prayer_dhuhr
                );

            case ASR:
                return context.getString(
                        R.string.prayer_asr
                );

            case MAGHRIB:
                return context.getString(
                        R.string.prayer_maghrib
                );

            case ISHA:
                return context.getString(
                        R.string.prayer_isha
                );

            default:
                throw new IllegalArgumentException(
                        "Unknown prayer type: "
                                + prayerType
                );
        }
    }

    private static String getStatusName(
            Context context,
            PrayerStatus status
    ) {
        switch (status) {
            case ON_TIME:
                return context.getString(
                        R.string.status_on_time
                );

            case LATE:
                return context.getString(
                        R.string.status_late
                );

            case MISSED:
                return context.getString(
                        R.string.status_missed
                );

            case UNRECORDED:
            default:
                return context.getString(
                        R.string.status_unrecorded
                );
        }
    }
}