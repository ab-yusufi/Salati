package com.abcoder.salati.ui.settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.abcoder.salati.data.repository.PrayerRepository;

public final class PrayerSettingsViewModelFactory
        implements ViewModelProvider.Factory {

    private final PrayerRepository prayerRepository;

    public PrayerSettingsViewModelFactory(
            PrayerRepository prayerRepository
    ) {
        this.prayerRepository = prayerRepository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass,
            @NonNull CreationExtras extras
    ) {
        if (modelClass.isAssignableFrom(
                PrayerSettingsViewModel.class
        )) {
            return (T) new PrayerSettingsViewModel(
                    prayerRepository
            );
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class: "
                        + modelClass.getName()
        );
    }
}