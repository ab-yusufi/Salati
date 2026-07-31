package com.abcoder.salati;

import android.Manifest;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.abcoder.salati.databinding.ActivityMainBinding;
import com.abcoder.salati.notification.NotificationPermissionHelper;
import com.abcoder.salati.ui.calendar.CalendarFragment;
import com.abcoder.salati.ui.insights.InsightsFragment;
import com.abcoder.salati.ui.settings.SettingsFragment;
import com.abcoder.salati.ui.today.TodayFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;

public class MainActivity extends AppCompatActivity {

    private static final String STATE_SELECTED_ITEM =
            "selected_bottom_navigation_item";

    private static final String TAG_TODAY =
            "main_today";

    private static final String TAG_CALENDAR =
            "main_calendar";

    private static final String TAG_INSIGHTS =
            "main_insights";

    private static final String TAG_SETTINGS =
            "main_settings";

    private ActivityMainBinding binding;

    private int selectedItemId =
            R.id.navigation_today;

    private final ActivityResultLauncher<String>
            notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts
                            .RequestPermission(),
                    isGranted -> {
                        int messageResource =
                                Boolean.TRUE.equals(
                                        isGranted
                                )
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
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding =
                ActivityMainBinding.inflate(
                        getLayoutInflater()
                );

        setContentView(binding.getRoot());

        configureSystemBarInsets();
        configureBottomNavigation(
                savedInstanceState
        );
        maybeShowNotificationPermissionOnboarding();
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

    private void configureBottomNavigation(
            Bundle savedInstanceState
    ) {
        if (savedInstanceState != null) {
            selectedItemId =
                    savedInstanceState.getInt(
                            STATE_SELECTED_ITEM,
                            R.id.navigation_today
                    );
        }

        binding.bottomNavigation
                .setOnItemSelectedListener(
                        item -> {
                            int itemId = item.getItemId();

                            if (!isMainDestination(itemId)) {
                                return false;
                            }

                            selectedItemId = itemId;
                            showDestination(itemId);

                            return true;
                        }
                );

        binding.bottomNavigation
                .getMenu()
                .findItem(selectedItemId)
                .setChecked(true);

        showDestination(selectedItemId);
    }

    private boolean isMainDestination(
            int itemId
    ) {
        return itemId == R.id.navigation_today
                || itemId == R.id.navigation_calendar
                || itemId == R.id.navigation_insights
                || itemId == R.id.navigation_settings;
    }

    private void showDestination(
            int itemId
    ) {
        FragmentManager fragmentManager =
                getSupportFragmentManager();

        String targetTag =
                getFragmentTag(itemId);

        Fragment targetFragment =
                fragmentManager
                        .findFragmentByTag(
                                targetTag
                        );

        FragmentTransaction transaction =
                fragmentManager
                        .beginTransaction()
                        .setReorderingAllowed(true);

        for (Fragment fragment :
                fragmentManager.getFragments()) {

            if (fragment.isAdded()) {
                transaction.hide(fragment);
            }
        }

        if (targetFragment == null) {
            targetFragment =
                    createFragment(itemId);

            transaction.add(
                    R.id.fragmentContainer,
                    targetFragment,
                    targetTag
            );

        } else {
            transaction.show(targetFragment);
        }

        transaction.commit();
    }

    private Fragment createFragment(
            int itemId
    ) {
        if (itemId == R.id.navigation_today) {
            return new TodayFragment();
        }

        if (itemId == R.id.navigation_calendar) {
            return new CalendarFragment();
        }

        if (itemId == R.id.navigation_insights) {
            return new InsightsFragment();
        }

        if (itemId == R.id.navigation_settings) {
            return new SettingsFragment();
        }

        throw new IllegalArgumentException(
                "Unknown navigation destination: "
                        + itemId
        );
    }

    private String getFragmentTag(
            int itemId
    ) {
        if (itemId == R.id.navigation_today) {
            return TAG_TODAY;
        }

        if (itemId == R.id.navigation_calendar) {
            return TAG_CALENDAR;
        }

        if (itemId == R.id.navigation_insights) {
            return TAG_INSIGHTS;
        }

        if (itemId == R.id.navigation_settings) {
            return TAG_SETTINGS;
        }

        throw new IllegalArgumentException(
                "Unknown navigation destination: "
                        + itemId
        );
    }

    private void maybeShowNotificationPermissionOnboarding() {
        if (!NotificationPermissionHelper
                .requiresRuntimePermission()) {
            return;
        }

        if (NotificationPermissionHelper
                .hasRuntimePermission(this)) {
            return;
        }

        if (NotificationPermissionHelper
                .wasOnboardingShown(this)) {
            return;
        }

        NotificationPermissionHelper
                .markOnboardingShown(this);

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        R.string
                                .notification_onboarding_title
                )
                .setMessage(
                        R.string
                                .notification_onboarding_message
                )
                .setPositiveButton(
                        R.string
                                .notification_onboarding_allow,
                        (dialog, which) ->
                                requestNotificationPermission()
                )
                .setNegativeButton(
                        R.string
                                .notification_onboarding_not_now,
                        null
                )
                .show();
    }

    private void requestNotificationPermission() {
        NotificationPermissionHelper
                .markPermissionRequested(this);

        notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
        );
    }

    @Override
    protected void onSaveInstanceState(
            Bundle outState
    ) {
        outState.putInt(
                STATE_SELECTED_ITEM,
                selectedItemId
        );

        super.onSaveInstanceState(outState);
    }
    public void openCalendarForDate(
            LocalDate selectedDate
    ) {
        Bundle result = new Bundle();

        result.putString(
                CalendarFragment
                        .BUNDLE_KEY_SELECTED_DATE,
                selectedDate.toString()
        );

        getSupportFragmentManager()
                .setFragmentResult(
                        CalendarFragment
                                .REQUEST_KEY_SELECTED_DATE,
                        result
                );

        binding.bottomNavigation
                .setSelectedItemId(
                        R.id.navigation_calendar
                );
    }
}