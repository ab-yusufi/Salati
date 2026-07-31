package com.abcoder.salati.ui.calendar;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.entity.HabitRecord;
import com.abcoder.salati.data.entity.PrayerRecord;
import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.data.repository.PrayerRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;

public final class CalendarViewModel
        extends ViewModel {

    private final PrayerRepository prayerRepository;
    private final HabitRepository habitRepository;
    private final MutableLiveData<LocalDate>
            currentDate =
            new MutableLiveData<>(
                    LocalDate.now()
            );
    private final MutableLiveData<YearMonth>
            displayedMonth =
            new MutableLiveData<>(
                    YearMonth.now()
            );

    private final MutableLiveData<LocalDate>
            selectedDate =
            new MutableLiveData<>(
                    LocalDate.now()
            );

    private final LiveData<List<PrayerRecord>>
            monthPrayerRecords;

    private final LiveData<List<HabitRecord>>
            monthHabitRecords;

    private final LiveData<List<PrayerRecord>>
            selectedPrayerRecords;

    private final LiveData<List<HabitRecord>>
            selectedHabitRecords;

    private final LiveData<List<Habit>>
            allHabits;

    public CalendarViewModel(
            PrayerRepository prayerRepository,
            HabitRepository habitRepository
    ) {
        this.prayerRepository =
                prayerRepository;

        this.habitRepository =
                habitRepository;

        monthPrayerRecords =
                Transformations.switchMap(
                        displayedMonth,
                        month ->
                                prayerRepository
                                        .observeBetweenDates(
                                                month
                                                        .atDay(1)
                                                        .toString(),
                                                month
                                                        .atEndOfMonth()
                                                        .toString()
                                        )
                );

        monthHabitRecords =
                Transformations.switchMap(
                        displayedMonth,
                        month ->
                                habitRepository
                                        .observeRecordsBetweenDates(
                                                month
                                                        .atDay(1)
                                                        .toString(),
                                                month
                                                        .atEndOfMonth()
                                                        .toString()
                                        )
                );

        selectedPrayerRecords =
                Transformations.switchMap(
                        selectedDate,
                        date -> {
                            prayerRepository
                                    .ensurePrayerRecordsForDate(
                                            date.toString()
                                    );

                            return prayerRepository
                                    .observeForDate(
                                            date.toString()
                                    );
                        }
                );

        selectedHabitRecords =
                Transformations.switchMap(
                        selectedDate,
                        date -> {
                            /*
                             * Only create missing habit records for
                             * today. Creating current habits for an
                             * arbitrary historical date would invent
                             * history that did not exist.
                             */
                            if (date.equals(
                                    getToday()
                            )) {
                                habitRepository
                                        .ensureRecordsForDate(
                                                date.toString()
                                        );
                            }

                            return habitRepository
                                    .observeRecordsForDate(
                                            date.toString()
                                    );
                        }
                );

        allHabits =
                habitRepository.observeAllHabits();
    }

    public LiveData<LocalDate>
    getCurrentDate() {
        return currentDate;
    }

    public LocalDate getToday() {
        LocalDate value =
                currentDate.getValue();

        return value == null
                ? LocalDate.now()
                : value;
    }

    public boolean refreshForCurrentDate() {
        LocalDate previousDate =
                getToday();

        LocalDate newDate =
                LocalDate.now();

        if (newDate.equals(previousDate)) {
            return false;
        }

        LocalDate currentSelection =
                selectedDate.getValue();

        boolean selectionFollowedToday =
                currentSelection != null
                        && currentSelection.equals(
                        previousDate
                );

        currentDate.setValue(newDate);

        /*
         * Calendar follows the new date only when the user was
         * looking at the previous "today". Historical selections
         * remain selected.
         */
        if (selectionFollowedToday) {
            displayedMonth.setValue(
                    YearMonth.from(newDate)
            );

            selectedDate.setValue(newDate);
        }

        return true;
    }
    public LiveData<YearMonth>
    getDisplayedMonth() {
        return displayedMonth;
    }

    public LiveData<LocalDate>
    getSelectedDate() {
        return selectedDate;
    }

    public LiveData<List<PrayerRecord>>
    getMonthPrayerRecords() {
        return monthPrayerRecords;
    }

    public LiveData<List<HabitRecord>>
    getMonthHabitRecords() {
        return monthHabitRecords;
    }

    public LiveData<List<PrayerRecord>>
    getSelectedPrayerRecords() {
        return selectedPrayerRecords;
    }

    public LiveData<List<HabitRecord>>
    getSelectedHabitRecords() {
        return selectedHabitRecords;
    }

    public LiveData<List<Habit>>
    getAllHabits() {
        return allHabits;
    }

    public void selectDate(
            LocalDate date
    ) {
        if (date == null
                || date.isAfter(
                getToday()
        )) {
            return;
        }

        displayedMonth.setValue(
                YearMonth.from(date)
        );

        selectedDate.setValue(date);
    }

    public void showPreviousMonth() {
        moveMonth(-1);
    }

    public void showNextMonth() {
        moveMonth(1);
    }

    public void showToday() {
        selectDate(
                getToday()
        );
    }
    public void setPrayerStatus(
            LocalDate date,
            PrayerType prayerType,
            PrayerStatus prayerStatus,
            PrayerRepository.OperationCallback callback
    ) {
        if (!isEditableDate(date)) {
            if (callback != null) {
                callback.onError(
                        new IllegalArgumentException(
                                "Future dates cannot be edited"
                        )
                );
            }

            return;
        }

        prayerRepository.setPrayerStatus(
                date.toString(),
                prayerType,
                prayerStatus,
                AnswerSource.APP,
                callback
        );
    }

    public void setHabitStatus(
            LocalDate date,
            long habitId,
            HabitStatus habitStatus,
            HabitRepository.StatusOperationCallback callback
    ) {
        if (!isEditableDate(date)) {
            if (callback != null) {
                callback.onError(
                        new IllegalArgumentException(
                                "Future dates cannot be edited"
                        )
                );
            }

            return;
        }

        habitRepository.setHabitStatus(
                habitId,
                date.toString(),
                habitStatus,
                AnswerSource.APP,
                callback
        );
    }

    private boolean isEditableDate(
            LocalDate date
    ) {
        return date != null
                && !date.isAfter(
                getToday()
        );
    }
    public boolean canShowNextMonth() {
        YearMonth current =
                displayedMonth.getValue();

        return current != null
                && current.isBefore(
                YearMonth.from(
                        getToday()
                )
        );
    }

    private void moveMonth(
            long monthDifference
    ) {
        YearMonth currentMonth =
                displayedMonth.getValue();

        LocalDate currentSelection =
                selectedDate.getValue();

        if (currentMonth == null) {
            currentMonth = YearMonth.now();
        }

        if (currentSelection == null) {
            currentSelection =
                    getToday();
        }

        YearMonth targetMonth =
                currentMonth.plusMonths(
                        monthDifference
                );

        if (targetMonth.isAfter(
                YearMonth.now()
        )) {
            return;
        }

        int targetDay =
                Math.min(
                        currentSelection
                                .getDayOfMonth(),
                        targetMonth
                                .lengthOfMonth()
                );

        LocalDate targetDate =
                targetMonth.atDay(
                        targetDay
                );

        if (targetDate.isAfter(
                getToday()
        )) {
            targetDate = LocalDate.now();
        }

        displayedMonth.setValue(
                targetMonth
        );

        selectedDate.setValue(
                targetDate
        );
    }
}