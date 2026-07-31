package com.abcoder.salati.ui.today;

import androidx.annotation.NonNull;

import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.model.HabitStatus;

public final class HabitTodayItem {

    @NonNull
    public final Habit habit;

    @NonNull
    public final HabitStatus status;

    public final int snoozeCount;

    public HabitTodayItem(
            @NonNull Habit habit,
            @NonNull HabitStatus status,
            int snoozeCount
    ) {
        this.habit = habit;
        this.status = status;
        this.snoozeCount = snoozeCount;
    }
}