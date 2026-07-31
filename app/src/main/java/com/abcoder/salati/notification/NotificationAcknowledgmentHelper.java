package com.abcoder.salati.notification;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.abcoder.salati.R;

public final class NotificationAcknowledgmentHelper {

    private static final long
            CONFIRMATION_TIMEOUT_MILLIS =
            5_000L;

    private NotificationAcknowledgmentHelper() {
        // Prevent instantiation.
    }

    public static void show(
            Context context,
            String channelId,
            int notificationId,
            String title,
            String message
    ) {
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        /*
         * The original notification should not remain visible
         * when notifications have since been disabled.
         */
        if (!NotificationPermissionHelper
                .areNotificationsEnabled(context)) {

            notificationManager.cancel(
                    notificationId
            );

            return;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        channelId
                )
                        .setSmallIcon(
                                R.drawable
                                        .ic_notification_status
                        )
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat
                                        .BigTextStyle()
                                        .bigText(message)
                        )
                        .setPriority(
                                NotificationCompat
                                        .PRIORITY_LOW
                        )
                        .setCategory(
                                NotificationCompat
                                        .CATEGORY_STATUS
                        )
                        .setVisibility(
                                NotificationCompat
                                        .VISIBILITY_PRIVATE
                        )
                        .setSilent(true)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true)
                        .setOngoing(false)
                        .setTimeoutAfter(
                                CONFIRMATION_TIMEOUT_MILLIS
                        );

        try {
            /*
             * Reusing the original ID replaces the actionable
             * reminder instead of creating another notification.
             */
            notificationManager.notify(
                    notificationId,
                    builder.build()
            );

        } catch (SecurityException exception) {
            /*
             * Permission may have been revoked between the
             * permission check and notify().
             */
            notificationManager.cancel(
                    notificationId
            );
        }
    }
}