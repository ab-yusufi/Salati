package com.abcoder.salati.ui.today;

import java.time.LocalDate;

public final class WeekDayItem {

    private final LocalDate date;
    private final boolean today;

    public WeekDayItem(
            LocalDate date,
            boolean today
    ) {
        this.date = date;
        this.today = today;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isToday() {
        return today;
    }
}