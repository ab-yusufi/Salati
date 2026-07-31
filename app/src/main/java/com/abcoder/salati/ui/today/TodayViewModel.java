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

public class TodayViewModel
        extends ViewModel {

    private final PrayerRepository prayerRepository;
    private final HabitRepository habitRepository;

    private final MutableLiveData<LocalDate>
            currentDate =
            new MutableLiveData<>(
                    LocalDate.now()
            );

    private final MediatorLiveData<
            List<PrayerRecord>>
            todayPrayerRecords =
            new MediatorLiveData<>();

    private final MediatorLiveData<
            List<HabitTodayItem>>
            todayHabitItems =
            new MediatorLiveData<>();

    private LiveData<List<PrayerRecord>>
            activePrayerSource;

    private LiveData<List<HabitRecord>>
            activeHabitRecordSource;

    private List<Habit> enabledHabits =
            new ArrayList<>();

    private List<HabitRecord> todayHabitRecords =
            new ArrayList<>();

    public TodayViewModel(
            PrayerRepository prayerRepository,
            HabitRepository habitRepository
    ) {
        this.prayerRepository =
                prayerRepository;

        this.habitRepository =
                habitRepository;

        todayHabitItems.addSource(
                habitRepository
                        .observeEnabledHabits(),
                habits -> {
                    enabledHabits =
                            habits == null
                                    ? new ArrayList<>()
                                    : habits;

                    rebuildHabitItems();
                }
        );

        switchDateSources(
                getCurrentDateValue()
        );
    }

    public LiveData<LocalDate>
    getCurrentDate() {
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

    /**
     * Returns true only when the device date changed.
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
         * Clear yesterday's values immediately while Room loads
         * the newly selected date.
         */
        todayPrayerRecords.setValue(
                new ArrayList<>()
        );

        todayHabitRecords =
                new ArrayList<>();

        rebuildHabitItems();

        prayerRepository
                .ensurePrayerRecordsForDate(
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
                habitRepository
                        .observeRecordsForDate(
                                dateValue
                        );

        todayHabitItems.addSource(
                activeHabitRecordSource,
                records -> {
                    todayHabitRecords =
                            records == null
                                    ? new ArrayList<>()
                                    : records;

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

        todayHabitItems.setValue(items);
    }
}