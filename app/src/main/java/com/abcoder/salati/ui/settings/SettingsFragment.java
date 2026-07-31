package com.abcoder.salati.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.abcoder.salati.R;
import com.abcoder.salati.databinding.FragmentSettingsBinding;
import com.abcoder.salati.ui.habits.HabitManagementActivity;
import com.abcoder.salati.ui.theme.ThemeManager;
import com.abcoder.salati.ui.theme.ThemeMode;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

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
    }

    @Override
    public void onResume() {
        super.onResume();

        if (binding != null) {
            updateAppearanceSummary();
        }
    }

    private void configureButtons() {
        binding.appearanceButton.setOnClickListener(
                view -> showAppearanceDialog()
        );

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
                .setTitle(R.string.appearance_title)
                .setSingleChoiceItems(
                        labels,
                        checkedItem,
                        (dialog, which) -> {
                            ThemeMode selectedTheme =
                                    themeModes[which];

                            dialog.dismiss();

                            ThemeManager.saveAndApplyTheme(
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
                        getString(R.string.theme_light);
                break;

            case DARK:
                themeLabel =
                        getString(R.string.theme_dark);
                break;

            case SYSTEM:
            default:
                themeLabel =
                        getString(R.string.theme_system);
                break;
        }

        binding.appearanceSummaryText.setText(
                getString(
                        R.string.current_theme_format,
                        themeLabel
                )
        );
    }

    private int getThemeModeIndex(
            ThemeMode[] themeModes,
            ThemeMode selectedTheme
    ) {
        for (int index = 0;
             index < themeModes.length;
             index++) {

            if (themeModes[index] == selectedTheme) {
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