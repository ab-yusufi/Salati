package com.abcoder.salati.ui.settings;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.abcoder.salati.BuildConfig;
import com.abcoder.salati.R;
import com.abcoder.salati.databinding.FragmentSettingsBinding;
import com.abcoder.salati.notification.NotificationPermissionHelper;
import com.abcoder.salati.ui.habits.HabitManagementActivity;
import com.abcoder.salati.ui.theme.ThemeManager;
import com.abcoder.salati.ui.theme.ThemeMode;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public final class SettingsFragment
        extends Fragment {

    private FragmentSettingsBinding binding;

    private final ActivityResultLauncher<String>
            notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts
                            .RequestPermission(),
                    isGranted -> {
                        if (binding == null) {
                            return;
                        }

                        updateNotificationStatus();

                        int messageResource =
                                Boolean.TRUE.equals(
                                        isGranted
                                )
                                        ? R.string
                                          .settings_notifications_enabled_message
                                        : R.string
                                          .settings_notifications_denied_message;

                        Snackbar.make(
                                binding.settingsRoot,
                                messageResource,
                                Snackbar.LENGTH_LONG
                        ).show();
                    }
            );

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding =
                FragmentSettingsBinding.inflate(
                        inflater,
                        container,
                        false
                );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(
                view,
                savedInstanceState
        );

        configureButtons();
        updateAppearanceSummary();
        updateNotificationStatus();
        updateVersionText();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (binding == null) {
            return;
        }

        /*
         * Notification access may have changed in Android
         * Settings while Salati was paused.
         */
        updateNotificationStatus();
        updateAppearanceSummary();
        updateVersionText();
    }

    private void configureButtons() {
        binding.prayerSettingsButton
                .setOnClickListener(
                        view -> startActivity(
                                new Intent(
                                        requireContext(),
                                        PrayerSettingsActivity.class
                                )
                        )
                );

        binding.manageHabitsButton
                .setOnClickListener(
                        view -> startActivity(
                                new Intent(
                                        requireContext(),
                                        HabitManagementActivity.class
                                )
                        )
                );

        binding.notificationActionButton
                .setOnClickListener(
                        view ->
                                handleNotificationAction()
                );

        binding.appearanceButton
                .setOnClickListener(
                        view ->
                                showAppearanceDialog()
                );

        binding.aboutButton
                .setOnClickListener(
                        view ->
                                showAboutDialog()
                );

        binding.privacyButton
                .setOnClickListener(
                        view ->
                                showPrivacyDialog()
                );
    }

    private void handleNotificationAction() {
        if (NotificationPermissionHelper
                .areNotificationsEnabled(
                        requireContext()
                )) {

            NotificationPermissionHelper
                    .openNotificationSettings(
                            requireContext()
                    );

            return;
        }

        if (NotificationPermissionHelper
                .requiresRuntimePermission()
                && !NotificationPermissionHelper
                .hasRuntimePermission(
                        requireContext()
                )) {

            boolean rationaleAvailable =
                    shouldShowRequestPermissionRationale(
                            Manifest.permission
                                    .POST_NOTIFICATIONS
                    );

            boolean firstRequest =
                    !NotificationPermissionHelper
                            .wasPermissionRequested(
                                    requireContext()
                            );

            if (firstRequest
                    || rationaleAvailable) {

                showNotificationPermissionExplanation();

            } else {
                NotificationPermissionHelper
                        .openNotificationSettings(
                                requireContext()
                        );
            }

            return;
        }

        /*
         * Runtime permission exists, but Android-level app
         * notifications or channels are disabled.
         */
        NotificationPermissionHelper
                .openNotificationSettings(
                        requireContext()
                );
    }

    private void showNotificationPermissionExplanation() {
        new MaterialAlertDialogBuilder(
                requireContext()
        )
                .setTitle(
                        R.string
                                .notification_onboarding_title
                )
                .setMessage(
                        R.string
                                .settings_notification_explanation
                )
                .setPositiveButton(
                        R.string
                                .notification_onboarding_allow,
                        (dialog, which) ->
                                requestNotificationPermission()
                )
                .setNegativeButton(
                        R.string.cancel,
                        null
                )
                .show();
    }

    private void requestNotificationPermission() {
        NotificationPermissionHelper
                .markPermissionRequested(
                        requireContext()
                );

        notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
        );
    }

    private void updateNotificationStatus() {
        if (binding == null) {
            return;
        }

        boolean enabled =
                NotificationPermissionHelper
                        .areNotificationsEnabled(
                                requireContext()
                        );

        binding.notificationStatusText.setText(
                enabled
                        ? R.string.notification_status_enabled
                        : R.string.notification_status_disabled
        );

        binding.notificationStatusSummaryText
                .setText(
                        enabled
                                ? R.string
                                  .settings_notifications_enabled_summary
                                : R.string
                                  .settings_notifications_disabled_summary
                );

        int statusColorAttribute =
                enabled
                        ? com.google.android.material
                          .R.attr.colorPrimary
                        : com.google.android.material
                          .R.attr.colorError;

        int statusColor =
                MaterialColors.getColor(
                        binding.notificationStatusText,
                        statusColorAttribute,
                        ContextCompat.getColor(
                                requireContext(),
                                enabled
                                        ? R.color
                                          .prayer_status_on_time
                                        : R.color
                                          .prayer_status_missed
                        )
                );

        binding.notificationStatusText
                .setTextColor(statusColor);

        binding.notificationStatusCard
                .setStrokeColor(statusColor);

        binding.notificationActionButton
                .setText(
                        getNotificationActionLabel(
                                enabled
                        )
                );
    }

    private int getNotificationActionLabel(
            boolean notificationsEnabled
    ) {
        if (notificationsEnabled) {
            return R.string.manage_notifications;
        }

        boolean missingRuntimePermission =
                NotificationPermissionHelper
                        .requiresRuntimePermission()
                        && !NotificationPermissionHelper
                        .hasRuntimePermission(
                                requireContext()
                        );

        if (missingRuntimePermission) {
            boolean rationaleAvailable =
                    shouldShowRequestPermissionRationale(
                            Manifest.permission
                                    .POST_NOTIFICATIONS
                    );

            boolean firstRequest =
                    !NotificationPermissionHelper
                            .wasPermissionRequested(
                                    requireContext()
                            );

            if (firstRequest
                    || rationaleAvailable) {

                return R.string.allow_notifications;
            }
        }

        return R.string.open_notification_settings;
    }

    private void showAppearanceDialog() {
        ThemeMode[] themeModes = {
                ThemeMode.SYSTEM,
                ThemeMode.LIGHT,
                ThemeMode.DARK
        };

        String[] labels = {
                getString(R.string.theme_system),
                getString(R.string.theme_light),
                getString(R.string.theme_dark)
        };

        ThemeMode currentTheme =
                ThemeManager.getSavedTheme(
                        requireContext()
                );

        int checkedItem =
                getThemeModeIndex(
                        themeModes,
                        currentTheme
                );

        new MaterialAlertDialogBuilder(
                requireContext()
        )
                .setTitle(
                        R.string.appearance_title
                )
                .setSingleChoiceItems(
                        labels,
                        checkedItem,
                        (dialog, which) -> {
                            ThemeMode selectedTheme =
                                    themeModes[which];

                            dialog.dismiss();

                            ThemeManager
                                    .saveAndApplyTheme(
                                            requireContext(),
                                            selectedTheme
                                    );
                        }
                )
                .setNegativeButton(
                        R.string.cancel,
                        null
                )
                .show();
    }

    private void updateAppearanceSummary() {
        ThemeMode themeMode =
                ThemeManager.getSavedTheme(
                        requireContext()
                );

        String themeLabel;

        switch (themeMode) {
            case LIGHT:
                themeLabel =
                        getString(
                                R.string.theme_light
                        );
                break;

            case DARK:
                themeLabel =
                        getString(
                                R.string.theme_dark
                        );
                break;

            case SYSTEM:
            default:
                themeLabel =
                        getString(
                                R.string.theme_system
                        );
                break;
        }

        binding.appearanceSummaryText.setText(
                getString(
                        R.string.current_theme_format,
                        themeLabel
                )
        );
    }

    private void updateVersionText() {
        binding.versionText.setText(
                getString(
                        R.string.app_version_format,
                        BuildConfig.VERSION_NAME
                )
        );
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(
                requireContext()
        )
                .setTitle(
                        R.string.about_salati
                )
                .setMessage(
                        getString(
                                R.string.about_salati_message,
                                BuildConfig.VERSION_NAME
                        )
                )
                .setPositiveButton(
                        R.string.close,
                        null
                )
                .show();
    }

    private void showPrivacyDialog() {
        new MaterialAlertDialogBuilder(
                requireContext()
        )
                .setTitle(
                        R.string.privacy_information
                )
                .setMessage(
                        R.string.privacy_information_message
                )
                .setPositiveButton(
                        R.string.close,
                        null
                )
                .show();
    }

    private int getThemeModeIndex(
            ThemeMode[] themeModes,
            ThemeMode selectedTheme
    ) {
        for (int index = 0;
             index < themeModes.length;
             index++) {

            if (themeModes[index]
                    == selectedTheme) {

                return index;
            }
        }

        return 0;
    }

    @Override
    public void onDestroyView() {
        binding = null;

        super.onDestroyView();
    }
}