package com.abcoder.salati.reminder.habit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.database.AppDatabase;
import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.entity.HabitRecord;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.data.repository.HabitRepository;

public class HabitAlarmReceiver
        extends BroadcastReceiver {

    private static final String TAG =
            "HabitAlarmReceiver";

    @Override
    public void onReceive(
            Context context,
            Intent intent
    ) {
        if (intent == null
                || !HabitReminderContract
                .ACTION_SHOW_HABIT
                .equals(intent.getAction())) {
            return;
        }

        long habitId = intent.getLongExtra(
                HabitReminderContract.EXTRA_HABIT_ID,
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

        boolean isSnooze =
                intent.getBooleanExtra(
                        HabitReminderContract
                                .EXTRA_IS_SNOOZE,
                        false
                );

        if (habitId < 1
                || recordDate == null
                || notificationId < 0) {
            Log.e(TAG, "Invalid habit alarm");
            return;
        }

        PendingResult pendingResult = goAsync();

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

                Habit habit =
                        repository.getHabitBlocking(
                                habitId
                        );

                if (habit == null || !habit.enabled) {
                    return;
                }

                if (!isSnooze) {
                    repository
                            .ensureHabitRecordBlocking(
                                    habitId,
                                    recordDate
                            );
                }

                HabitRecord record =
                        repository
                                .getHabitRecordBlocking(
                                        habitId,
                                        recordDate
                                );

                /*
                 * Install tomorrow's normal reminder even when
                 * today's habit was already manually answered.
                 */
                if (!isSnooze) {
                    HabitReminderScheduler.scheduleNext(
                            appContext,
                            habit
                    );
                }

                if (record == null
                        || record.status
                        != HabitStatus.PENDING) {
                    return;
                }

                HabitNotificationHelper
                        .showHabitReminder(
                                appContext,
                                habit,
                                recordDate,
                                notificationId,
                                record.snoozeCount
                        );

            } catch (Exception exception) {
                Log.e(
                        TAG,
                        "Could not process habit alarm",
                        exception
                );

            } finally {
                pendingResult.finish();
            }
        });
    }
}