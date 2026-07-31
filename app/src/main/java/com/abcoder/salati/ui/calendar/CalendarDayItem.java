package com.abcoder.salati.ui.calendar;

import java.time.LocalDate;

public final class CalendarDayItem {

    public final LocalDate date;

    public final boolean inDisplayedMonth;
    public final boolean selected;
    public final boolean today;
    public final boolean future;

    public final boolean hasPrayerRecords;
    public final int recordedPrayerCount;

    public final int completedHabitCount;
    public final int habitRecordCount;

    public CalendarDayItem(
            LocalDate date,
            boolean inDisplayedMonth,
            boolean selected,
            boolean today,
            boolean future,
            boolean hasPrayerRecords,
            int recordedPrayerCount,
            int completedHabitCount,
            int habitRecordCount
    ) {
        this.date = date;
        this.inDisplayedMonth = inDisplayedMonth;
        this.selected = selected;
        this.today = today;
        this.future = future;
        this.hasPrayerRecords = hasPrayerRecords;
        this.recordedPrayerCount = recordedPrayerCount;
        this.completedHabitCount = completedHabitCount;
        this.habitRecordCount = habitRecordCount;
    }
}