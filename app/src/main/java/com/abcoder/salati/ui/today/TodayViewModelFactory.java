package com.abcoder.salati.ui.today;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.data.repository.PrayerRepository;

public final class TodayViewModelFactory
        implements ViewModelProvider.Factory {

    private final PrayerRepository prayerRepository;
    private final HabitRepository habitRepository;

    public TodayViewModelFactory(
            PrayerRepository prayerRepository,
            HabitRepository habitRepository
    ) {
        this.prayerRepository = prayerRepository;
        this.habitRepository = habitRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass,
            @NonNull CreationExtras extras
    ) {
        if (modelClass.isAssignableFrom(
                TodayViewModel.class
        )) {
            return (T) new TodayViewModel(
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