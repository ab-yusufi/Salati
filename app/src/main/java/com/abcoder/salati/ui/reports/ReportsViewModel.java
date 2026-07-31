package com.abcoder.salati.ui.reports;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.entity.HabitRecord;
import com.abcoder.salati.data.entity.PrayerRecord;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.data.repository.PrayerRepository;
import java.time.YearMonth;

public final class ReportsViewModel
        extends ViewModel {

    private final PrayerRepository prayerRepository;
    private final HabitRepository habitRepository;

    private final MutableLiveData<ReportRange>
            selectedRange =
            new MutableLiveData<>();

    private final LiveData<List<PrayerRecord>>
            prayerRecords;

    private final LiveData<List<HabitRecord>>
            habitRecords;

    private final LiveData<List<Habit>> habits;

    private final MediatorLiveData<ReportsUiState>
            uiState =
            new MediatorLiveData<>();

    private ReportsUiState.Period selectedPeriod =
            ReportsUiState.Period.WEEK;
    private LocalDate currentDate =
            LocalDate.now();
    private LocalDate anchorDate =
            currentDate;

    private List<PrayerRecord>
            latestPrayerRecords =
            Collections.emptyList();

    private List<HabitRecord>
            latestHabitRecords =
            Collections.emptyList();

    private List<Habit> latestHabits =
            Collections.emptyList();

    public ReportsViewModel(
            PrayerRepository prayerRepository,
            HabitRepository habitRepository
    ) {
        this.prayerRepository = prayerRepository;
        this.habitRepository = habitRepository;

        prayerRecords =
                Transformations.switchMap(
                        selectedRange,
                        range ->
                                prayerRepository
                                        .observeBetweenDates(
                                                range.startDate
                                                        .toString(),
                                                range.endDate
                                                        .toString()
                                        )
                );

        habitRecords =
                Transformations.switchMap(
                        selectedRange,
                        range ->
                                habitRepository
                                        .observeRecordsBetweenDates(
                                                range.startDate
                                                        .toString(),
                                                range.endDate
                                                        .toString()
                                        )
                );

        habits =
                habitRepository.observeAllHabits();

        uiState.addSource(
                prayerRecords,
                records -> {
                    latestPrayerRecords =
                            records == null
                                    ? Collections.emptyList()
                                    : records;

                    rebuildUiState();
                }
        );

        uiState.addSource(
                habitRecords,
                records -> {
                    latestHabitRecords =
                            records == null
                                    ? Collections.emptyList()
                                    : records;

                    rebuildUiState();
                }
        );

        uiState.addSource(
                habits,
                habitList -> {
                    latestHabits =
                            habitList == null
                                    ? Collections.emptyList()
                                    : habitList;

                    rebuildUiState();
                }
        );

        updateSelectedRange();
    }

    public LiveData<ReportsUiState> getUiState() {
        return uiState;
    }
    public boolean refreshForCurrentDate() {
        LocalDate newDate =
                LocalDate.now();

        if (newDate.equals(currentDate)) {
            return false;
        }

        boolean viewingCurrentPeriod =
                isSameReportingPeriod(
                        anchorDate,
                        currentDate,
                        selectedPeriod
                );

        currentDate = newDate;

        /*
         * Follow the newly current week or month only when the
         * user was viewing the previous current period.
         */
        if (viewingCurrentPeriod) {
            anchorDate = newDate;
        }

        updateSelectedRange();

        return true;
    }
    public void selectWeek() {
        if (selectedPeriod
                == ReportsUiState.Period.WEEK) {
            return;
        }

        selectedPeriod =
                ReportsUiState.Period.WEEK;

        anchorDate = currentDate;

        updateSelectedRange();
    }

    public void selectMonth() {
        if (selectedPeriod
                == ReportsUiState.Period.MONTH) {
            return;
        }

        selectedPeriod =
                ReportsUiState.Period.MONTH;

        anchorDate = currentDate;

        updateSelectedRange();
    }

    public void movePrevious() {
        if (selectedPeriod
                == ReportsUiState.Period.WEEK) {
            anchorDate = anchorDate.minusWeeks(1);
        } else {
            anchorDate = anchorDate.minusMonths(1);
        }

        updateSelectedRange();
    }

    public void moveNext() {
        if (!canMoveNext()) {
            return;
        }

        if (selectedPeriod
                == ReportsUiState.Period.WEEK) {
            anchorDate = anchorDate.plusWeeks(1);
        } else {
            anchorDate = anchorDate.plusMonths(1);
        }

        updateSelectedRange();
    }

    private boolean isSameReportingPeriod(
            LocalDate firstDate,
            LocalDate secondDate,
            ReportsUiState.Period period
    ) {
        if (period
                == ReportsUiState.Period.WEEK) {

            LocalDate firstWeekStart =
                    firstDate.with(
                            TemporalAdjusters
                                    .previousOrSame(
                                            DayOfWeek.MONDAY
                                    )
                    );

            LocalDate secondWeekStart =
                    secondDate.with(
                            TemporalAdjusters
                                    .previousOrSame(
                                            DayOfWeek.MONDAY
                                    )
                    );

            return firstWeekStart.equals(
                    secondWeekStart
            );
        }

        return YearMonth.from(firstDate)
                .equals(
                        YearMonth.from(secondDate)
                );
    }
    private void updateSelectedRange() {
        /*
         * Prevent old records from briefly appearing under the
         * newly selected date label.
         */
        latestPrayerRecords =
                Collections.emptyList();

        latestHabitRecords =
                Collections.emptyList();

        selectedRange.setValue(
                buildRange(
                        selectedPeriod,
                        anchorDate
                )
        );

        rebuildUiState();
    }

    private ReportRange buildRange(
            ReportsUiState.Period period,
            LocalDate anchor
    ) {
        LocalDate today = currentDate;

        LocalDate startDate;
        LocalDate nominalEndDate;
        String rangeLabel;

        if (period == ReportsUiState.Period.WEEK) {
            startDate =
                    anchor.with(
                            TemporalAdjusters
                                    .previousOrSame(
                                            DayOfWeek.MONDAY
                                    )
                    );

            nominalEndDate =
                    startDate.plusDays(6);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "d MMM yyyy",
                            Locale.getDefault()
                    );

            rangeLabel =
                    startDate.format(formatter)
                            + " – "
                            + nominalEndDate.format(
                            formatter
                    );

        } else {
            startDate =
                    anchor.withDayOfMonth(1);

            nominalEndDate =
                    startDate.with(
                            TemporalAdjusters
                                    .lastDayOfMonth()
                    );

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "MMMM yyyy",
                            Locale.getDefault()
                    );

            rangeLabel =
                    startDate.format(formatter);
        }

        LocalDate actualEndDate =
                nominalEndDate.isAfter(today)
                        ? today
                        : nominalEndDate;

        return new ReportRange(
                startDate,
                actualEndDate,
                rangeLabel
        );
    }

    private boolean canMoveNext() {
        LocalDate today = currentDate;

        if (selectedPeriod
                == ReportsUiState.Period.WEEK) {

            LocalDate nextWeekStart =
                    anchorDate.plusWeeks(1)
                            .with(
                                    TemporalAdjusters
                                            .previousOrSame(
                                                    DayOfWeek
                                                            .MONDAY
                                            )
                            );

            return !nextWeekStart.isAfter(today);
        }

        LocalDate nextMonthStart =
                anchorDate.plusMonths(1)
                        .withDayOfMonth(1);

        return !nextMonthStart.isAfter(today);
    }

    private void rebuildUiState() {
        ReportRange range =
                selectedRange.getValue();

        if (range == null) {
            return;
        }

        LocalDate earliestPrayerDate =
                findEarliestPrayerDate();

        int trackedPrayerDays =
                calculateTrackedDays(
                        earliestPrayerDate,
                        range.endDate
                );

        ReportsUiState.PrayerSummary
                prayerSummary =
                buildPrayerSummary(
                        trackedPrayerDays
                );

        List<ReportsUiState.PrayerBreakdownItem>
                prayerBreakdown =
                buildPrayerBreakdown(
                        trackedPrayerDays
                );

        ReportsUiState.HabitSummary
                habitSummary =
                buildHabitSummary();

        List<ReportsUiState.HabitBreakdownItem>
                habitBreakdown =
                buildHabitBreakdown();

        List<ReportsUiState.DailyBreakdownItem>
                dailyBreakdown =
                buildDailyBreakdown(
                        range,
                        earliestPrayerDate
                );

        boolean hasData =
                !latestPrayerRecords.isEmpty()
                        || !latestHabitRecords.isEmpty();

        uiState.setValue(
                new ReportsUiState(
                        selectedPeriod,
                        range.label,
                        canMoveNext(),
                        prayerSummary,
                        prayerBreakdown,
                        habitSummary,
                        habitBreakdown,
                        dailyBreakdown,
                        hasData
                )
        );
    }

    private ReportsUiState.PrayerSummary
    buildPrayerSummary(int trackedDays) {
        int onTime = 0;
        int late = 0;
        int missed = 0;

        for (PrayerRecord record
                : latestPrayerRecords) {

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
                    break;
            }
        }

        int expectedRecords =
                trackedDays
                        * PrayerType.values().length;

        int unrecorded =
                Math.max(
                        0,
                        expectedRecords
                                - onTime
                                - late
                                - missed
                );

        int answered =
                onTime + late + missed;

        double percentage =
                answered == 0
                        ? 0.0
                        : onTime * 100.0 / answered;

        return new ReportsUiState.PrayerSummary(
                trackedDays,
                onTime,
                late,
                missed,
                unrecorded,
                percentage
        );
    }

    private List<ReportsUiState.PrayerBreakdownItem>
    buildPrayerBreakdown(int trackedDays) {
        Map<PrayerType, int[]> counts =
                new EnumMap<>(PrayerType.class);

        for (PrayerType prayerType
                : PrayerType.values()) {

            /*
             * Index 0 = On Time
             * Index 1 = Late
             * Index 2 = Missed
             */
            counts.put(prayerType, new int[3]);
        }

        for (PrayerRecord record
                : latestPrayerRecords) {

            int[] prayerCounts =
                    counts.get(record.prayerType);

            if (prayerCounts == null) {
                continue;
            }

            switch (record.status) {
                case ON_TIME:
                    prayerCounts[0]++;
                    break;

                case LATE:
                    prayerCounts[1]++;
                    break;

                case MISSED:
                    prayerCounts[2]++;
                    break;

                case UNRECORDED:
                    break;
            }
        }

        List<ReportsUiState.PrayerBreakdownItem>
                items =
                new ArrayList<>();

        for (PrayerType prayerType
                : PrayerType.values()) {

            int[] prayerCounts =
                    counts.get(prayerType);

            int unrecorded =
                    Math.max(
                            0,
                            trackedDays
                                    - prayerCounts[0]
                                    - prayerCounts[1]
                                    - prayerCounts[2]
                    );

            items.add(
                    new ReportsUiState
                            .PrayerBreakdownItem(
                            prayerType,
                            prayerCounts[0],
                            prayerCounts[1],
                            prayerCounts[2],
                            unrecorded
                    )
            );
        }

        return items;
    }

    private ReportsUiState.HabitSummary
    buildHabitSummary() {
        int completed = 0;
        int notCompleted = 0;
        int pending = 0;

        for (HabitRecord record
                : latestHabitRecords) {

            switch (record.status) {
                case COMPLETED:
                    completed++;
                    break;

                case NOT_COMPLETED:
                    notCompleted++;
                    break;

                case PENDING:
                    pending++;
                    break;
            }
        }

        int answered =
                completed + notCompleted;

        double percentage =
                answered == 0
                        ? 0.0
                        : completed * 100.0 / answered;

        return new ReportsUiState.HabitSummary(
                latestHabitRecords.size(),
                completed,
                notCompleted,
                pending,
                percentage
        );
    }

    private List<ReportsUiState.HabitBreakdownItem>
    buildHabitBreakdown() {
        Map<Long, Habit> habitsById =
                new LinkedHashMap<>();

        for (Habit habit : latestHabits) {
            habitsById.put(habit.id, habit);
        }

        Map<Long, int[]> countsByHabit =
                new HashMap<>();

        for (HabitRecord record
                : latestHabitRecords) {

            int[] counts =
                    countsByHabit.computeIfAbsent(
                            record.habitId,
                            ignored -> new int[3]
                    );

            switch (record.status) {
                case COMPLETED:
                    counts[0]++;
                    break;

                case NOT_COMPLETED:
                    counts[1]++;
                    break;

                case PENDING:
                    counts[2]++;
                    break;
            }
        }

        List<ReportsUiState.HabitBreakdownItem>
                items =
                new ArrayList<>();

        for (Habit habit : latestHabits) {
            int[] counts =
                    countsByHabit.get(habit.id);

            if (counts == null) {
                continue;
            }

            int trackedDays =
                    counts[0] + counts[1] + counts[2];

            int answered =
                    counts[0] + counts[1];

            double percentage =
                    answered == 0
                            ? 0.0
                            : counts[0]
                              * 100.0
                              / answered;

            items.add(
                    new ReportsUiState
                            .HabitBreakdownItem(
                            habit.id,
                            habit.title,
                            trackedDays,
                            counts[0],
                            counts[1],
                            counts[2],
                            percentage
                    )
            );
        }

        return items;
    }

    private List<ReportsUiState.DailyBreakdownItem>
    buildDailyBreakdown(
            ReportRange range,
            LocalDate earliestPrayerDate
    ) {
        LocalDate earliestHabitDate =
                findEarliestHabitDate();

        LocalDate earliestAnyDate =
                earlierOf(
                        earliestPrayerDate,
                        earliestHabitDate
                );

        if (earliestAnyDate == null) {
            return Collections.emptyList();
        }

        if (earliestAnyDate
                .isBefore(range.startDate)) {
            earliestAnyDate = range.startDate;
        }

        Map<String, List<PrayerRecord>>
                prayersByDate =
                new HashMap<>();

        for (PrayerRecord record
                : latestPrayerRecords) {

            prayersByDate
                    .computeIfAbsent(
                            record.recordDate,
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(record);
        }

        Map<String, List<HabitRecord>>
                habitsByDate =
                new HashMap<>();

        for (HabitRecord record
                : latestHabitRecords) {

            habitsByDate
                    .computeIfAbsent(
                            record.recordDate,
                            ignored ->
                                    new ArrayList<>()
                    )
                    .add(record);
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, d MMMM",
                        Locale.getDefault()
                );

        List<ReportsUiState.DailyBreakdownItem>
                items =
                new ArrayList<>();

        LocalDate date = range.endDate;

        while (!date.isBefore(earliestAnyDate)) {
            String dateValue = date.toString();

            int prayerOnTime = 0;
            int prayerLate = 0;
            int prayerMissed = 0;

            List<PrayerRecord> datePrayers =
                    prayersByDate.getOrDefault(
                            dateValue,
                            Collections.emptyList()
                    );

            for (PrayerRecord record : datePrayers) {
                switch (record.status) {
                    case ON_TIME:
                        prayerOnTime++;
                        break;

                    case LATE:
                        prayerLate++;
                        break;

                    case MISSED:
                        prayerMissed++;
                        break;

                    case UNRECORDED:
                        break;
                }
            }

            boolean prayerTrackingStarted =
                    earliestPrayerDate != null
                            && !date.isBefore(
                            earliestPrayerDate
                    );

            int prayerUnrecorded =
                    prayerTrackingStarted
                            ? Math.max(
                            0,
                            PrayerType.values().length
                            - prayerOnTime
                            - prayerLate
                            - prayerMissed
                    )
                            : 0;

            int habitsCompleted = 0;
            int habitsNotCompleted = 0;
            int habitsPending = 0;

            List<HabitRecord> dateHabits =
                    habitsByDate.getOrDefault(
                            dateValue,
                            Collections.emptyList()
                    );

            for (HabitRecord record : dateHabits) {
                switch (record.status) {
                    case COMPLETED:
                        habitsCompleted++;
                        break;

                    case NOT_COMPLETED:
                        habitsNotCompleted++;
                        break;

                    case PENDING:
                        habitsPending++;
                        break;
                }
            }

            items.add(
                    new ReportsUiState
                            .DailyBreakdownItem(
                            date.format(formatter),
                            prayerOnTime,
                            prayerLate,
                            prayerMissed,
                            prayerUnrecorded,
                            habitsCompleted,
                            habitsNotCompleted,
                            habitsPending
                    )
            );

            date = date.minusDays(1);
        }

        return items;
    }

    private LocalDate findEarliestPrayerDate() {
        LocalDate earliest = null;

        for (PrayerRecord record
                : latestPrayerRecords) {

            LocalDate recordDate =
                    LocalDate.parse(
                            record.recordDate
                    );

            if (earliest == null
                    || recordDate.isBefore(earliest)) {
                earliest = recordDate;
            }
        }

        return earliest;
    }

    private LocalDate findEarliestHabitDate() {
        LocalDate earliest = null;

        for (HabitRecord record
                : latestHabitRecords) {

            LocalDate recordDate =
                    LocalDate.parse(
                            record.recordDate
                    );

            if (earliest == null
                    || recordDate.isBefore(earliest)) {
                earliest = recordDate;
            }
        }

        return earliest;
    }

    private int calculateTrackedDays(
            LocalDate earliestDate,
            LocalDate endDate
    ) {
        if (earliestDate == null
                || earliestDate.isAfter(endDate)) {
            return 0;
        }

        return Math.toIntExact(
                ChronoUnit.DAYS.between(
                        earliestDate,
                        endDate
                ) + 1
        );
    }

    private LocalDate earlierOf(
            LocalDate first,
            LocalDate second
    ) {
        if (first == null) {
            return second;
        }

        if (second == null) {
            return first;
        }

        return first.isBefore(second)
                ? first
                : second;
    }

    private static final class ReportRange {

        private final LocalDate startDate;
        private final LocalDate endDate;
        private final String label;

        private ReportRange(
                LocalDate startDate,
                LocalDate endDate,
                String label
        ) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.label = label;
        }
    }
}