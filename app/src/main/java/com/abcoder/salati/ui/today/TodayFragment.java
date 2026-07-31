package com.abcoder.salati.ui.today;

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
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.data.repository.PrayerRepository;
import com.abcoder.salati.databinding.BottomSheetPrayerStatusBinding;
import com.abcoder.salati.databinding.FragmentTodayBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import com.abcoder.salati.MainActivity;
import com.abcoder.salati.data.entity.PrayerRecord;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.databinding.BottomSheetHabitStatusBinding;

import com.abcoder.salati.util.DayRolloverScheduler;

public class TodayFragment extends Fragment {

    private FragmentTodayBinding binding;

    private TodayViewModel todayViewModel;

    private PrayerListAdapter prayerListAdapter;
    private TodayHabitAdapter habitAdapter;
    private WeekDayAdapter weekDayAdapter;

    private DayRolloverScheduler
            dayRolloverScheduler;

    private BottomSheetDialog activeStatusDialog;

    private int recordedPrayerCount;
    private int totalHabitCount;
    private int completedHabitCount;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding =
                FragmentTodayBinding.inflate(
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

        dayRolloverScheduler =
                new DayRolloverScheduler(
                        this::refreshForCurrentDate
                );
        configureWeekDayList();
        configurePrayerList();
        configureHabitList();
        observeCurrentDate();
        observePrayerRecords();
        observeHabitRecords();
    }

    private void configureViewModel() {
        SalatiApplication application =
                (SalatiApplication)
                        requireActivity()
                                .getApplication();

        TodayViewModelFactory factory =
                new TodayViewModelFactory(
                        application
                                .getPrayerRepository(),
                        application
                                .getHabitRepository()
                );

        todayViewModel =
                new ViewModelProvider(
                        this,
                        factory
                ).get(TodayViewModel.class);
    }

    private void configurePrayerList() {
        prayerListAdapter =
                new PrayerListAdapter(
                        this::showPrayerStatusSheet
                );

        binding.prayerList.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
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
                        this::showHabitStatusSheet
                );

        binding.habitList.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        binding.habitList.setAdapter(
                habitAdapter
        );

