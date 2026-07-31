package com.abcoder.salati.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title;

    public int reminderHour;

    public int reminderMinute;

    public int snoozeMinutes;

    public boolean enabled;

    public long createdAt;

    public long updatedAt;

    public Habit(
            long id,
            @NonNull String title,
            int reminderHour,
            int reminderMinute,
            int snoozeMinutes,
            boolean enabled,
            long createdAt,
            long updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.reminderHour = reminderHour;
        this.reminderMinute = reminderMinute;
        this.snoozeMinutes = snoozeMinutes;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}