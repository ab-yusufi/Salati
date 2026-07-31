package com.abcoder.salati.ui.theme;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeManager {

    private static final String PREFERENCES_NAME =
            "appearance_preferences";

    private static final String KEY_THEME_MODE =
            "theme_mode";

    private ThemeManager() {
        // Prevent instantiation.
    }

    public static ThemeMode getSavedTheme(
            Context context
    ) {
        SharedPreferences preferences =
                context.getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                );

        String savedValue =
                preferences.getString(
                        KEY_THEME_MODE,
                        ThemeMode.SYSTEM.name()
                );

        try {
            return ThemeMode.valueOf(savedValue);

        } catch (IllegalArgumentException exception) {
            return ThemeMode.SYSTEM;
        }
    }

    public static void saveAndApplyTheme(
            Context context,
            ThemeMode themeMode
    ) {
        context.getSharedPreferences(
                        PREFERENCES_NAME,
                        Context.MODE_PRIVATE
                )
                .edit()
                .putString(
                        KEY_THEME_MODE,
                        themeMode.name()
                )
                .apply();

        applyTheme(themeMode);
    }

    public static void applySavedTheme(
            Context context
    ) {
        applyTheme(
                getSavedTheme(context)
        );
    }

    private static void applyTheme(
            ThemeMode themeMode
    ) {
        switch (themeMode) {
            case LIGHT:
                AppCompatDelegate
                        .setDefaultNightMode(
                                AppCompatDelegate
                                        .MODE_NIGHT_NO
                        );
                break;

            case DARK:
                AppCompatDelegate
                        .setDefaultNightMode(
                                AppCompatDelegate
                                        .MODE_NIGHT_YES
                        );
                break;

            case SYSTEM:
            default:
                AppCompatDelegate
                        .setDefaultNightMode(
                                AppCompatDelegate
                                        .MODE_NIGHT_FOLLOW_SYSTEM
                        );
                break;
        }
    }
}