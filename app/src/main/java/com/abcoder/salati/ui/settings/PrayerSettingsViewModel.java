package com.abcoder.salati.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import com.abcoder.salati.data.entity.PrayerReminderSetting;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.data.repository.PrayerRepository;

public class PrayerSettingsViewModel extends ViewModel {

    private final PrayerRepository prayerRepository;

    private final LiveData<List<PrayerReminderSetting>>
            reminderSettings;

    public PrayerSettingsViewModel(
            PrayerRepository prayerRepository
    ) {
        this.prayerRepository = prayerRepository;

        reminderSettings =
                prayerRepository.observeReminderSettings();
    }

    public LiveData<List<PrayerReminderSetting>>
    getReminderSettings() {
        return reminderSettings;
    }

    public void saveSetting(
            PrayerType prayerType,
            int hour,
            int minute,
            boolean enabled
    ) {
        PrayerReminderSetting setting =
                new PrayerReminderSetting(
                        prayerType,
                        hour,
                        minute,
                        enabled
                );

        prayerRepository.saveReminderSetting(setting);
    }
}