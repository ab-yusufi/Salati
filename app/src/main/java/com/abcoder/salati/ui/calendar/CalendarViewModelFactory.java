package com.abcoder.salati.ui.calendar;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.data.repository.PrayerRepository;

public final class CalendarViewModelFactory
        implements ViewModelProvider.Factory {

    private final PrayerRepository prayerRepository;
    private final HabitRepository habitRepository;

    public CalendarViewModelFactory(
            PrayerRepository prayerRepository,
            HabitRepository habitRepository
    ) {
        this.prayerRepository =
                prayerRepository;

        this.habitRepository =
                habitRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass
    ) {
        if (modelClass.isAssignableFrom(
                CalendarViewModel.class
        )) {
            return (T) new CalendarViewModel(
                    prayerRepository,
                    habitRepository
            );
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class: "
                        + modelClass.getName()
        );
    }
}