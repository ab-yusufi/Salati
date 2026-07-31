package com.abcoder.salati.ui.today;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.entity.HabitRecord;
import com.abcoder.salati.data.entity.PrayerRecord;
import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.data.repository.PrayerRepository;

public class TodayViewModel extends ViewModel {

    private final PrayerRepository prayerRepository;
    private final HabitRepository habitRepository;

    private final String today;

    private final LiveData<List<PrayerRecord>>
            todayPrayerRecords;

    private final MediatorLiveData<List<HabitTodayItem>>
            todayHabitItems =
            new MediatorLiveData<>();

    private List<Habit> enabledHabits =
            new ArrayList<>();

    private List<HabitRecord> todayHabitRecords =
            new ArrayList<>();

    public TodayViewModel(
            PrayerRepository prayerRepository,
            HabitRepository habitRepository
    ) {
        this.prayerRepository = prayerRepository;
        this.habitRepository = habitRepository;

        today = LocalDate.now().toString();

        todayPrayerRecords =
                prayerRepository.observeForDate(today);

        prayerRepository
                .ensurePrayerRecordsForDate(today);

        habitRepository.ensureRecordsForDate(today);

        todayHabitItems.addSource(
                habitRepository.observeEnabledHabits(),
                habits -> {
                    enabledHabits = habits == null
                            ? new ArrayList<>()
                            : habits;

                    rebuildHabitItems();
                }
        );

        todayHabitItems.addSource(
                habitRepository
                        .observeRecordsForDate(today),
                records -> {
                    todayHabitRecords = records == null
                            ? new ArrayList<>()
                            : records;

                    rebuildHabitItems();
                }
        );
    }

    public LiveData<List<PrayerRecord>>
    getTodayPrayerRecords() {
        return todayPrayerRecords;
    }

    public LiveData<List<HabitTodayItem>>
    getTodayHabitItems() {
        return todayHabitItems;
    }

    public String getToday() {
        return today;
    }

    public String getDisplayDate() {
        LocalDate localDate =
                LocalDate.parse(today);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, d MMMM yyyy",
                        Locale.getDefault()
                );

        return localDate.format(formatter);
    }

    public void setPrayerStatus(
            PrayerType prayerType,
            PrayerStatus prayerStatus
    ) {
        prayerRepository.setPrayerStatus(
                today,
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
                today,
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
                today,
                habitStatus,
                AnswerSource.APP
        );
    }

    private void rebuildHabitItems() {
        Map<Long, HabitRecord> recordMap =
                new HashMap<>();

        for (HabitRecord record : todayHabitRecords) {
            recordMap.put(record.habitId, record);
        }

        List<HabitTodayItem> items =
                new ArrayList<>();

        for (Habit habit : enabledHabits) {
            HabitRecord record =
                    recordMap.get(habit.id);

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