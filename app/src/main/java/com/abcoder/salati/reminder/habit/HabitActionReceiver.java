package com.abcoder.salati.reminder.habit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.abcoder.salati.R;
import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.database.AppDatabase;
import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.entity.HabitRecord;
import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.notification
        .NotificationAcknowledgmentHelper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class HabitActionReceiver
        extends BroadcastReceiver {

    private static final String TAG =
            "HabitActionReceiver";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        if (intent == null
                || intent.getAction() == null) {
            return;
        }

        String action =
                intent.getAction();

        if (!HabitReminderContract
                .ACTION_RECORD_HABIT
                .equals(action)
                && !HabitReminderContract
                .ACTION_SNOOZE_HABIT
                .equals(action)) {

            return;
        }

        long habitId =
                intent.getLongExtra(
                        HabitReminderContract
                                .EXTRA_HABIT_ID,
                        -1L
                );

        String recordDate =
                intent.getStringExtra(
                        HabitReminderContract
                                .EXTRA_RECORD_DATE
                );

        int notificationId =
                intent.getIntExtra(
                        HabitReminderContract
                                .EXTRA_NOTIFICATION_ID,
                        -1
                );

        if (habitId < 1
                || recordDate == null
                || notificationId < 0) {

            Log.e(
                    TAG,
                    "Invalid habit action"
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

                HabitRepository repository =
                        application
                                .getHabitRepository();

                if (HabitReminderContract
                        .ACTION_RECORD_HABIT
                        .equals(action)) {

                    handleStatusAction(
                            appContext,
                            intent,
                            repository,
                            habitId,
                            recordDate,
                            notificationId
                    );

                } else {
                    handleSnoozeAction(
                            appContext,
                            repository,
                            habitId,
                            recordDate,
                            notificationId
                    );
                }

            } catch (Exception exception) {
                Log.e(
                        TAG,
                        "Could not process habit action",
                        exception
                );

            } finally {
                pendingResult.finish();
            }
        });
    }

    private void handleStatusAction(
            Context context,
            Intent intent,
            HabitRepository repository,
            long habitId,
            String recordDate,
            int notificationId
    ) {
        String statusValue =
                intent.getStringExtra(
                        HabitReminderContract
                                .EXTRA_HABIT_STATUS
                );

        if (statusValue == null) {
            return;
        }

        final HabitStatus status;

        try {
            status =
                    HabitStatus.valueOf(
                            statusValue
                    );

        } catch (IllegalArgumentException exception) {
            Log.e(
                    TAG,
                    "Unknown habit status: "
                            + statusValue,
                    exception
            );

            return;
        }

        if (status != HabitStatus.COMPLETED
                && status
                != HabitStatus.NOT_COMPLETED) {

            return;
        }

        Habit habit =
                repository.getHabitBlocking(
                        habitId
                );

        if (habit == null) {
            NotificationManagerCompat
                    .from(context)
                    .cancel(notificationId);

            return;
        }

        /*
         * Do not acknowledge until the blocking Room operation
         * completes successfully.
         */
        repository.setHabitStatusBlocking(
                habitId,
                recordDate,
                status,
                AnswerSource.NOTIFICATION
        );

        HabitReminderScheduler.cancelSnooze(
                context,
                habitId
        );

        NotificationAcknowledgmentHelper.show(
                context,
                HabitReminderContract.CHANNEL_ID,
                notificationId,
                context.getString(
                        R.string
                                .notification_habit_saved_title
                ),
                context.getString(
                        R.string
                                .notification_habit_saved_message,
                        habit.title,
                        getStatusName(
                                context,
                                status
                        )
                )
        );
    }

    private void handleSnoozeAction(
            Context context,
            HabitRepository repository,
            long habitId,
            String recordDate,
            int notificationId
    ) {
        Habit habit =
                repository.getHabitBlocking(
                        habitId
                );

        HabitRecord record =
                repository.getHabitRecordBlocking(
                        habitId,
                        recordDate
                );

        if (habit == null
                || !habit.enabled
                || record == null
                || record.status
                != HabitStatus.PENDING) {

            NotificationManagerCompat
                    .from(context)
                    .cancel(notificationId);

            return;
        }

        long triggerAtMillis =
                System.currentTimeMillis()
                        + habit.snoozeMinutes
                        * 60_000L;

        LocalDate triggerDate =
                Instant.ofEpochMilli(
                                triggerAtMillis
                        )
                        .atZone(
                                ZoneId.systemDefault()
                        )
                        .toLocalDate();

        if (!triggerDate.toString()
                .equals(recordDate)) {

            NotificationAcknowledgmentHelper.show(
                    context,
                    HabitReminderContract.CHANNEL_ID,
                    notificationId,
                    context.getString(
                            R.string
                                    .notification_snooze_unavailable_title
                    ),
                    context.getString(
                            R.string
                                    .notification_snooze_unavailable_message,
                            habit.title
                    )
            );

            return;
        }

        boolean allowed =
                repository
                        .tryIncrementSnoozeCountBlocking(
                                habitId,
                                recordDate
                        );

        if (!allowed) {
            NotificationAcknowledgmentHelper.show(
                    context,
                    HabitReminderContract.CHANNEL_ID,
                    notificationId,
                    context.getString(
                            R.string
                                    .notification_snooze_limit_title
                    ),
                    context.getString(
                            R.string
                                    .notification_snooze_limit_message,
                            habit.title,
                            HabitRepository
                                    .MAXIMUM_DAILY_SNOOZES
                    )
            );

            return;
        }

        /*
         * Only show success after the snooze count has been
         * updated and the next alarm has been scheduled.
         */
        HabitReminderScheduler.scheduleSnooze(
                context,
                habitId,
                recordDate,
                triggerAtMillis
        );

        NotificationAcknowledgmentHelper.show(
                context,
                HabitReminderContract.CHANNEL_ID,
                notificationId,
                context.getString(
                        R.string
                                .notification_habit_snoozed_title
                ),
                context.getString(
                        R.string
                                .notification_habit_snoozed_message,
                        habit.title,
                        habit.snoozeMinutes
                )
        );
    }

    private static String getStatusName(
            Context context,
            HabitStatus status
    ) {
        switch (status) {
            case COMPLETED:
                return context.getString(
                        R.string
                                .habit_status_completed
                );

            case NOT_COMPLETED:
                return context.getString(
                        R.string
                                .habit_status_not_completed
                );

            case PENDING:
            default:
                return context.getString(
                        R.string
                                .habit_status_pending
                );
        }
    }
}