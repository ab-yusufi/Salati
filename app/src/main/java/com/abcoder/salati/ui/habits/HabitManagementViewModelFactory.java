package com.abcoder.salati.ui.habits;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.abcoder.salati.SalatiApplication;

public final class HabitManagementViewModelFactory
        implements ViewModelProvider.Factory {

    private final SalatiApplication application;

    public HabitManagementViewModelFactory(
            SalatiApplication application
    ) {
        this.application = application;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass,
            @NonNull CreationExtras extras
    ) {
        if (modelClass.isAssignableFrom(
                HabitManagementViewModel.class
        )) {
            return (T) new HabitManagementViewModel(
                    application,
                    application.getHabitRepository()
            );
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class: "
                        + modelClass.getName()
        );
    }
}