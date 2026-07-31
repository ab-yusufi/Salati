package com.abcoder.salati.ui.reports;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abcoder.salati.R;
import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.databinding.ActivityReportsBinding;

public final class ReportsActivity
        extends AppCompatActivity {

    private ActivityReportsBinding binding;

    private ReportsViewModel viewModel;

    private PrayerStatisticsAdapter
            prayerStatisticsAdapter;

    private HabitStatisticsAdapter
            habitStatisticsAdapter;

    private DailyStatisticsAdapter
            dailyStatisticsAdapter;

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityReportsBinding.inflate(
                getLayoutInflater()
        );

        setContentView(binding.getRoot());

        configureSystemBars();
        configureViewModel();
        configureLists();
        configureButtons();
        observeUiState();
    }

    private void configureSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(
                binding.main,
                (view, insets) -> {
                    Insets systemBars =
                            insets.getInsets(
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

                    return insets;
                }
        );
    }

    private void configureViewModel() {
        SalatiApplication application =
                (SalatiApplication)
                        getApplication();

        ReportsViewModelFactory factory =
                new ReportsViewModelFactory(
                        application
                                .getPrayerRepository(),
                        application
                                .getHabitRepository()
                );

        viewModel =
                new ViewModelProvider(this, factory)
                        .get(ReportsViewModel.class);
    }

    private void configureLists() {
        prayerStatisticsAdapter =
                new PrayerStatisticsAdapter();

        habitStatisticsAdapter =
                new HabitStatisticsAdapter();

        dailyStatisticsAdapter =
                new DailyStatisticsAdapter();

        binding.prayerBreakdownList
                .setLayoutManager(
                        new LinearLayoutManager(this)
                );

        binding.prayerBreakdownList
                .setAdapter(
                        prayerStatisticsAdapter
                );

        binding.habitBreakdownList
                .setLayoutManager(
                        new LinearLayoutManager(this)
                );

        binding.habitBreakdownList
                .setAdapter(
                        habitStatisticsAdapter
                );

        binding.dailyBreakdownList
                .setLayoutManager(
                        new LinearLayoutManager(this)
                );

        binding.dailyBreakdownList
                .setAdapter(
                        dailyStatisticsAdapter
                );
    }

    private void configureButtons() {
        binding.backButton.setOnClickListener(
                view -> finish()
        );

        binding.weekButton.setOnClickListener(
                view -> viewModel.selectWeek()
        );

        binding.monthButton.setOnClickListener(
                view -> viewModel.selectMonth()
        );

        binding.previousButton.setOnClickListener(
                view -> viewModel.movePrevious()
        );

        binding.nextButton.setOnClickListener(
                view -> viewModel.moveNext()
        );
    }

    private void observeUiState() {
        viewModel.getUiState().observe(
                this,
                this::displayUiState
        );
    }

    private void displayUiState(
            ReportsUiState state
    ) {
        if (state == null) {
            return;
        }

        binding.rangeText.setText(
                state.rangeLabel
        );

        binding.periodToggleGroup.check(
                state.period
                        == ReportsUiState
                        .Period.WEEK
                        ? R.id.weekButton
                        : R.id.monthButton
        );

        binding.nextButton.setEnabled(
                state.canGoNext
        );

        binding.noDataText.setVisibility(
                state.hasData
                        ? View.GONE
                        : View.VISIBLE
        );

        ReportsUiState.PrayerSummary prayer =
                state.prayerSummary;

        binding.prayerSummaryText.setText(
                getString(
                        R.string.prayer_summary_format,
                        prayer.trackedDays,
                        prayer.onTime,
                        prayer.late,
                        prayer.missed,
                        prayer.unrecorded,
                        prayer.onTimePercentage
                )
        );

        ReportsUiState.HabitSummary habit =
                state.habitSummary;

        binding.habitSummaryText.setText(
                getString(
                        R.string.habit_summary_format,
                        habit.trackedRecords,
                        habit.completed,
                        habit.notCompleted,
                        habit.pending,
                        habit.completionPercentage
                )
        );

        prayerStatisticsAdapter.submitList(
                state.prayerBreakdown
        );

        habitStatisticsAdapter.submitList(
                state.habitBreakdown
        );

        dailyStatisticsAdapter.submitList(
                state.dailyBreakdown
        );
    }
}