package com.abcoder.salati.ui.settings;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.entity.PrayerReminderSetting;
import com.abcoder.salati.databinding.ActivityPrayerSettingsBinding;
import com.abcoder.salati.reminder.prayer.PrayerReminderScheduler;

public class PrayerSettingsActivity
        extends AppCompatActivity {

    private ActivityPrayerSettingsBinding binding;

    private PrayerSettingsViewModel viewModel;

    private PrayerReminderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding =
                ActivityPrayerSettingsBinding.inflate(
                        getLayoutInflater()
                );

        setContentView(binding.getRoot());

        configureSystemBarInsets();
        configureViewModel();
        configureReminderList();
        configureBackButton();
        observeReminderSettings();
    }

    private void configureSystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
                binding.main,
                (view, windowInsets) -> {
                    Insets systemBars =
                            windowInsets.getInsets(
                                    WindowInsetsCompat
                                            .Type
                                            .systemBars()
                            );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return windowInsets;
                }
        );
    }

    private void configureViewModel() {
        SalatiApplication application =
                (SalatiApplication)
                        getApplication();

        PrayerSettingsViewModelFactory factory =
                new PrayerSettingsViewModelFactory(
                        application.getPrayerRepository()
                );

        viewModel =
                new ViewModelProvider(this, factory)
                        .get(
                                PrayerSettingsViewModel.class
                        );
    }

    private void configureReminderList() {
        adapter = new PrayerReminderAdapter(
                new PrayerReminderAdapter
                        .ReminderActionListener() {

                    @Override
                    public void onEnabledChanged(
                            PrayerReminderSetting setting,
                            boolean enabled
                    ) {
                        saveAndApplySetting(
                                new PrayerReminderSetting(
                                        setting.prayerType,
                                        setting.hour,
                                        setting.minute,
                                        enabled
                                )
                        );
                    }

                    @Override
                    public void onTimeClicked(
                            PrayerReminderSetting setting
                    ) {
                        showTimePicker(setting);
                    }
                }
        );

        binding.reminderList.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.reminderList.setAdapter(adapter);
    }

    private void configureBackButton() {
        binding.backButton.setOnClickListener(
                view -> finish()
        );
    }

    private void observeReminderSettings() {
        viewModel
                .getReminderSettings()
                .observe(
                        this,
                        adapter::submitList
                );
    }

    private void showTimePicker(
            PrayerReminderSetting setting
    ) {
        boolean use24HourClock =
                DateFormat.is24HourFormat(this);

        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        this,
                        (timePicker, selectedHour, selectedMinute) ->
                                saveAndApplySetting(
                                        new PrayerReminderSetting(
                                                setting.prayerType,
                                                selectedHour,
                                                selectedMinute,
                                                setting.enabled
                                        )
                                ),
                        setting.hour,
                        setting.minute,
                        use24HourClock
                );

        timePickerDialog.show();
    }
    private void saveAndApplySetting(
            PrayerReminderSetting setting
    ) {
        viewModel.saveSetting(
                setting.prayerType,
                setting.hour,
                setting.minute,
                setting.enabled
        );

        PrayerReminderScheduler.applySetting(
                getApplicationContext(),
                setting
        );
    }
}