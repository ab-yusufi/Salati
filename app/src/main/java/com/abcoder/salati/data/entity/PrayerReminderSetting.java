package com.abcoder.salati.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.abcoder.salati.data.model.PrayerType;

@Entity(tableName = "prayer_reminder_settings")


public class PrayerReminderSetting {

    @PrimaryKey
    @NonNull
    public PrayerType prayerType;

    public int hour;

    public int minute;

    public boolean enabled;

    public PrayerReminderSetting(
            @NonNull PrayerType prayerType,
            int hour,
            int minute,
            boolean enabled
    ) {
        this.prayerType = prayerType;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
    }
}
