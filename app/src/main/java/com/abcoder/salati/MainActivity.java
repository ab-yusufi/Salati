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
                        if (Boolean.TRUE.equals(isGranted)) {
                            scheduleTestReminder();
                        } else {
                            Toast.makeText(
                                    this,
                                    R.string
                                            .notification_permission_denied,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
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
        configureTestReminderButton();
        configureReportsButton();
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

    private void configureTestReminderButton() {
        binding.testReminderButton
                .setOnClickListener(view ->
                        requestPermissionAndSchedule()
                );
    }

    private void requestPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission
                        .POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            notificationPermissionLauncher.launch(
                    Manifest.permission
                            .POST_NOTIFICATIONS
            );

            return;
        }

        scheduleTestReminder();
    }

    private void scheduleTestReminder() {
        if (!NotificationManagerCompat
                .from(this)
                .areNotificationsEnabled()) {

            Toast.makeText(
                    this,
                    R.string.notifications_disabled,
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        try {
            long triggerAtMillis =
                    PrayerReminderScheduler
                            .scheduleTestReminder(this);

            String formattedTime =
                    DateFormat
                            .getTimeFormat(this)
                            .format(
                                    new Date(
                                            triggerAtMillis
                                    )
                            );

            Toast.makeText(
                    this,
                    getString(
                            R.string
                                    .test_reminder_scheduled,
                            formattedTime
                    ),
                    Toast.LENGTH_LONG
            ).show();

        } catch (RuntimeException exception) {
            Toast.makeText(
                    this,
                    R.string
                            .test_reminder_schedule_failed,
                    Toast.LENGTH_LONG
            ).show();
        }
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
}