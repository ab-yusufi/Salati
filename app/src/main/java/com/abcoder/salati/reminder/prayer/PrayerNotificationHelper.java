package com.abcoder.salati.reminder.prayer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.abcoder.salati.R;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;

public final class PrayerNotificationHelper {

    private PrayerNotificationHelper() {
        // Prevent instantiation.
    }

    public static void createNotificationChannel(
            Context context
    ) {
        if (Build.VERSION.SDK_INT
                < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel =
                new NotificationChannel(
                        PrayerReminderContract.CHANNEL_ID,
                        context.getString(
                                R.string
                                        .prayer_notification_channel_name
                        ),
                        NotificationManager.IMPORTANCE_DEFAULT
                );

        channel.setDescription(
                context.getString(
                        R.string
                                .prayer_notification_channel_description
                )
        );

        NotificationManager notificationManager =
                context.getSystemService(
                        NotificationManager.class
                );

        if (notificationManager != null) {
            notificationManager
                    .createNotificationChannel(channel);
        }
    }

    public static void showPrayerReminder(
            Context context,
            String recordDate,
            PrayerType prayerType,
            int notificationId
    ) {
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        String prayerName =
                getPrayerName(context, prayerType);

        String message = context.getString(
                R.string.prayer_notification_message,
                prayerName
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        PrayerReminderContract.CHANNEL_ID
                )
                        .setSmallIcon(
                                R.drawable
                                        .ic_notification_status
                        )
                        .setContentTitle(
                                context.getString(
                                        R.string
                                                .prayer_notification_title,
                                        prayerName
                                )
                        )
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat
                                        .BigTextStyle()
                                        .bigText(message)
                        )
                        .setPriority(
                                NotificationCompat
                                        .PRIORITY_DEFAULT
                        )
                        .setCategory(
                                NotificationCompat
                                        .CATEGORY_REMINDER
                        )
                        .setVisibility(
                                NotificationCompat
                                        .VISIBILITY_PRIVATE
                        )
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .addAction(
                                R.drawable
                                        .ic_notification_status,
                                context.getString(
                                        R.string.action_on_time
                                ),
                                createStatusPendingIntent(
                                        context,
                                        recordDate,
                                        prayerType,
                                        PrayerStatus.ON_TIME,
                                        notificationId
                                )
                        )
                        .addAction(
                                R.drawable
                                        .ic_notification_status,
                                context.getString(
                                        R.string.action_late
                                ),
                                createStatusPendingIntent(
                                        context,
                                        recordDate,
                                        prayerType,
                                        PrayerStatus.LATE,
                                        notificationId
                                )
                        )
                        .addAction(
                                R.drawable
                                        .ic_notification_status,
                                context.getString(
                                        R.string.action_missed
                                ),
                                createStatusPendingIntent(
                                        context,
                                        recordDate,
                                        prayerType,
                                        PrayerStatus.MISSED,
                                        notificationId
                                )
                        );

        try {
            NotificationManagerCompat
                    .from(context)
                    .notify(
                            notificationId,
                            builder.build()
                    );
        } catch (SecurityException ignored) {
            /*
             * Notification permission may have been revoked
             * between the permission check and notify().
             */
        }
    }

    private static PendingIntent createStatusPendingIntent(
            Context context,
            String recordDate,
            PrayerType prayerType,
            PrayerStatus prayerStatus,
            int notificationId
    ) {
        Intent actionIntent = new Intent(
                context,
                PrayerActionReceiver.class
        );

        actionIntent.setAction(
                PrayerReminderContract
                        .ACTION_RECORD_PRAYER
        );

        actionIntent.putExtra(
                PrayerReminderContract.EXTRA_RECORD_DATE,
                recordDate
        );

        actionIntent.putExtra(
                PrayerReminderContract.EXTRA_PRAYER_TYPE,
                prayerType.name()
        );

        actionIntent.putExtra(
                PrayerReminderContract.EXTRA_PRAYER_STATUS,
                prayerStatus.name()
        );

        actionIntent.putExtra(
                PrayerReminderContract.EXTRA_NOTIFICATION_ID,
                notificationId
        );

        int requestCode =
                notificationId * 10
                        + prayerStatus.ordinal();

        return PendingIntent.getBroadcast(
                context,
                requestCode,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
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
}