package com.abcoder.salati.ui.habits;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.reminder.habit.HabitReminderScheduler;

public class HabitManagementViewModel
        extends AndroidViewModel {

    private final HabitRepository habitRepository;

    private final LiveData<List<Habit>> habits;

    public HabitManagementViewModel(
            @NonNull Application application,
            HabitRepository habitRepository
    ) {
        super(application);

        this.habitRepository = habitRepository;
        habits = habitRepository.observeAllHabits();
    }

    public LiveData<List<Habit>> getHabits() {
        return habits;
    }

    public void saveHabit(
            Habit habit,
            HabitRepository.SaveHabitCallback callback
    ) {
        habitRepository.saveHabit(
                habit,
                new HabitRepository.SaveHabitCallback() {

                    @Override
                    public void onSuccess(
                            Habit savedHabit
                    ) {
                        HabitReminderScheduler.applyHabit(
                                getApplication(),
                                savedHabit
                        );

                        callback.onSuccess(savedHabit);
                    }

                    @Override
                    public void onLimitReached() {
                        callback.onLimitReached();
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                }
        );
    }

    public void deleteHabit(
            Habit habit,
            HabitRepository.DeleteHabitCallback callback
    ) {
        HabitReminderScheduler.cancelAll(
                getApplication(),
                habit.id
        );

        habitRepository.deleteHabit(
                habit.id,
                new HabitRepository
                        .DeleteHabitCallback() {

                    @Override
                    public void onSuccess() {
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String message) {
                        /*
                         * Restore the alarm when deletion failed.
                         */
                        HabitReminderScheduler.applyHabit(
                                getApplication(),
                                habit
                        );

                        callback.onError(message);
                    }
                }
        );
    }
}