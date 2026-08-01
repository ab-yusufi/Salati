package com.abcoder.salati.ui.today;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.entity.HabitRecord;
import com.abcoder.salati.data.entity.PrayerRecord;
import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.data.repository.PrayerRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TodayViewModel extends ViewModel {

    private final PrayerRepository prayerRepository;
    private final HabitRepository habitRepository;

    private final MutableLiveData<LocalDate> currentDate =
            new MutableLiveData<>(
                    LocalDate.now()
            );

    private final MediatorLiveData<List<PrayerRecord>>
            todayPrayerRecords =
            new MediatorLiveData<>();

    private final MediatorLiveData<List<HabitTodayItem>>
            todayHabitItems =
            new MediatorLiveData<>();

    /*
     * This is separate from todayHabitItems because an empty list
     * can mean two different things:
     *
     * 1. Room is still loading.
     * 2. Room finished and there are no enabled habits.
     */
    private final MutableLiveData<Boolean>
            habitDataLoaded =
            new MutableLiveData<>(false);

    private LiveData<List<PrayerRecord>>
            activePrayerSource;

    private LiveData<List<HabitRecord>>
            activeHabitRecordSource;

    private List<Habit> enabledHabits =
            new ArrayList<>();

    private List<HabitRecord> todayHabitRecords =
            new ArrayList<>();

    /*
     * Habit items must not be built until both Room queries
     * have emitted at least once.
     */
    private boolean enabledHabitsLoaded;
    private boolean todayHabitRecordsLoaded;

    public TodayViewModel(
            PrayerRepository prayerRepository,
            HabitRepository habitRepository
    ) {
        this.prayerRepository =
                prayerRepository;

        this.habitRepository =
                habitRepository;

        todayHabitItems.addSource(
                habitRepository.observeEnabledHabits(),
                habits -> {
                    enabledHabits =
                            habits == null
                                    ? new ArrayList<>()
                                    : habits;

                    enabledHabitsLoaded = true;

                    rebuildHabitItems();
                }
        );

        switchDateSources(
                getCurrentDateValue()
        );
    }

    public LiveData<LocalDate> getCurrentDate() {
        return currentDate;
    }

    public LiveData<List<PrayerRecord>>
    getTodayPrayerRecords() {
        return todayPrayerRecords;
    }

    public LiveData<List<HabitTodayItem>>
    getTodayHabitItems() {
        return todayHabitItems;
    }

    public LiveData<Boolean> getHabitDataLoaded() {
        return habitDataLoaded;
    }

    /**
     * Checks whether the device date has changed.
     *
     * @return true only when the current date changed.
     */
    public boolean refreshDateIfNeeded() {
        LocalDate newDate =
                LocalDate.now();

        LocalDate previousDate =
                getCurrentDateValue();

        if (newDate.equals(previousDate)) {
            return false;
        }

        currentDate.setValue(newDate);

        switchDateSources(newDate);

        return true;
    }

    public String getToday() {
        return getCurrentDateValue()
                .toString();
    }

    public String getDisplayDate() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, d MMMM yyyy",
                        Locale.getDefault()
                );

        return getCurrentDateValue()
                .format(formatter);
    }

    public void setPrayerStatus(
            PrayerType prayerType,
            PrayerStatus prayerStatus
    ) {
        prayerRepository.setPrayerStatus(
                getToday(),
                prayerType,
                prayerStatus,
                AnswerSource.APP
        );
    }

    public void setPrayerStatus(
            PrayerType prayerType,
            PrayerStatus prayerStatus,
            PrayerRepository.OperationCallback callback
    ) {
        prayerRepository.setPrayerStatus(
                getToday(),
                prayerType,
                prayerStatus,
                AnswerSource.APP,
                callback
        );
    }

    public void setHabitStatus(
            long habitId,
            HabitStatus habitStatus
    ) {
        habitRepository.setHabitStatus(
                habitId,
                getToday(),
                habitStatus,
                AnswerSource.APP
        );
    }

    public void setHabitStatus(
            long habitId,
            HabitStatus habitStatus,
            HabitRepository.StatusOperationCallback callback
    ) {
        habitRepository.setHabitStatus(
                habitId,
                getToday(),
                habitStatus,
                AnswerSource.APP,
                callback
        );
    }

    private void switchDateSources(
            LocalDate date
    ) {
        String dateValue =
                date.toString();

        if (activePrayerSource != null) {
            todayPrayerRecords.removeSource(
                    activePrayerSource
            );
        }

        if (activeHabitRecordSource != null) {
            todayHabitItems.removeSource(
                    activeHabitRecordSource
            );
        }

        /*
         * Clear the previous prayer values while Room switches
         * to the new date.
         */
        todayPrayerRecords.setValue(
                new ArrayList<>()
        );

        /*
         * Do not publish an empty habit list here. Publishing an
         * empty list would incorrectly display the empty state
         * while Room is still loading.
         */
        todayHabitRecords =
                new ArrayList<>();

        todayHabitRecordsLoaded = false;

        habitDataLoaded.setValue(false);

        prayerRepository.ensurePrayerRecordsForDate(
                dateValue
        );

        habitRepository.ensureRecordsForDate(
                dateValue
        );

        activePrayerSource =
                prayerRepository.observeForDate(
                        dateValue
                );

        todayPrayerRecords.addSource(
                activePrayerSource,
                records ->
                        todayPrayerRecords.setValue(
                                records == null
                                        ? new ArrayList<>()
                                        : records
                        )
        );

        activeHabitRecordSource =
                habitRepository.observeRecordsForDate(
                        dateValue
                );

        todayHabitItems.addSource(
                activeHabitRecordSource,
                records -> {
                    todayHabitRecords =
                            records == null
                                    ? new ArrayList<>()
                                    : records;

                    todayHabitRecordsLoaded = true;

                    rebuildHabitItems();
                }
        );
    }

    private LocalDate getCurrentDateValue() {
        LocalDate date =
                currentDate.getValue();

        return date == null
                ? LocalDate.now()
                : date;
    }

    private void rebuildHabitItems() {
        /*
         * Wait until both Room queries have returned.
         *
         * Without this check, an initial empty enabled-habit
         * value or empty date-record value can temporarily
         * produce a false empty state.
         */
        if (!enabledHabitsLoaded
                || !todayHabitRecordsLoaded) {

            return;
        }

        Map<Long, HabitRecord> recordMap =
                new HashMap<>();

        for (HabitRecord record :
                todayHabitRecords) {

            recordMap.put(
                    record.habitId,
                    record
            );
        }

        List<HabitTodayItem> items =
                new ArrayList<>();

        for (Habit habit : enabledHabits) {
            HabitRecord record =
                    recordMap.get(
                            habit.id
                    );

            if (record == null) {
                items.add(
                        new HabitTodayItem(
                                habit,
                                HabitStatus.PENDING,
                                0
                        )
                );

            } else {
                items.add(
                        new HabitTodayItem(
                                habit,
                                record.status,
                                record.snoozeCount
                        )
                );
            }
        }

        /*
         * Publish the completed list before marking loading as
         * finished. The Fragment will therefore always have the
         * latest list when it renders the loaded state.
         */
        todayHabitItems.setValue(items);
        habitDataLoaded.setValue(true);
    }
}