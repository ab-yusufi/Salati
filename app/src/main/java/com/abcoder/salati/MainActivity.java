package com.abcoder.salati;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Date;

import com.abcoder.salati.databinding.ActivityMainBinding;
import com.abcoder.salati.reminder.prayer.PrayerReminderScheduler;
import com.abcoder.salati.ui.habits.HabitManagementActivity;
import com.abcoder.salati.ui.settings.PrayerSettingsActivity;
import com.abcoder.salati.ui.today.PrayerListAdapter;
import com.abcoder.salati.ui.today.TodayHabitAdapter;
import com.abcoder.salati.ui.today.TodayViewModel;
import com.abcoder.salati.ui.today.TodayViewModelFactory;
import com.abcoder.salati.ui.reports.ReportsActivity;

import com.abcoder.salati.notification.NotificationPermissionHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private TodayViewModel todayViewModel;

    private PrayerListAdapter prayerListAdapter;
    private TodayHabitAdapter habitAdapter;

    private final ActivityResultLauncher<String>
            notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts
                            .RequestPermission(),
                    isGranted -> {
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
        configureViewModel();
        configurePrayerList();
        configureHabitList();
        configureDate();
        observePrayerRecords();
        observeHabitRecords();
        configurePrayerSettingsButton();
        configureManageHabitsButton();
        configureReportsButton();
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

    private void configureViewModel() {
        SalatiApplication application =
                (SalatiApplication)
                        getApplication();

        TodayViewModelFactory factory =
                new TodayViewModelFactory(
                        application
                                .getPrayerRepository(),
                        application
                                .getHabitRepository()
                );

        todayViewModel =
                new ViewModelProvider(this, factory)
                        .get(TodayViewModel.class);
    }

    private void configurePrayerList() {
        prayerListAdapter =
                new PrayerListAdapter(
                        (prayerType, prayerStatus) ->
                                todayViewModel
                                        .setPrayerStatus(
                                                prayerType,
                                                prayerStatus
                                        )
                );

        binding.prayerList.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.prayerList.setAdapter(
                prayerListAdapter
        );

        binding.prayerList.setNestedScrollingEnabled(
                false
        );
    }

    private void configureHabitList() {
        habitAdapter =
                new TodayHabitAdapter(
                        (habitId, status) ->
                                todayViewModel
                                        .setHabitStatus(
                                                habitId,
                                                status
                                        )
                );

        binding.habitList.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.habitList.setAdapter(habitAdapter);

        binding.habitList.setNestedScrollingEnabled(
                false
        );
    }

    private void configureDate() {
        binding.todayDateText.setText(
                todayViewModel.getDisplayDate()
        );
    }

    private void observePrayerRecords() {
        todayViewModel
                .getTodayPrayerRecords()
                .observe(
                        this,
                        prayerListAdapter::submitList
                );
    }

    private void observeHabitRecords() {
        todayViewModel
                .getTodayHabitItems()
                .observe(
                        this,
                        items -> {
                            habitAdapter.submitList(items);

                            boolean empty =
                                    items == null
                                            || items.isEmpty();

                            binding.noHabitsText
                                    .setVisibility(
                                            empty
                                                    ? View.VISIBLE
                                                    : View.GONE
                                    );

                            binding.habitList
                                    .setVisibility(
                                            empty
                                                    ? View.GONE
                                                    : View.VISIBLE
                                    );
                        }
                );
    }

    private void configurePrayerSettingsButton() {
        binding.prayerSettingsButton
                .setOnClickListener(view ->
                        startActivity(
                                new Intent(
                                        this,
                                        PrayerSettingsActivity
                                                .class
                                )
                        )
                );
    }

    private void configureManageHabitsButton() {
        binding.manageHabitsButton
                .setOnClickListener(view ->
                        startActivity(
                                new Intent(
                                        this,
                                        HabitManagementActivity
                                                .class
                                )
                        )
                );
    }

    private void configureReportsButton() {
        binding.reportsButton.setOnClickListener(
                view -> startActivity(
                        new Intent(
                                this,
                                ReportsActivity.class
                        )
                )
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

        /*
         * Mark it immediately so dismissing the explanation
         * does not cause it to appear every time the app opens.
         */
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
}