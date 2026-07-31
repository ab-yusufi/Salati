package com.abcoder.salati;

import android.app.Application;

import com.abcoder.salati.data.database.AppDatabase;
import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.data.repository.PrayerRepository;
import com.abcoder.salati.reminder.habit.HabitNotificationHelper;
import com.abcoder.salati.reminder.habit.HabitReminderRescheduler;
import com.abcoder.salati.reminder.prayer.PrayerNotificationHelper;
import com.abcoder.salati.reminder.prayer.PrayerReminderRescheduler;

import com.abcoder.salati.ui.theme.ThemeManager;

public class SalatiApplication extends Application {

    private AppDatabase database;
    private PrayerRepository prayerRepository;
    private HabitRepository habitRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeManager.applySavedTheme(this);
        database = AppDatabase.getInstance(this);

        prayerRepository =
                new PrayerRepository(database);

        habitRepository =
                new HabitRepository(database);

        PrayerNotificationHelper
                .createNotificationChannel(this);

        HabitNotificationHelper
                .createNotificationChannel(this);

        PrayerReminderRescheduler.rescheduleAsync(
                this,
                "application_start"
        );

        HabitReminderRescheduler.rescheduleAsync(
                this,
                "application_start"
        );
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public PrayerRepository getPrayerRepository() {
        return prayerRepository;
    }

    public HabitRepository getHabitRepository() {
        return habitRepository;
    }
}