        binding.habitList.setNestedScrollingEnabled(
                false
        );
    }

    private void observeCurrentDate() {
        todayViewModel.getCurrentDate()
                .observe(
                        getViewLifecycleOwner(),
                        date -> {
                            binding.todayDateText.setText(
                                    todayViewModel
                                            .getDisplayDate()
                            );

                            weekDayAdapter.submitList(
                                    createCurrentWeek(
                                            date
                                    )
                            );
                        }
                );
    }

    private void observePrayerRecords() {
        todayViewModel
                .getTodayPrayerRecords()
                .observe(
                        getViewLifecycleOwner(),
                        records -> {
                            prayerListAdapter.submitList(
                                    records
                            );

                            updatePrayerSummary(
                                    records
                            );
                        }
                );
    }

    private void observeHabitRecords() {
        todayViewModel
                .getTodayHabitItems()
                .observe(
                        getViewLifecycleOwner(),
                        items -> {
                            habitAdapter.submitList(items);
                            updateHabitSummary(items);

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

    private void showPrayerStatusSheet(
            PrayerType prayerType,
            PrayerStatus currentStatus
    ) {
        BottomSheetPrayerStatusBinding
                sheetBinding =
                BottomSheetPrayerStatusBinding
                        .inflate(
                                getLayoutInflater()
                        );

        BottomSheetDialog dialog =
                new BottomSheetDialog(
                        requireContext()
                );
        activeStatusDialog = dialog;

        dialog.setOnDismissListener(
                ignored -> {
                    if (activeStatusDialog == dialog) {
                        activeStatusDialog = null;
                    }
                }
        );

        dialog.setContentView(
                sheetBinding.getRoot()
        );

        String prayerName =
                getPrayerName(prayerType);

        boolean recorded =
                currentStatus
                        != PrayerStatus.UNRECORDED;

        String title =
                getString(
                        recorded
                                ? R.string
                                  .edit_prayer_title_format
                                : R.string
                                  .log_prayer_title_format,
                        prayerName
                );

        dialog.setTitle(title);

        sheetBinding.sheetTitleText.setText(
                title
        );

        sheetBinding.currentStatusText.setText(
                getString(
                        R.string
                                .current_prayer_status_format,
                        getStatusName(currentStatus)
                )
        );

        sheetBinding.onTimeButton.setEnabled(
                currentStatus
                        != PrayerStatus.ON_TIME
        );

        sheetBinding.lateButton.setEnabled(
                currentStatus
                        != PrayerStatus.LATE
        );

        sheetBinding.missedButton.setEnabled(
                currentStatus
                        != PrayerStatus.MISSED
        );

        sheetBinding.clearRecordButton
                .setVisibility(
                        recorded
                                ? View.VISIBLE
                                : View.GONE
                );

        sheetBinding.onTimeButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                prayerType,
                                currentStatus,
                                PrayerStatus.ON_TIME
                        )
                );

        sheetBinding.lateButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                prayerType,
                                currentStatus,
                                PrayerStatus.LATE
                        )
                );

        sheetBinding.missedButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                prayerType,
                                currentStatus,
                                PrayerStatus.MISSED
                        )
                );

        sheetBinding.clearRecordButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                prayerType,
                                currentStatus,
                                PrayerStatus.UNRECORDED
                        )
                );

        sheetBinding.cancelButton
                .setOnClickListener(
                        view -> dialog.dismiss()
                );

        dialog.show();
    }

    private void savePrayerStatus(
            BottomSheetDialog dialog,
            PrayerType prayerType,
            PrayerStatus previousStatus,
            PrayerStatus newStatus
    ) {
        dialog.dismiss();

        todayViewModel.setPrayerStatus(
                prayerType,
                newStatus,
                new PrayerRepository
                        .OperationCallback() {

                    @Override
                    public void onSuccess() {
                        showPrayerSavedMessage(
                                prayerType,
                                previousStatus,
                                newStatus
                        );
                    }

                    @Override
                    public void onError(
                            Exception exception
                    ) {
                        showSnackbar(
                                getString(
                                        R.string
                                                .prayer_save_failed_message,
                                        getPrayerName(
                                                prayerType
                                        )
                                )
                        );
                    }
                }
        );
    }

    private void showPrayerSavedMessage(
            PrayerType prayerType,
            PrayerStatus previousStatus,
            PrayerStatus newStatus
    ) {
        if (binding == null || !isAdded()) {
            return;
        }

        String message;

        if (newStatus
                == PrayerStatus.UNRECORDED) {

            message =
                    getString(
                            R.string
                                    .prayer_record_cleared_message,
                            getPrayerName(prayerType)
                    );

        } else {
            message =
                    getString(
                            R.string
                                    .prayer_saved_message,
                            getPrayerName(prayerType),
                            getStatusName(newStatus)
                    );
        }

        Snackbar snackbar =
                Snackbar.make(
                        binding.todayRoot,
                        message,
                        Snackbar.LENGTH_LONG
                );

        snackbar.setAction(
                R.string.action_undo,
                view -> undoPrayerStatus(
                        prayerType,
                        previousStatus
                )
        );

        snackbar.show();
    }

    private void undoPrayerStatus(
            PrayerType prayerType,
            PrayerStatus previousStatus
    ) {
        todayViewModel.setPrayerStatus(
                prayerType,
                previousStatus,
                new PrayerRepository
                        .OperationCallback() {

                    @Override
                    public void onSuccess() {
                        showSnackbar(
                                getString(
                                        R.string
                                                .prayer_change_undone_message,
                                        getPrayerName(
                                                prayerType
                                        )
                                )
                        );
                    }

                    @Override
                    public void onError(
                            Exception exception
                    ) {
                        showSnackbar(
                                getString(
                                        R.string
                                                .prayer_undo_failed_message,
                                        getPrayerName(
                                                prayerType
                                        )
                                )
                        );
                    }
                }
        );
    }

    private void showSnackbar(
            String message
    ) {
        if (binding == null || !isAdded()) {
            return;
        }

        Snackbar.make(
                binding.todayRoot,
                message,
                Snackbar.LENGTH_LONG
        ).show();
    }

    private String getPrayerName(
            PrayerType prayerType
    ) {
        switch (prayerType) {
            case FAJR:
                return getString(
                        R.string.prayer_fajr
                );

            case DHUHR:
                return getString(
                        R.string.prayer_dhuhr
                );

            case ASR:
                return getString(
                        R.string.prayer_asr
                );

            case MAGHRIB:
                return getString(
                        R.string.prayer_maghrib
                );

            case ISHA:
                return getString(
                        R.string.prayer_isha
                );

            default:
                throw new IllegalArgumentException(
                        "Unknown prayer type: "
                                + prayerType
                );
        }
    }

    private String getStatusName(
            PrayerStatus status
    ) {
        switch (status) {
            case ON_TIME:
                return getString(
                        R.string.status_on_time
                );

            case LATE:
                return getString(
                        R.string.status_late
                );

            case MISSED:
                return getString(
                        R.string.status_missed
                );

            case UNRECORDED:
            default:
                return getString(
                        R.string.status_unrecorded
                );
        }
    }

    @Override
    public void onDestroyView() {
        if (dayRolloverScheduler != null) {
            dayRolloverScheduler.stop();
        }

        if (activeStatusDialog != null) {
            activeStatusDialog.dismiss();
        }
        binding.weekDayList.setAdapter(null);
        binding.prayerList.setAdapter(null);
        binding.habitList.setAdapter(null);

        weekDayAdapter = null;
        prayerListAdapter = null;
        habitAdapter = null;
        dayRolloverScheduler = null;
        activeStatusDialog = null;
        binding = null;

        super.onDestroyView();
    }

    private void configureWeekDayList() {
        weekDayAdapter =
                new WeekDayAdapter(
                        this::openCalendarForDate
                );

        binding.weekDayList.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        binding.weekDayList.setAdapter(
                weekDayAdapter
        );



    }
    private List<WeekDayItem> createCurrentWeek(LocalDate today) {


        LocalDate monday =
                today.with(
                        TemporalAdjusters
                                .previousOrSame(
                                        DayOfWeek.MONDAY
                                )
                );

        List<WeekDayItem> weekDays =
                new ArrayList<>();

        for (int index = 0;
             index < 7;
             index++) {

            LocalDate date =
                    monday.plusDays(index);

            weekDays.add(
                    new WeekDayItem(
                            date,
                            date.equals(today)
                    )
            );
        }

        return weekDays;
    }
    private void openCalendarForDate(
            LocalDate date
    ) {
        MainActivity mainActivity =
                (MainActivity) requireActivity();

        mainActivity.openCalendarForDate(
                date
        );
    }
    private void updatePrayerSummary(
            List<PrayerRecord> records
    ) {
        int onTime = 0;
        int late = 0;
        int missed = 0;

        if (records != null) {
            for (PrayerRecord record : records) {
                switch (record.status) {
                    case ON_TIME:
                        onTime++;
                        break;

                    case LATE:
                        late++;
                        break;

                    case MISSED:
                        missed++;
                        break;

                    case UNRECORDED:
                    default:
                        break;
                }
            }
        }

        int recorded =
                onTime + late + missed;
        recordedPrayerCount = recorded;

        binding.prayerSummaryText.setText(
                getString(
                        R.string
                                .today_prayer_recorded_format,
                        recorded,
                        5
                )
        );

        binding.prayerBreakdownText.setText(
                getString(
                        R.string
                                .today_prayer_breakdown_format,
                        onTime,
                        late,
                        missed
                )
        );

        binding.prayerProgressIndicator
                .setProgressCompat(
                        recorded,
                        true
                );

        binding.prayerProgressIndicator
                .setContentDescription(
                        getString(
                                R.string
                                        .today_prayer_progress_content_description,
                                recorded,
                                5
                        )
                );

        updateDailyOverview();
    }
    private void updateHabitSummary(
            List<HabitTodayItem> items
    ) {
        int completed = 0;
        int notCompleted = 0;
        int pending = 0;

        if (items != null) {
            for (HabitTodayItem item : items) {
                switch (item.status) {
                    case COMPLETED:
                        completed++;
                        break;

                    case NOT_COMPLETED:
                        notCompleted++;
                        break;

                    case PENDING:
                    default:
                        pending++;
                        break;
                }
            }
        }

        int total =
                completed
                        + notCompleted
                        + pending;

        totalHabitCount = total;
        completedHabitCount = completed;

        binding.habitSummaryCard.setVisibility(
                total > 0
                        ? View.VISIBLE
                        : View.GONE
        );

        if (total > 0) {
            binding.habitSummaryText.setText(
                    getString(
                            R.string
                                    .today_habit_completed_format,
                            completed,
                            total
                    )
            );

            binding.habitBreakdownText.setText(
                    getString(
                            R.string
                                    .today_habit_breakdown_format,
                            completed,
                            notCompleted,
                            pending
                    )
            );

            binding.habitProgressIndicator.setMax(
                    total
            );

            binding.habitProgressIndicator
                    .setProgressCompat(
                            completed,
                            true
                    );

            binding.habitProgressIndicator
                    .setContentDescription(
                            getString(
                                    R.string
                                            .today_habit_progress_content_description,
                                    completed,
                                    total
                            )
                    );
        }

        updateDailyOverview();
    }
    private void updateDailyOverview() {
        if (binding == null) {
            return;
        }

        if (totalHabitCount == 0) {
            binding.dailyOverviewText.setText(
                    getString(
                            R.string
                                    .today_overview_no_habits_format,
                            recordedPrayerCount,
                            5
                    )
            );

            return;
        }

        binding.dailyOverviewText.setText(
                getString(
                        R.string
                                .today_overview_format,
                        recordedPrayerCount,
                        5,
                        completedHabitCount,
                        totalHabitCount
                )
        );
    }
    private void showHabitStatusSheet(
            long habitId,
            String habitTitle,
            HabitStatus currentStatus
    ) {
        BottomSheetHabitStatusBinding
                sheetBinding =
                BottomSheetHabitStatusBinding
                        .inflate(
                                getLayoutInflater()
                        );

        BottomSheetDialog dialog =
                new BottomSheetDialog(
                        requireContext()
                );
        activeStatusDialog = dialog;

        dialog.setOnDismissListener(
                ignored -> {
                    if (activeStatusDialog == dialog) {
                        activeStatusDialog = null;
                    }
                }
        );

        dialog.setContentView(
                sheetBinding.getRoot()
        );

        boolean recorded =
                currentStatus
                        != HabitStatus.PENDING;

        String title =
                getString(
                        recorded
                                ? R.string
                                  .edit_habit_title_format
                                : R.string
                                  .log_habit_title_format,
                        habitTitle
                );

        dialog.setTitle(title);

        sheetBinding.sheetTitleText.setText(
                title
        );

        sheetBinding.currentStatusText.setText(
                getString(
                        R.string
                                .current_habit_status_format,
                        getHabitStatusName(
                                currentStatus
                        )
                )
        );

        sheetBinding.completedButton.setEnabled(
                currentStatus
                        != HabitStatus.COMPLETED
        );

        sheetBinding.notCompletedButton.setEnabled(
                currentStatus
                        != HabitStatus.NOT_COMPLETED
        );

        sheetBinding.clearRecordButton
                .setVisibility(
                        recorded
                                ? View.VISIBLE
                                : View.GONE
                );

        sheetBinding.completedButton
                .setOnClickListener(
                        view -> saveHabitStatus(
                                dialog,
                                habitId,
                                habitTitle,
                                currentStatus,
                                HabitStatus.COMPLETED
                        )
                );

        sheetBinding.notCompletedButton
                .setOnClickListener(
                        view -> saveHabitStatus(
                                dialog,
                                habitId,
                                habitTitle,
                                currentStatus,
                                HabitStatus.NOT_COMPLETED
                        )
                );

        sheetBinding.clearRecordButton
                .setOnClickListener(
                        view -> saveHabitStatus(
                                dialog,
                                habitId,
                                habitTitle,
                                currentStatus,
                                HabitStatus.PENDING
                        )
                );

        sheetBinding.cancelButton
                .setOnClickListener(
                        view -> dialog.dismiss()
                );

        dialog.show();
    }


    private void saveHabitStatus(
            BottomSheetDialog dialog,
            long habitId,
            String habitTitle,
            HabitStatus previousStatus,
            HabitStatus newStatus
    ) {
        dialog.dismiss();

        todayViewModel.setHabitStatus(
                habitId,
                newStatus,
                new HabitRepository
                        .StatusOperationCallback() {

                    @Override
                    public void onSuccess() {
                        showHabitSavedMessage(
                                habitId,
                                habitTitle,
                                previousStatus,
                                newStatus
                        );
                    }

                    @Override
                    public void onError(
                            Exception exception
                    ) {
                        showSnackbar(
                                getString(
                                        R.string
                                                .habit_status_save_failed,
                                        habitTitle
                                )
                        );
                    }
                }
        );
    }
    private void undoHabitStatus(
            long habitId,
            String habitTitle,
            HabitStatus previousStatus
    ) {
        todayViewModel.setHabitStatus(
                habitId,
                previousStatus,
                new HabitRepository
                        .StatusOperationCallback() {

                    @Override
                    public void onSuccess() {
                        showSnackbar(
                                getString(
                                        R.string
                                                .habit_change_undone_message,
                                        habitTitle
                                )
                        );
                    }

                    @Override
                    public void onError(
                            Exception exception
                    ) {
                        showSnackbar(
                                getString(
                                        R.string
                                                .habit_undo_failed_message,
                                        habitTitle
                                )
                        );
                    }
                }
        );
    }
    private String getHabitStatusName(
            HabitStatus status
    ) {
        switch (status) {
            case COMPLETED:
                return getString(
                        R.string
                                .habit_status_completed
                );

            case NOT_COMPLETED:
                return getString(
                        R.string
                                .habit_status_not_completed
                );

            case PENDING:
            default:
                return getString(
                        R.string
                                .habit_status_pending
                );
        }
    }
    private void showHabitSavedMessage(
            long habitId,
            String habitTitle,
            HabitStatus previousStatus,
            HabitStatus newStatus
    ) {
        if (binding == null || !isAdded()) {
            return;
        }

        String message;

        if (newStatus == HabitStatus.PENDING) {
            message =
                    getString(
                            R.string
                                    .habit_record_cleared_message,
                            habitTitle
                    );

        } else {
            message =
                    getString(
                            R.string
                                    .habit_status_saved_message,
                            habitTitle,
                            getHabitStatusName(
                                    newStatus
                            )
                    );
        }

        Snackbar snackbar =
                Snackbar.make(
                        binding.todayRoot,
                        message,
                        Snackbar.LENGTH_LONG
                );

        snackbar.setAction(
                R.string.action_undo,
                view -> undoHabitStatus(
                        habitId,
                        habitTitle,
                        previousStatus
                )
        );

        snackbar.show();
    }
    private void refreshForCurrentDate() {
        if (todayViewModel == null) {
            return;
        }

        boolean dateChanged =
                todayViewModel
                        .refreshDateIfNeeded();

        /*
         * Prevent a sheet opened before midnight from saving
         * yesterday's visible choice into the new date.
         */
        if (dateChanged
                && activeStatusDialog != null) {

            activeStatusDialog.dismiss();
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        updateDayRolloverMonitoring();
    }

    @Override
    public void onPause() {
        if (dayRolloverScheduler != null) {
            dayRolloverScheduler.stop();
        }

        super.onPause();
    }

    @Override
    public void onHiddenChanged(
            boolean hidden
    ) {
        super.onHiddenChanged(hidden);

        updateDayRolloverMonitoring();
    }

    private void updateDayRolloverMonitoring() {
        if (dayRolloverScheduler == null
                || todayViewModel == null) {

            return;
        }

        if (isResumed() && !isHidden()) {
            refreshForCurrentDate();
            dayRolloverScheduler.start();

        } else {
            dayRolloverScheduler.stop();
        }
    }
}