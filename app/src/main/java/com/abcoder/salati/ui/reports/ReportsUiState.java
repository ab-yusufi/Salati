package com.abcoder.salati.ui.reports;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import com.abcoder.salati.data.model.PrayerType;

public final class ReportsUiState {

    public enum Period {
        WEEK,
        MONTH
    }

    @NonNull
    public final Period period;

    @NonNull
    public final String rangeLabel;

    public final boolean canGoNext;

    @NonNull
    public final PrayerSummary prayerSummary;

    @NonNull
    public final List<PrayerBreakdownItem>
            prayerBreakdown;

    @NonNull
    public final HabitSummary habitSummary;

    @NonNull
    public final List<HabitBreakdownItem>
            habitBreakdown;

    @NonNull
    public final List<DailyBreakdownItem>
            dailyBreakdown;

    public final boolean loading;
    public final boolean hasData;

    public ReportsUiState(
            @NonNull Period period,
            @NonNull String rangeLabel,
            boolean canGoNext,
            @NonNull PrayerSummary prayerSummary,
            @NonNull List<PrayerBreakdownItem>
                    prayerBreakdown,
            @NonNull HabitSummary habitSummary,
            @NonNull List<HabitBreakdownItem>
                    habitBreakdown,
            @NonNull List<DailyBreakdownItem>
                    dailyBreakdown,
            boolean loading,
            boolean hasData
    ) {
        this.period = period;
        this.rangeLabel = rangeLabel;
        this.canGoNext = canGoNext;
        this.prayerSummary = prayerSummary;
        this.prayerBreakdown =
                Collections.unmodifiableList(
                        prayerBreakdown
                );
        this.habitSummary = habitSummary;
        this.habitBreakdown =
                Collections.unmodifiableList(
                        habitBreakdown
                );
        this.dailyBreakdown =
                Collections.unmodifiableList(
                        dailyBreakdown
                );
        this.loading = loading;
        this.hasData = hasData;
    }

    public static final class PrayerSummary {

        public final int trackedDays;
        public final int onTime;
        public final int late;
        public final int missed;
        public final int unrecorded;
        public final double onTimePercentage;

        public PrayerSummary(
                int trackedDays,
                int onTime,
                int late,
                int missed,
                int unrecorded,
                double onTimePercentage
        ) {
            this.trackedDays = trackedDays;
            this.onTime = onTime;
            this.late = late;
            this.missed = missed;
            this.unrecorded = unrecorded;
            this.onTimePercentage =
                    onTimePercentage;
        }
    }

    public static final class PrayerBreakdownItem {

        @NonNull
        public final PrayerType prayerType;

        public final int onTime;
        public final int late;
        public final int missed;
        public final int unrecorded;

        public PrayerBreakdownItem(
                @NonNull PrayerType prayerType,
                int onTime,
                int late,
                int missed,
                int unrecorded
        ) {
            this.prayerType = prayerType;
            this.onTime = onTime;
            this.late = late;
            this.missed = missed;
            this.unrecorded = unrecorded;
        }
    }

    public static final class HabitSummary {

        public final int trackedRecords;
        public final int completed;
        public final int notCompleted;
        public final int pending;
        public final double completionPercentage;

        public HabitSummary(
                int trackedRecords,
                int completed,
                int notCompleted,
                int pending,
                double completionPercentage
        ) {
            this.trackedRecords = trackedRecords;
            this.completed = completed;
            this.notCompleted = notCompleted;
            this.pending = pending;
            this.completionPercentage =
                    completionPercentage;
        }
    }

    public static final class HabitBreakdownItem {

        public final long habitId;

        @NonNull
        public final String title;

        public final int trackedDays;
        public final int completed;
        public final int notCompleted;
        public final int pending;
        public final double completionPercentage;

        public HabitBreakdownItem(
                long habitId,
                @NonNull String title,
                int trackedDays,
                int completed,
                int notCompleted,
                int pending,
                double completionPercentage
        ) {
            this.habitId = habitId;
            this.title = title;
            this.trackedDays = trackedDays;
            this.completed = completed;
            this.notCompleted = notCompleted;
            this.pending = pending;
            this.completionPercentage =
                    completionPercentage;
        }
    }

    public static final class DailyBreakdownItem {

        @NonNull
        public final String displayDate;

        public final int prayerOnTime;
        public final int prayerLate;
        public final int prayerMissed;
        public final int prayerUnrecorded;

        public final int habitsCompleted;
        public final int habitsNotCompleted;
        public final int habitsPending;

        public DailyBreakdownItem(
                @NonNull String displayDate,
                int prayerOnTime,
                int prayerLate,
                int prayerMissed,
                int prayerUnrecorded,
                int habitsCompleted,
                int habitsNotCompleted,
                int habitsPending
        ) {
            this.displayDate = displayDate;
            this.prayerOnTime = prayerOnTime;
            this.prayerLate = prayerLate;
            this.prayerMissed = prayerMissed;
            this.prayerUnrecorded =
                    prayerUnrecorded;
            this.habitsCompleted =
                    habitsCompleted;
            this.habitsNotCompleted =
                    habitsNotCompleted;
            this.habitsPending = habitsPending;
        }
    }
}