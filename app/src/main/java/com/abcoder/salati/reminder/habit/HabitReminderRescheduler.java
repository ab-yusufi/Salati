package com.abcoder.salati.reminder.habit;

import android.content.Context;
import android.util.Log;

import androidx.annotation.WorkerThread;

import java.util.List;

import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.database.AppDatabase;
import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.repository.HabitRepository;

public final class HabitReminderRescheduler {

    private static final String TAG =
            "HabitRescheduler";

    private HabitReminderRescheduler() {
    }

    public static void rescheduleAsync(
            Context context,
            String reason
    ) {
        Context appContext =
                context.getApplicationContext();

        AppDatabase.databaseExecutor.execute(() ->
                rescheduleBlocking(
                        appContext,
                        reason
                )
        );
    }

    @WorkerThread
    public static void rescheduleBlocking(
            Context context,
            String reason
    ) {
        SalatiApplication application =
                (SalatiApplication)
                        context.getApplicationContext();

        HabitRepository repository =
                application.getHabitRepository();

        List<Habit> habits =
                repository.getEnabledHabitsBlocking();

        HabitReminderScheduler.rescheduleAll(
                context,
                habits
        );

        Log.i(
                TAG,
                "Rescheduled "
                        + habits.size()
                        + " habits. Reason: "
                        + reason
        );
    }
}