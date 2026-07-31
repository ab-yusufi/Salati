package com.abcoder.salati.data.repository;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.abcoder.salati.data.dao.HabitDao;
import com.abcoder.salati.data.dao.HabitRecordDao;
import com.abcoder.salati.data.database.AppDatabase;
import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.entity.HabitRecord;
import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.HabitStatus;

import android.util.Log;

public final class HabitRepository {
    private static final String TAG =
            "HabitRepository";

    public static final int MAX_ACTIVE_HABITS = 5;

    public static final int DEFAULT_SNOOZE_MINUTES = 60;

    public static final int MAXIMUM_DAILY_SNOOZES = 3;

    public interface StatusOperationCallback {

        void onSuccess();

        void onError(Exception exception);
    }
    public interface SaveHabitCallback {

        void onSuccess(Habit savedHabit);

        void onLimitReached();

        void onError(String message);
    }

    public interface DeleteHabitCallback {

        void onSuccess();

        void onError(String message);
    }

    private final HabitDao habitDao;
    private final HabitRecordDao habitRecordDao;
    private final ExecutorService databaseExecutor;
    private final Handler mainHandler;

    public HabitRepository(AppDatabase database) {
        habitDao = database.habitDao();
        habitRecordDao = database.habitRecordDao();

        databaseExecutor = AppDatabase.databaseExecutor;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public LiveData<List<Habit>> observeAllHabits() {
        return habitDao.observeAll();
    }

    public LiveData<List<Habit>> observeEnabledHabits() {
        return habitDao.observeEnabled();
    }

    public LiveData<List<HabitRecord>>
    observeRecordsForDate(String recordDate) {
        return habitRecordDao.observeForDate(recordDate);
    }

    public void saveHabit(
            Habit habit,
            SaveHabitCallback callback
    ) {
        databaseExecutor.execute(() -> {
            try {
                String cleanedTitle = habit.title.trim();

                if (cleanedTitle.isEmpty()) {
                    postError(callback, "Habit title is required");
                    return;
                }

                Habit existingHabit = habit.id == 0
                        ? null
                        : habitDao.getById(habit.id);

                boolean isBecomingEnabled =
                        habit.enabled
                                && (
                                existingHabit == null
                                        || !existingHabit.enabled
                        );

                if (isBecomingEnabled
                        && habitDao.countEnabled()
                        >= MAX_ACTIVE_HABITS) {

                    mainHandler.post(
                            callback::onLimitReached
                    );

                    return;
                }

                long now = System.currentTimeMillis();

                long createdAt = existingHabit == null
                        ? now
                        : existingHabit.createdAt;

                Habit normalizedHabit = new Habit(
                        habit.id,
                        cleanedTitle,
                        habit.reminderHour,
                        habit.reminderMinute,
                        DEFAULT_SNOOZE_MINUTES,
                        habit.enabled,
                        createdAt,
                        now
                );

                long savedHabitId;

                if (normalizedHabit.id == 0) {
                    savedHabitId =
                            habitDao.insert(normalizedHabit);
                } else {
                    int updatedRows =
                            habitDao.update(normalizedHabit);

                    if (updatedRows == 0) {
                        throw new IllegalStateException(
                                "Habit no longer exists"
                        );
                    }

                    savedHabitId = normalizedHabit.id;
                }

                Habit savedHabit = new Habit(
                        savedHabitId,
                        normalizedHabit.title,
                        normalizedHabit.reminderHour,
                        normalizedHabit.reminderMinute,
                        normalizedHabit.snoozeMinutes,
                        normalizedHabit.enabled,
                        normalizedHabit.createdAt,
                        normalizedHabit.updatedAt
                );

                if (savedHabit.enabled) {
                    ensureHabitRecordBlocking(
                            savedHabit.id,
                            LocalDate.now().toString()
                    );
                }

                mainHandler.post(() ->
                        callback.onSuccess(savedHabit)
                );

            } catch (Exception exception) {
                String message = exception.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {
                    message =
                            exception.getClass()
                                    .getSimpleName();
                }

                postError(callback, message);
            }
        });
    }

    public void deleteHabit(
            long habitId,
            DeleteHabitCallback callback
    ) {
        databaseExecutor.execute(() -> {
            try {
                habitDao.deleteById(habitId);

                mainHandler.post(callback::onSuccess);

            } catch (Exception exception) {
                String message = exception.getMessage();

                if (message == null
                        || message.trim().isEmpty()) {
                    message =
                            exception.getClass()
                                    .getSimpleName();
                }

                final String finalMessage = message;

                mainHandler.post(() ->
                        callback.onError(finalMessage)
                );
            }
        });
    }

    public void ensureRecordsForDate(
            String recordDate
    ) {
        databaseExecutor.execute(() -> {
            List<Habit> enabledHabits =
                    habitDao.getEnabled();

            for (Habit habit : enabledHabits) {
                ensureHabitRecordBlocking(
                        habit.id,
                        recordDate
                );
            }
        });
    }

    public void setHabitStatus(
            long habitId,
            String recordDate,
            HabitStatus habitStatus,
            AnswerSource answerSource
    ) {
        setHabitStatus(
                habitId,
                recordDate,
                habitStatus,
                answerSource,
                null
        );
    }

    public void setHabitStatus(
            long habitId,
            String recordDate,
            HabitStatus habitStatus,
            AnswerSource answerSource,
            StatusOperationCallback callback
    ) {
        databaseExecutor.execute(() -> {
            try {
                setHabitStatusBlocking(
                        habitId,
                        recordDate,
                        habitStatus,
                        answerSource
                );

                if (callback != null) {
                    mainHandler.post(
                            callback::onSuccess
                    );
                }

            } catch (Exception exception) {
                Log.e(
                        TAG,
                        "Could not update habit status",
                        exception
                );

                if (callback != null) {
                    mainHandler.post(
                            () -> callback.onError(
                                    exception
                            )
                    );
                }
            }
        });
    }

    @WorkerThread
    public Habit getHabitBlocking(long habitId) {
        return habitDao.getById(habitId);
    }

    @WorkerThread
    public List<Habit> getEnabledHabitsBlocking() {
        return habitDao.getEnabled();
    }

    @WorkerThread
    public HabitRecord getHabitRecordBlocking(
            long habitId,
            String recordDate
    ) {
        return habitRecordDao.getByHabitAndDate(
                habitId,
                recordDate
        );
    }

    @WorkerThread
    public void ensureHabitRecordBlocking(
            long habitId,
            String recordDate
    ) {
        long now = System.currentTimeMillis();

        HabitRecord habitRecord = new HabitRecord(
                habitId,
                recordDate,
                HabitStatus.PENDING,
                null,
                null,
                0,
                now,
                now
        );

        habitRecordDao.insertIgnore(habitRecord);
    }

    @WorkerThread
    public void setHabitStatusBlocking(
            long habitId,
            String recordDate,
            HabitStatus habitStatus,
            AnswerSource answerSource
    ) {
        long now = System.currentTimeMillis();

        Long answeredAt;
        AnswerSource storedAnswerSource;

        if (habitStatus == HabitStatus.PENDING) {
            answeredAt = null;
            storedAnswerSource = null;
        } else {
            answeredAt = now;
            storedAnswerSource = answerSource;
        }

        int updatedRows =
                habitRecordDao.updateStatus(
                        habitId,
                        recordDate,
                        habitStatus,
                        answeredAt,
                        storedAnswerSource,
                        now
                );

        if (updatedRows == 0) {
            HabitRecord newRecord = new HabitRecord(
                    habitId,
                    recordDate,
                    habitStatus,
                    answeredAt,
                    storedAnswerSource,
                    0,
                    now,
                    now
            );

            habitRecordDao.upsert(newRecord);
        }
    }

    @WorkerThread
    public boolean tryIncrementSnoozeCountBlocking(
            long habitId,
            String recordDate
    ) {
        int updatedRows =
                habitRecordDao
                        .incrementSnoozeCountIfAllowed(
                                habitId,
                                recordDate,
                                MAXIMUM_DAILY_SNOOZES,
                                System.currentTimeMillis()
                        );

        return updatedRows == 1;
    }

    private void postError(
            SaveHabitCallback callback,
            String message
    ) {
        mainHandler.post(() ->
                callback.onError(message)
        );
    }

    public LiveData<List<HabitRecord>>
    observeRecordsBetweenDates(
            String startDate,
            String endDate
    ) {
        return habitRecordDao.observeBetweenDates(
                startDate,
                endDate
        );
    }
}