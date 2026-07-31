package com.abcoder.salati.ui.settings;

import android.Manifest;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.abcoder.salati.R;
import com.abcoder.salati.notification.NotificationPermissionHelper;
import com.google.android.material.snackbar.Snackbar;

public class PrayerSettingsActivity
        extends AppCompatActivity {

    private ActivityPrayerSettingsBinding binding;

    private PrayerSettingsViewModel viewModel;

    private PrayerReminderAdapter adapter;

    private final ActivityResultLauncher<String>
            notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts
                            .RequestPermission(),
                    isGranted -> {
                        updateNotificationStatus();

                        int messageResource =
                                Boolean.TRUE.equals(isGranted)
                                        ? R.string
                                          .notification_permission_enabled_message
                                        : R.string
                                          .notification_permission_denied_message;

                        Snackbar.make(
                                binding.main,
                                messageResource,
                                Snackbar.LENGTH_LONG
                        ).show();
                    }
            );

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
        configureNotificationAccess();
        observeReminderSettings();
        updateNotificationStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
         * Refresh after the user returns from Android's
         * notification settings.
         */
        updateNotificationStatus();
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

    private void configureNotificationAccess() {
        binding.notificationSettingsButton
                .setOnClickListener(
                        view -> handleNotificationAction()
                );
    }

    private void handleNotificationAction() {
        if (NotificationPermissionHelper
                .areNotificationsEnabled(this)) {

            NotificationPermissionHelper
                    .openNotificationSettings(this);

            return;
        }

        boolean canShowPermissionDialog =
                NotificationPermissionHelper
                        .requiresRuntimePermission()
                        && !NotificationPermissionHelper
                        .hasRuntimePermission(this)
                        && !NotificationPermissionHelper
                        .wasPermissionRequested(this);

        if (canShowPermissionDialog) {
            NotificationPermissionHelper
                    .markPermissionRequested(this);

            notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
            );

            return;
        }

        NotificationPermissionHelper
                .openNotificationSettings(this);
    }

    private void updateNotificationStatus() {
        boolean notificationsEnabled =
                NotificationPermissionHelper
                        .areNotificationsEnabled(this);

        if (notificationsEnabled) {
            binding.notificationStatusText.setText(
                    R.string
                            .notification_status_enabled
            );

            binding.notificationSettingsButton.setText(
                    R.string
                            .notification_action_manage
            );

            return;
        }

        binding.notificationStatusText.setText(
                R.string
                        .notification_status_disabled
        );

        boolean permissionCanBeRequested =
                NotificationPermissionHelper
                        .requiresRuntimePermission()
                        && !NotificationPermissionHelper
                        .hasRuntimePermission(this)
                        && !NotificationPermissionHelper
                        .wasPermissionRequested(this);

        binding.notificationSettingsButton.setText(
                permissionCanBeRequested
                        ? R.string
                          .notification_action_allow
                        : R.string
                          .notification_action_open_settings
        );
    }
}