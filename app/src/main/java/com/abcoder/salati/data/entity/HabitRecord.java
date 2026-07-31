package com.abcoder.salati.data.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.HabitStatus;

@Entity(
        tableName = "habit_records",
        primaryKeys = {"habitId", "recordDate"},
        foreignKeys = {
                @ForeignKey(
                        entity = Habit.class,
                        parentColumns = "id",
                        childColumns = "habitId",
                        onDelete = CASCADE
                )
        },
        indices = {
                @Index(value = "habitId")
        }
)

public class HabitRecord {

    public long habitId;

    /**
     * Stored using ISO format: YYYY-MM-DD.
     */
    @NonNull
    public String recordDate;

    @NonNull
    public HabitStatus status;

    @Nullable
    public Long answeredAt;

    @Nullable
    public AnswerSource answerSource;

    public int snoozeCount;

    public long createdAt;

    public long updatedAt;

    public HabitRecord(
            long habitId,
            @NonNull String recordDate,
            @NonNull HabitStatus status,
            @Nullable Long answeredAt,
            @Nullable AnswerSource answerSource,
            int snoozeCount,
            long createdAt,
            long updatedAt
    ) {
        this.habitId = habitId;
        this.recordDate = recordDate;
        this.status = status;
        this.answeredAt = answeredAt;
        this.answerSource = answerSource;
        this.snoozeCount = snoozeCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
