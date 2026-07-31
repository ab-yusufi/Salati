package com.abcoder.salati.reminder.habit;

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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

import com.abcoder.salati.R;
import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.data.repository.HabitRepository;

public final class HabitNotificationHelper {

    private HabitNotificationHelper() {
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
                        HabitReminderContract.CHANNEL_ID,
                        context.getString(
                                R.string
                                        .habit_notification_channel_name
                        ),
                        NotificationManager
                                .IMPORTANCE_DEFAULT
                );

        channel.setDescription(
                context.getString(
                        R.string
                                .habit_notification_channel_description
                )
        );

        NotificationManager manager =
                context.getSystemService(
                        NotificationManager.class
                );

        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    public static void showHabitReminder(
            Context context,
            Habit habit,
            String recordDate,
            int notificationId,
            int snoozeCount
    ) {
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        HabitReminderContract.CHANNEL_ID
                )
                        .setSmallIcon(
                                R.drawable
                                        .ic_notification_status
                        )
                        .setContentTitle(habit.title)
                        .setContentText(
                                context.getString(
                                        R.string
                                                .habit_notification_message
                                )
                        )
                        .setPriority(
                                NotificationCompat
                                        .PRIORITY_DEFAULT
                        )
                        .setCategory(
                                NotificationCompat
                                        .CATEGORY_REMINDER
                        )
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .addAction(
                                R.drawable
                                        .ic_notification_status,
                                context.getString(
                                        R.string.action_completed
                                ),
                                createStatusPendingIntent(
                                        context,
                                        habit.id,
                                        recordDate,
                                        HabitStatus.COMPLETED,
                                        notificationId
                                )
                        )
                        .addAction(
                                R.drawable
                                        .ic_notification_status,
                                context.getString(
                                        R.string
                                                .action_not_completed
                                ),
                                createStatusPendingIntent(
                                        context,
                                        habit.id,
                                        recordDate,
                                        HabitStatus.NOT_COMPLETED,
                                        notificationId
                                )
                        );

        if (canOfferSnooze(
                recordDate,
                habit.snoozeMinutes,
                snoozeCount
        )) {
            builder.addAction(
                    R.drawable.ic_notification_status,
                    context.getString(
                            R.string.action_remind_later
                    ),
                    createSnoozePendingIntent(
                            context,
                            habit.id,
                            recordDate,
                            notificationId
                    )
            );
        }

        try {
            NotificationManagerCompat
                    .from(context)
                    .notify(
                            notificationId,
                            builder.build()
                    );

        } catch (SecurityException ignored) {
            // Permission may have just been revoked.
        }
    }

    private static boolean canOfferSnooze(
            String recordDate,
            int snoozeMinutes,
            int snoozeCount
    ) {
        if (snoozeCount
                >= HabitRepository
                .MAXIMUM_DAILY_SNOOZES) {
            return false;
        }

        long snoozeAt =
                System.currentTimeMillis()
                        + snoozeMinutes * 60_000L;

        LocalDate snoozeDate =
                Instant.ofEpochMilli(snoozeAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

        return snoozeDate.toString()
                .equals(recordDate);
    }

    private static PendingIntent
    createStatusPendingIntent(
            Context context,
            long habitId,
            String recordDate,
            HabitStatus status,
            int notificationId
    ) {
        Intent intent = new Intent(
                context,
                HabitActionReceiver.class
        );

        intent.setAction(
                HabitReminderContract
                        .ACTION_RECORD_HABIT
        );

        addCommonExtras(
                intent,
                habitId,
                recordDate,
                notificationId
        );

        intent.putExtra(
                HabitReminderContract
                        .EXTRA_HABIT_STATUS,
                status.name()
        );

        return PendingIntent.getBroadcast(
                context,
                Objects.hash(
                        notificationId,
                        status.name()
                ),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static PendingIntent
    createSnoozePendingIntent(
            Context context,
            long habitId,
            String recordDate,
            int notificationId
    ) {
        Intent intent = new Intent(
                context,
                HabitActionReceiver.class
        );

        intent.setAction(
                HabitReminderContract
                        .ACTION_SNOOZE_HABIT
        );

        addCommonExtras(
                intent,
                habitId,
                recordDate,
                notificationId
        );

        return PendingIntent.getBroadcast(
                context,
                Objects.hash(
                        notificationId,
                        "snooze"
                ),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static void addCommonExtras(
            Intent intent,
            long habitId,
            String recordDate,
            int notificationId
    ) {
        intent.putExtra(
                HabitReminderContract.EXTRA_HABIT_ID,
                habitId
        );

        intent.putExtra(
                HabitReminderContract.EXTRA_RECORD_DATE,
                recordDate
        );

        intent.putExtra(
                HabitReminderContract
                        .EXTRA_NOTIFICATION_ID,
                notificationId
        );
    }
}