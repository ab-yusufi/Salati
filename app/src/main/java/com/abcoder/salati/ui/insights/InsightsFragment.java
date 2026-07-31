package com.abcoder.salati.ui.insights;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abcoder.salati.R;
import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.databinding.FragmentInsightsBinding;
import com.abcoder.salati.ui.reports.DailyStatisticsAdapter;
import com.abcoder.salati.ui.reports.HabitStatisticsAdapter;
import com.abcoder.salati.ui.reports.PrayerStatisticsAdapter;
import com.abcoder.salati.ui.reports.ReportsUiState;
import com.abcoder.salati.ui.reports.ReportsViewModel;
import com.abcoder.salati.ui.reports.ReportsViewModelFactory;

public final class InsightsFragment
        extends Fragment {

    private FragmentInsightsBinding binding;

    private ReportsViewModel viewModel;

    private PrayerStatisticsAdapter
            prayerStatisticsAdapter;

    private HabitStatisticsAdapter
            habitStatisticsAdapter;

    private DailyStatisticsAdapter
            dailyStatisticsAdapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding =
                FragmentInsightsBinding.inflate(
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

        configureViewModel();
        configureLists();
        configureControls();
        observeUiState();
    }

    private void configureViewModel() {
        SalatiApplication application =
                (SalatiApplication)
                        requireActivity()
                                .getApplication();

        ReportsViewModelFactory factory =
                new ReportsViewModelFactory(
                        application
                                .getPrayerRepository(),
                        application
                                .getHabitRepository()
                );

        viewModel =
                new ViewModelProvider(
                        this,
                        factory
                ).get(
                        ReportsViewModel.class
                );
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
                        new LinearLayoutManager(
                                requireContext()
                        )
                );

        binding.prayerBreakdownList
                .setAdapter(
                        prayerStatisticsAdapter
                );

        binding.prayerBreakdownList
                .setNestedScrollingEnabled(false);

        binding.habitBreakdownList
                .setLayoutManager(
                        new LinearLayoutManager(
                                requireContext()
                        )
                );

        binding.habitBreakdownList
                .setAdapter(
                        habitStatisticsAdapter
                );

        binding.habitBreakdownList
                .setNestedScrollingEnabled(false);

        binding.dailyBreakdownList
                .setLayoutManager(
                        new LinearLayoutManager(
                                requireContext()
                        )
                );

        binding.dailyBreakdownList
                .setAdapter(
                        dailyStatisticsAdapter
                );

        binding.dailyBreakdownList
                .setNestedScrollingEnabled(false);
    }

    private void configureControls() {
        binding.periodToggleGroup
                .addOnButtonCheckedListener(
                        (group, checkedId, isChecked) -> {
                            if (!isChecked) {
                                return;
                            }

                            if (checkedId
                                    == R.id.weekButton) {

                                viewModel.selectWeek();

                            } else if (
                                    checkedId
                                            == R.id.monthButton
                            ) {
                                viewModel.selectMonth();
                            }
                        }
                );

        binding.previousButton
                .setOnClickListener(
                        view ->
                                viewModel.movePrevious()
                );

        binding.nextButton
                .setOnClickListener(
                        view ->
                                viewModel.moveNext()
                );
    }

    private void observeUiState() {
        viewModel.getUiState()
                .observe(
                        getViewLifecycleOwner(),
                        this::displayUiState
                );
    }

    private void displayUiState(
            ReportsUiState state
    ) {
        if (binding == null
                || state == null) {
            return;
        }

        binding.rangeText.setText(
                state.rangeLabel
        );

        int selectedButton =
                state.period
                        == ReportsUiState.Period.WEEK
                        ? R.id.weekButton
                        : R.id.monthButton;

        if (binding.periodToggleGroup
                .getCheckedButtonId()
                != selectedButton) {

            binding.periodToggleGroup.check(
                    selectedButton
            );
        }

        binding.nextButton.setEnabled(
                state.canGoNext
        );

        binding.nextButton.setAlpha(
                state.canGoNext
                        ? 1f
                        : 0.38f
        );

        binding.noDataCard.setVisibility(
                state.hasData
                        ? View.GONE
                        : View.VISIBLE
        );

        binding.reportSections.setVisibility(
                state.hasData
                        ? View.VISIBLE
                        : View.GONE
        );

        displayPrayerSummary(
                state
        );

        displayHabitSummary(
                state
        );

        displayDailyBreakdown(
                state
        );
    }

    private void displayPrayerSummary(
            ReportsUiState state
    ) {
        ReportsUiState.PrayerSummary prayer =
                state.prayerSummary;

        boolean hasPrayerData =
                prayer.trackedDays > 0;

        binding.prayerSection.setVisibility(
                hasPrayerData
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.noPrayerDataText.setVisibility(
                state.hasData && !hasPrayerData
                        ? View.VISIBLE
                        : View.GONE
        );

        int progress =
                percentageToProgress(
                        prayer.onTimePercentage
                );

        binding.prayerRateText.setText(
                getString(
                        R.string
                                .insights_prayer_rate_format,
                        prayer.onTimePercentage
                )
        );

        binding.prayerRateProgress
                .setProgressCompat(
                        progress,
                        true
                );

        binding.prayerRateProgress
                .setContentDescription(
                        getString(
                                R.string
                                        .insights_prayer_rate_content_description,
                                prayer.onTimePercentage
                        )
                );

        binding.prayerTrackedDaysText.setText(
                getString(
                        R.string
                                .insights_tracked_days_format,
                        prayer.trackedDays
                )
        );

        binding.prayerCountsText.setText(
                getString(
                        R.string
                                .insights_prayer_counts_format,
                        prayer.onTime,
                        prayer.late,
                        prayer.missed,
                        prayer.unrecorded
                )
        );

        prayerStatisticsAdapter.submitList(
                state.prayerBreakdown
        );
    }

    private void displayHabitSummary(
            ReportsUiState state
    ) {
        ReportsUiState.HabitSummary habit =
                state.habitSummary;

        boolean hasHabitData =
                habit.trackedRecords > 0;

        binding.habitSection.setVisibility(
                hasHabitData
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.noHabitDataText.setVisibility(
                state.hasData && !hasHabitData
                        ? View.VISIBLE
                        : View.GONE
        );

        int progress =
                percentageToProgress(
                        habit.completionPercentage
                );

        binding.habitRateText.setText(
                getString(
                        R.string
                                .insights_habit_rate_format,
                        habit.completionPercentage
                )
        );

        binding.habitRateProgress
                .setProgressCompat(
                        progress,
                        true
                );

        binding.habitRateProgress
                .setContentDescription(
                        getString(
                                R.string
                                        .insights_habit_rate_content_description,
                                habit.completionPercentage
                        )
                );

        binding.habitTrackedRecordsText.setText(
                getString(
                        R.string
                                .insights_tracked_habit_records_format,
                        habit.trackedRecords
                )
        );

        binding.habitCountsText.setText(
                getString(
                        R.string
                                .insights_habit_counts_format,
                        habit.completed,
                        habit.notCompleted,
                        habit.pending
                )
        );

        habitStatisticsAdapter.submitList(
                state.habitBreakdown
        );
    }

    private void displayDailyBreakdown(
            ReportsUiState state
    ) {
        boolean hasDailyData =
                !state.dailyBreakdown.isEmpty();

        binding.dailySection.setVisibility(
                hasDailyData
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.noDailyDataText.setVisibility(
                state.hasData && !hasDailyData
                        ? View.VISIBLE
                        : View.GONE
        );

        dailyStatisticsAdapter.submitList(
                state.dailyBreakdown
        );
    }

    private int percentageToProgress(
            double percentage
    ) {
        int rounded =
                (int) Math.round(percentage);

        return Math.max(
                0,
                Math.min(
                        100,
                        rounded
                )
        );
    }

    @Override
    public void onDestroyView() {
        binding.prayerBreakdownList
                .setAdapter(null);

        binding.habitBreakdownList
                .setAdapter(null);

        binding.dailyBreakdownList
                .setAdapter(null);

        prayerStatisticsAdapter = null;
        habitStatisticsAdapter = null;
        dailyStatisticsAdapter = null;
        binding = null;

        super.onDestroyView();
    }
}