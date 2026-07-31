package com.abcoder.salati.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.abcoder.salati.R;
import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.entity.HabitRecord;
import com.abcoder.salati.data.entity.PrayerRecord;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.databinding.FragmentCalendarBinding;

import androidx.recyclerview.widget.LinearLayoutManager;
import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.data.repository.PrayerRepository;
import com.abcoder.salati.databinding.BottomSheetHabitStatusBinding;
import com.abcoder.salati.databinding.BottomSheetPrayerStatusBinding;
import com.abcoder.salati.ui.today.HabitTodayItem;
import com.abcoder.salati.ui.today.PrayerListAdapter;
import com.abcoder.salati.ui.today.TodayHabitAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendarFragment
        extends Fragment {

    public static final String
            REQUEST_KEY_SELECTED_DATE =
            "calendar_selected_date_request";

    public static final String
            BUNDLE_KEY_SELECTED_DATE =
            "calendar_selected_date";

    private FragmentCalendarBinding binding;

    private CalendarViewModel viewModel;
    private CalendarDayAdapter calendarAdapter;
    private PrayerListAdapter selectedPrayerAdapter;
    private TodayHabitAdapter selectedHabitAdapter;

    private YearMonth displayedMonth =
            YearMonth.now();

    private LocalDate selectedDate =
            LocalDate.now();

    private List<PrayerRecord>
            monthPrayerRecords =
            Collections.emptyList();

    private List<HabitRecord>
            monthHabitRecords =
            Collections.emptyList();

    private List<PrayerRecord>
            selectedPrayerRecords =
            Collections.emptyList();

    private List<HabitRecord>
            selectedHabitRecords =
            Collections.emptyList();

    private List<Habit> allHabits =
            Collections.emptyList();

    private int selectedPrayerRecordedCount;
    private int selectedHabitCompletedCount;
    private int selectedHabitTotalCount;

    @Override
    public void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        configureViewModel();

        getParentFragmentManager()
                .setFragmentResultListener(
                        REQUEST_KEY_SELECTED_DATE,
                        this,
                        (requestKey, result) -> {
                            String dateValue =
                                    result.getString(
                                            BUNDLE_KEY_SELECTED_DATE
                                    );

                            if (dateValue == null) {
                                return;
                            }

                            try {
                                viewModel.selectDate(
                                        LocalDate.parse(
                                                dateValue
                                        )
                                );

                            } catch (
                                    DateTimeParseException
                                            exception
                            ) {
                                viewModel.showToday();
                            }
                        }
                );
    }

    private void configureViewModel() {
        SalatiApplication application =
                (SalatiApplication)
                        requireActivity()
                                .getApplication();

        CalendarViewModelFactory factory =
                new CalendarViewModelFactory(
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
                        CalendarViewModel.class
                );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding =
                FragmentCalendarBinding.inflate(
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

        configureCalendarGrid();
        configureSelectedDayLists();
        configureButtons();
        observeCalendarState();
    }

    private void configureCalendarGrid() {
        calendarAdapter =
                new CalendarDayAdapter(
                        viewModel::selectDate
                );

        binding.calendarGrid.setLayoutManager(
                new GridLayoutManager(
                        requireContext(),
                        7
                )
        );

        binding.calendarGrid.setAdapter(
                calendarAdapter
        );

        binding.calendarGrid
                .setNestedScrollingEnabled(
                        false
                );
    }

    private void configureButtons() {
        binding.previousMonthButton
                .setOnClickListener(
                        view ->
                                viewModel
                                        .showPreviousMonth()
                );

        binding.nextMonthButton
                .setOnClickListener(
                        view ->
                                viewModel
                                        .showNextMonth()
                );

        binding.todayButton
                .setOnClickListener(
                        view ->
                                viewModel.showToday()
                );
    }

    private void observeCalendarState() {
        viewModel.getDisplayedMonth()
                .observe(
                        getViewLifecycleOwner(),
                        month -> {
                            displayedMonth = month;

                            updateMonthHeader();
                            rebuildCalendarGrid();
                        }
                );

        viewModel.getSelectedDate()
                .observe(
                        getViewLifecycleOwner(),
                        date -> {
                            selectedDate = date;

                            updateSelectedDateHeader();
                            rebuildCalendarGrid();
                        }
                );

        viewModel.getMonthPrayerRecords()
                .observe(
                        getViewLifecycleOwner(),
                        records -> {
                            monthPrayerRecords =
                                    records == null
                                            ? Collections
                                            .emptyList()
                                            : records;

                            rebuildCalendarGrid();
                        }
                );

        viewModel.getMonthHabitRecords()
                .observe(
                        getViewLifecycleOwner(),
                        records -> {
                            monthHabitRecords =
                                    records == null
                                            ? Collections
                                            .emptyList()
                                            : records;

                            rebuildCalendarGrid();
                        }
                );

        viewModel.getSelectedPrayerRecords()
                .observe(
                        getViewLifecycleOwner(),
                        records -> {
                            selectedPrayerRecords =
                                    records == null
                                            ? Collections.emptyList()
                                            : records;

                            selectedPrayerAdapter.submitList(
                                    selectedPrayerRecords
                            );

                            updatePrayerDetails();
                        }
                );

        viewModel.getSelectedHabitRecords()
                .observe(
                        getViewLifecycleOwner(),
                        records -> {
                            selectedHabitRecords =
                                    records == null
                                            ? Collections
                                            .emptyList()
                                            : records;

                            updateHabitDetails();
                        }
                );

        viewModel.getAllHabits()
                .observe(
                        getViewLifecycleOwner(),
                        habits -> {
                            allHabits =
                                    habits == null
                                            ? Collections
                                            .emptyList()
                                            : habits;

                            updateHabitDetails();
                        }
                );
    }

    private void updateMonthHeader() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "MMMM yyyy",
                        Locale.getDefault()
                );

        binding.monthTitleText.setText(
                displayedMonth.format(
                        formatter
                )
        );

        binding.nextMonthButton.setEnabled(
                viewModel.canShowNextMonth()
        );

        binding.nextMonthButton.setAlpha(
                viewModel.canShowNextMonth()
                        ? 1f
                        : 0.38f
        );
    }

    private void updateSelectedDateHeader() {
        binding.selectedDateText.setText(
                selectedDate.format(
                        DateTimeFormatter.ofPattern(
                                "EEEE, d MMMM yyyy",
                                Locale.getDefault()
                        )
                )
        );
    }

    private void rebuildCalendarGrid() {
        if (binding == null
                || calendarAdapter == null
                || displayedMonth == null
                || selectedDate == null) {

            return;
        }

        Map<LocalDate, Integer>
                prayerRecordedCounts =
                new HashMap<>();

        Set<LocalDate> prayerDates =
                new HashSet<>();

        for (PrayerRecord record :
                monthPrayerRecords) {

            try {
                LocalDate date =
                        LocalDate.parse(
                                record.recordDate
                        );

                prayerDates.add(date);

                if (record.status
                        != PrayerStatus.UNRECORDED) {

                    prayerRecordedCounts.merge(
                            date,
                            1,
                            Integer::sum
                    );
                }

            } catch (
                    DateTimeParseException ignored
            ) {
                // Ignore malformed stored dates.
            }
        }

        Map<LocalDate, Integer>
                habitRecordCounts =
                new HashMap<>();

        Map<LocalDate, Integer>
                habitCompletedCounts =
                new HashMap<>();

        for (HabitRecord record :
                monthHabitRecords) {

            try {
                LocalDate date =
                        LocalDate.parse(
                                record.recordDate
                        );

                habitRecordCounts.merge(
                        date,
                        1,
                        Integer::sum
                );

                if (record.status
                        == HabitStatus.COMPLETED) {

                    habitCompletedCounts.merge(
                            date,
                            1,
                            Integer::sum
                    );
                }

            } catch (
                    DateTimeParseException ignored
            ) {
                // Ignore malformed stored dates.
            }
        }

        LocalDate firstOfMonth =
                displayedMonth.atDay(1);

        LocalDate gridStart =
                firstOfMonth.with(
                        TemporalAdjusters
                                .previousOrSame(
                                        DayOfWeek.MONDAY
                                )
                );

        LocalDate today =
                LocalDate.now();

        List<CalendarDayItem> cells =
                new ArrayList<>();

        for (int index = 0;
             index < 42;
             index++) {

            LocalDate date =
                    gridStart.plusDays(index);

            boolean inDisplayedMonth =
                    YearMonth.from(date)
                            .equals(
                                    displayedMonth
                            );

            cells.add(
                    new CalendarDayItem(
                            date,
                            inDisplayedMonth,
                            date.equals(
                                    selectedDate
                            ),
                            date.equals(today),
                            date.isAfter(today),
                            prayerDates.contains(date),
                            prayerRecordedCounts
                                    .getOrDefault(
                                            date,
                                            0
                                    ),
                            habitCompletedCounts
                                    .getOrDefault(
                                            date,
                                            0
                                    ),
                            habitRecordCounts
                                    .getOrDefault(
                                            date,
                                            0
                                    )
                    )
            );
        }

        calendarAdapter.submitList(cells);
    }

    private void updatePrayerDetails() {
        int recordedCount = 0;

        for (PrayerRecord record :
                selectedPrayerRecords) {

            if (record.status
                    != PrayerStatus.UNRECORDED) {

                recordedCount++;
            }
        }

        selectedPrayerRecordedCount =
                recordedCount;

        boolean empty =
                selectedPrayerRecords.isEmpty();

        binding.noPrayerRecordsText.setVisibility(
                empty
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.selectedPrayerList.setVisibility(
                empty
                        ? View.GONE
                        : View.VISIBLE
        );

        updateSelectedDaySummary();
    }

    private void updateHabitDetails() {
        Map<Long, HabitRecord> recordsByHabitId =
                new HashMap<>();

        for (HabitRecord record :
                selectedHabitRecords) {

            recordsByHabitId.put(
                    record.habitId,
                    record
            );
        }

        List<HabitTodayItem> historyItems =
                new ArrayList<>();

        int completed = 0;

        /*
         * Iterate through allHabits so the display order stays
         * consistent with habit creation order.
         */
        for (Habit habit : allHabits) {
            HabitRecord record =
                    recordsByHabitId.get(
                            habit.id
                    );

            if (record == null) {
                continue;
            }

            historyItems.add(
                    new HabitTodayItem(
                            habit,
                            record.status,
                            record.snoozeCount
                    )
            );

            if (record.status
                    == HabitStatus.COMPLETED) {

                completed++;
            }
        }

        selectedHabitCompletedCount =
                completed;

        selectedHabitTotalCount =
                historyItems.size();

        selectedHabitAdapter.submitList(
                historyItems
        );

        boolean empty =
                historyItems.isEmpty();

        binding.noHabitRecordsText.setVisibility(
                empty
                        ? View.VISIBLE
                        : View.GONE
        );

        binding.selectedHabitList.setVisibility(
                empty
                        ? View.GONE
                        : View.VISIBLE
        );

        updateSelectedDaySummary();
    }

    private void updateSelectedDaySummary() {
        if (binding == null) {
            return;
        }

        if (selectedHabitTotalCount == 0) {
            binding.selectedDaySummaryText
                    .setText(
                            getString(
                                    R.string
                                            .calendar_day_summary_no_habits,
                                    selectedPrayerRecordedCount,
                                    5
                            )
                    );

            return;
        }

        binding.selectedDaySummaryText.setText(
                getString(
                        R.string
                                .calendar_day_summary_format,
                        selectedPrayerRecordedCount,
                        5,
                        selectedHabitCompletedCount,
                        selectedHabitTotalCount
                )
        );
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

    private String getPrayerStatusName(
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

    @Override
    public void onDestroyView() {
        binding.calendarGrid.setAdapter(null);
        binding.selectedPrayerList.setAdapter(null);
        binding.selectedHabitList.setAdapter(null);

        calendarAdapter = null;
        selectedPrayerAdapter = null;
        selectedHabitAdapter = null;
        binding = null;

        super.onDestroyView();
    }
    private void configureSelectedDayLists() {
        selectedPrayerAdapter =
                new PrayerListAdapter(
                        this::showPrayerStatusSheet
                );

        binding.selectedPrayerList.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        binding.selectedPrayerList.setAdapter(
                selectedPrayerAdapter
        );

        binding.selectedPrayerList
                .setNestedScrollingEnabled(false);

        selectedHabitAdapter =
                new TodayHabitAdapter(
                        this::showHabitStatusSheet
                );

        binding.selectedHabitList.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        binding.selectedHabitList.setAdapter(
                selectedHabitAdapter
        );

        binding.selectedHabitList
                .setNestedScrollingEnabled(false);
    }
    private void showPrayerStatusSheet(
            PrayerType prayerType,
            PrayerStatus currentStatus
    ) {
        LocalDate actionDate =
                selectedDate;

        if (actionDate == null
                || actionDate.isAfter(
                LocalDate.now()
        )) {
            return;
        }

        BottomSheetPrayerStatusBinding
                sheetBinding =
                BottomSheetPrayerStatusBinding.inflate(
                        getLayoutInflater()
                );

        BottomSheetDialog dialog =
                new BottomSheetDialog(
                        requireContext()
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
                        getPrayerStatusName(
                                currentStatus
                        )
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
                                actionDate,
                                prayerType,
                                currentStatus,
                                PrayerStatus.ON_TIME
                        )
                );

        sheetBinding.lateButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                actionDate,
                                prayerType,
                                currentStatus,
                                PrayerStatus.LATE
                        )
                );

        sheetBinding.missedButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                actionDate,
                                prayerType,
                                currentStatus,
                                PrayerStatus.MISSED
                        )
                );

        sheetBinding.clearRecordButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                actionDate,
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
            LocalDate actionDate,
            PrayerType prayerType,
            PrayerStatus previousStatus,
            PrayerStatus newStatus
    ) {
        dialog.dismiss();

        viewModel.setPrayerStatus(
                actionDate,
                prayerType,
                newStatus,
                new PrayerRepository
                        .OperationCallback() {

                    @Override
                    public void onSuccess() {
                        showPrayerSavedMessage(
                                actionDate,
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
            LocalDate actionDate,
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
                            getPrayerStatusName(
                                    newStatus
                            )
                    );
        }

        Snackbar snackbar =
                Snackbar.make(
                        binding.calendarRoot,
                        message,
                        Snackbar.LENGTH_LONG
                );

        snackbar.setAction(
                R.string.action_undo,
                view -> undoPrayerStatus(
                        actionDate,
                        prayerType,
                        previousStatus
                )
        );

        snackbar.show();
    }

    private void undoPrayerStatus(
            LocalDate actionDate,
            PrayerType prayerType,
            PrayerStatus previousStatus
    ) {
        viewModel.setPrayerStatus(
                actionDate,
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
    private void showHabitStatusSheet(
            long habitId,
            String habitTitle,
            HabitStatus currentStatus
    ) {
        LocalDate actionDate =
                selectedDate;

        if (actionDate == null
                || actionDate.isAfter(
                LocalDate.now()
        )) {
            return;
        }

        BottomSheetHabitStatusBinding
                sheetBinding =
                BottomSheetHabitStatusBinding.inflate(
                        getLayoutInflater()
                );

        BottomSheetDialog dialog =
                new BottomSheetDialog(
                        requireContext()
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
                                actionDate,
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
                                actionDate,
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
                                actionDate,
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
            LocalDate actionDate,
            long habitId,
            String habitTitle,
            HabitStatus previousStatus,
            HabitStatus newStatus
    ) {
        dialog.dismiss();

        viewModel.setHabitStatus(
                actionDate,
                habitId,
                newStatus,
                new HabitRepository
                        .StatusOperationCallback() {

                    @Override
                    public void onSuccess() {
                        showHabitSavedMessage(
                                actionDate,
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
    private void showHabitSavedMessage(
            LocalDate actionDate,
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
                        binding.calendarRoot,
                        message,
                        Snackbar.LENGTH_LONG
                );

        snackbar.setAction(
                R.string.action_undo,
                view -> undoHabitStatus(
                        actionDate,
                        habitId,
                        habitTitle,
                        previousStatus
                )
        );

        snackbar.show();
    }

    private void undoHabitStatus(
            LocalDate actionDate,
            long habitId,
            String habitTitle,
            HabitStatus previousStatus
    ) {
        viewModel.setHabitStatus(
                actionDate,
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
    private void showSnackbar(
            String message
    ) {
        if (binding == null || !isAdded()) {
            return;
        }

        Snackbar.make(
                binding.calendarRoot,
                message,
                Snackbar.LENGTH_LONG
        ).show();
    }
}