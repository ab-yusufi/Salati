package com.abcoder.salati.reminder.habit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

import com.abcoder.salati.data.entity.Habit;

public final class HabitReminderScheduler {

    private HabitReminderScheduler() {
    }

    public static void applyHabit(
            Context context,
            Habit habit
    ) {
        cancelAll(context, habit.id);

        if (habit.enabled) {
            scheduleNext(context, habit);
        }
    }

    public static void rescheduleAll(
            Context context,
            List<Habit> habits
    ) {
        if (habits == null) {
            return;
        }

        for (Habit habit : habits) {
            applyHabit(context, habit);
        }
    }

    public static long scheduleNext(
            Context context,
            Habit habit
    ) {
        validateTime(
                habit.reminderHour,
                habit.reminderMinute
        );

        ZonedDateTime now =
                ZonedDateTime.now();

        ZonedDateTime triggerDateTime =
                now.withHour(habit.reminderHour)
                        .withMinute(habit.reminderMinute)
                        .withSecond(0)
                        .withNano(0);

        if (!triggerDateTime.isAfter(now)) {
            triggerDateTime =
                    triggerDateTime.plusDays(1);
        }

        long triggerAtMillis =
                triggerDateTime
                        .toInstant()
                        .toEpochMilli();

        String recordDate =
                triggerDateTime
                        .toLocalDate()
                        .toString();

        int notificationId =
                createNotificationId(
                        habit.id,
                        recordDate
                );

        PendingIntent pendingIntent =
                createAlarmPendingIntent(
                        context,
                        dailyRequestCode(habit.id),
                        habit.id,
                        recordDate,
                        notificationId,
                        triggerAtMillis,
                        false
                );

        scheduleAlarm(
                context,
                triggerAtMillis,
                pendingIntent
        );

        return triggerAtMillis;
    }

    public static void scheduleSnooze(
            Context context,
            long habitId,
            String recordDate,
            long triggerAtMillis
    ) {
        int notificationId =
                createNotificationId(
                        habitId,
                        recordDate
                );

        PendingIntent pendingIntent =
                createAlarmPendingIntent(
                        context,
                        snoozeRequestCode(habitId),
                        habitId,
                        recordDate,
                        notificationId,
                        triggerAtMillis,
                        true
                );

        scheduleAlarm(
                context,
                triggerAtMillis,
                pendingIntent
        );
    }

    public static void cancelAll(
            Context context,
            long habitId
    ) {
        cancel(
                context,
                dailyRequestCode(habitId)
        );

        cancel(
                context,
                snoozeRequestCode(habitId)
        );
    }

    public static void cancelSnooze(
            Context context,
            long habitId
    ) {
        cancel(
                context,
                snoozeRequestCode(habitId)
        );
    }

    private static void scheduleAlarm(
            Context context,
            long triggerAtMillis,
            PendingIntent pendingIntent
    ) {
        AlarmManager alarmManager =
                getAlarmManager(context);

        alarmManager.cancel(pendingIntent);

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
        );
    }

    private static void cancel(
            Context context,
            int requestCode
    ) {
        Intent intent = new Intent(
                context,
                HabitAlarmReceiver.class
        );

        intent.setAction(
                HabitReminderContract
                        .ACTION_SHOW_HABIT
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_NO_CREATE
                                | PendingIntent.FLAG_IMMUTABLE
                );

        if (pendingIntent != null) {
            getAlarmManager(context)
                    .cancel(pendingIntent);

            pendingIntent.cancel();
        }
    }

    private static PendingIntent
    createAlarmPendingIntent(
            Context context,
            int requestCode,
            long habitId,
            String recordDate,
            int notificationId,
            long triggerAtMillis,
            boolean isSnooze
    ) {
        Intent intent = new Intent(
                context,
                HabitAlarmReceiver.class
        );

        intent.setAction(
                HabitReminderContract
                        .ACTION_SHOW_HABIT
        );

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

        intent.putExtra(
                HabitReminderContract.EXTRA_TRIGGER_AT,
                triggerAtMillis
        );

        intent.putExtra(
                HabitReminderContract.EXTRA_IS_SNOOZE,
                isSnooze
        );

        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static AlarmManager getAlarmManager(
            Context context
    ) {
        AlarmManager alarmManager =
                (AlarmManager)
                        context.getSystemService(
                                Context.ALARM_SERVICE
                        );

        if (alarmManager == null) {
            throw new IllegalStateException(
                    "AlarmManager unavailable"
            );
        }

        return alarmManager;
    }

    private static int dailyRequestCode(long habitId) {
        return Objects.hash(
                "habit_daily",
                habitId
        );
    }

    private static int snoozeRequestCode(long habitId) {
        return Objects.hash(
                "habit_snooze",
                habitId
        );
    }

    private static int createNotificationId(
            long habitId,
            String recordDate
    ) {
        return Objects.hash(
                "habit_notification",
                habitId,
                recordDate
        ) & Integer.MAX_VALUE;
    }

    private static void validateTime(
            int hour,
            int minute
    ) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(
                    "Invalid reminder hour"
            );
        }

        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(
                    "Invalid reminder minute"
            );
        }
    }
}