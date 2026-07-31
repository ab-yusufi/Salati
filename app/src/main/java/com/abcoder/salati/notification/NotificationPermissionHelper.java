package com.abcoder.salati.notification;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public final class NotificationPermissionHelper {

    private static final String PREFERENCES_NAME =
            "notification_permission_preferences";

    private static final String KEY_ONBOARDING_SHOWN =
            "notification_onboarding_shown";

    private static final String KEY_PERMISSION_REQUESTED =
            "notification_permission_requested";

    private NotificationPermissionHelper() {
        // Prevent instantiation.
    }

    public static boolean requiresRuntimePermission() {
        return Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU;
    }

    public static boolean hasRuntimePermission(
            Context context
    ) {
        if (!requiresRuntimePermission()) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean areNotificationsEnabled(
            Context context
    ) {
        return hasRuntimePermission(context)
                && NotificationManagerCompat
                .from(context)
                .areNotificationsEnabled();
    }

    public static boolean wasOnboardingShown(
            Context context
    ) {
        return getPreferences(context)
                .getBoolean(
                        KEY_ONBOARDING_SHOWN,
                        false
                );
    }

    public static void markOnboardingShown(
            Context context
    ) {
        getPreferences(context)
                .edit()
                .putBoolean(
                        KEY_ONBOARDING_SHOWN,
                        true
                )
                .apply();
    }

    public static boolean wasPermissionRequested(
            Context context
    ) {
        return getPreferences(context)
                .getBoolean(
                        KEY_PERMISSION_REQUESTED,
                        false
                );
    }

    public static void markPermissionRequested(
            Context context
    ) {
        getPreferences(context)
                .edit()
                .putBoolean(
                        KEY_PERMISSION_REQUESTED,
                        true
                )
                .apply();
    }

    public static void openNotificationSettings(
            Context context
    ) {
        Intent notificationSettingsIntent =
                new Intent(
                        Settings
                                .ACTION_APP_NOTIFICATION_SETTINGS
                );

        notificationSettingsIntent.putExtra(
                Settings.EXTRA_APP_PACKAGE,
                context.getPackageName()
        );

        notificationSettingsIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
        );

        try {
            context.startActivity(
                    notificationSettingsIntent
            );

        } catch (ActivityNotFoundException exception) {
            Intent appSettingsIntent =
                    new Intent(
                            Settings
                                    .ACTION_APPLICATION_DETAILS_SETTINGS
                    );

            appSettingsIntent.setData(
                    Uri.parse(
                            "package:"
                                    + context.getPackageName()
                    )
            );

            appSettingsIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            context.startActivity(appSettingsIntent);
        }
    }

    private static SharedPreferences getPreferences(
            Context context
    ) {
        return context.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
    }
}