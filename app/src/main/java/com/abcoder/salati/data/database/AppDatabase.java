package com.abcoder.salati.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.abcoder.salati.data.converter.AppTypeConverters;
import com.abcoder.salati.data.dao.HabitDao;
import com.abcoder.salati.data.dao.HabitRecordDao;
import com.abcoder.salati.data.dao.PrayerRecordDao;
import com.abcoder.salati.data.dao.PrayerReminderSettingDao;
import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.entity.HabitRecord;
import com.abcoder.salati.data.entity.PrayerRecord;
import com.abcoder.salati.data.entity.PrayerReminderSetting;

@Database(
        entities = {
                PrayerRecord.class,
                PrayerReminderSetting.class,
                Habit.class,
                HabitRecord.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters(AppTypeConverters.class)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "salah_tracker_database";

    private static volatile AppDatabase INSTANCE;

    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract PrayerRecordDao prayerRecordDao();

    public abstract PrayerReminderSettingDao prayerReminderSettingDao();

    public abstract HabitDao habitDao();

    public abstract HabitRecordDao habitRecordDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .build();
                }
            }
        }

        return INSTANCE;
    }
}
