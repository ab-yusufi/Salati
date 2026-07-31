package com.abcoder.salati.reminder.prayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.abcoder.salati.data.entity.PrayerReminderSetting;
import com.abcoder.salati.data.model.PrayerType;

public final class PrayerReminderScheduler {



    private static final int NOTIFICATION_ID_BASE =
            100_000;

    private PrayerReminderScheduler() {
        // Prevent instantiation.
    }



    /**
     * Schedules or cancels an alarm according to one setting.
     */
    public static void applySetting(
            Context context,
            PrayerReminderSetting setting
    ) {
        cancel(context, setting.prayerType);

        if (setting.enabled) {
            scheduleNext(context, setting);
        }
    }

    /**
     * Applies every stored reminder setting.
     */
    public static void rescheduleAll(
            Context context,
            List<PrayerReminderSetting> settings
    ) {
        if (settings == null) {
            return;
        }

        for (PrayerReminderSetting setting : settings) {
            applySetting(context, setting);
        }
    }

    /**
     * Schedules the next future occurrence of a prayer.
     */
    public static long scheduleNext(
            Context context,
            PrayerReminderSetting setting
    ) {
        validateTime(setting.hour, setting.minute);

        ZonedDateTime now = ZonedDateTime.now();

        ZonedDateTime triggerDateTime =
                now.withHour(setting.hour)
                        .withMinute(setting.minute)
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
                        triggerDateTime.toLocalDate(),
                        setting.prayerType
                );

        int alarmRequestCode =
                getAlarmRequestCode(
                        setting.prayerType
                );

        PendingIntent pendingIntent =
                createAlarmPendingIntent(
                        context,
                        alarmRequestCode,
                        recordDate,
                        setting.prayerType,
                        notificationId,
                        triggerAtMillis
                );

        scheduleAlarm(
                context,
                triggerAtMillis,
                pendingIntent
        );

        return triggerAtMillis;
    }

    /**
     * Cancels the currently scheduled alarm for one prayer.
     */
    public static void cancel(
            Context context,
            PrayerType prayerType
    ) {
        AlarmManager alarmManager =
                getAlarmManager(context);

        Intent intent = new Intent(
                context,
                PrayerAlarmReceiver.class
        );

        intent.setAction(
                PrayerReminderContract
                        .ACTION_SHOW_REMINDER
        );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        getAlarmRequestCode(prayerType),
                        intent,
                        PendingIntent.FLAG_NO_CREATE
                                | PendingIntent.FLAG_IMMUTABLE
                );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static void scheduleAlarm(
            Context context,
            long triggerAtMillis,
            PendingIntent pendingIntent
    ) {
        AlarmManager alarmManager =
                getAlarmManager(context);

        /*
         * Explicitly cancel an older matching alarm before
         * installing its replacement.
         */
        alarmManager.cancel(pendingIntent);

        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
        );
    }

    private static PendingIntent
    createAlarmPendingIntent(
            Context context,
            int requestCode,
            String recordDate,
            PrayerType prayerType,
            int notificationId,
            long triggerAtMillis
    ) {
        Intent alarmIntent = new Intent(
                context,
                PrayerAlarmReceiver.class
        );

        alarmIntent.setAction(
                PrayerReminderContract
                        .ACTION_SHOW_REMINDER
        );

        alarmIntent.putExtra(
                PrayerReminderContract.EXTRA_RECORD_DATE,
                recordDate
        );

        alarmIntent.putExtra(
                PrayerReminderContract.EXTRA_PRAYER_TYPE,
                prayerType.name()
        );

        alarmIntent.putExtra(
                PrayerReminderContract
                        .EXTRA_NOTIFICATION_ID,
                notificationId
        );

        alarmIntent.putExtra(
                PrayerReminderContract.EXTRA_TRIGGER_AT,
                triggerAtMillis
        );



        return PendingIntent.getBroadcast(
                context,
                requestCode,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static AlarmManager getAlarmManager(
            Context context
    ) {
        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(
                        Context.ALARM_SERVICE
                );

        if (alarmManager == null) {
            throw new IllegalStateException(
                    "AlarmManager is unavailable"
            );
        }

        return alarmManager;
    }

    private static int getAlarmRequestCode(
            PrayerType prayerType
    ) {
        return PrayerReminderContract
                .PRAYER_ALARM_REQUEST_CODE_BASE
                + prayerType.ordinal();
    }

    private static int createNotificationId(
            LocalDate recordDate,
            PrayerType prayerType
    ) {
        long value =
                recordDate.toEpochDay() * 10L
                        + prayerType.ordinal();

        return NOTIFICATION_ID_BASE
                + Math.toIntExact(value);
    }

    private static void validateTime(
            int hour,
            int minute
    ) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(
                    "Hour must be between 0 and 23"
            );
        }

        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(
                    "Minute must be between 0 and 59"
            );
        }
    }
}
