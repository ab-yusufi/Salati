package com.abcoder.salati.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;

import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;

@Entity(
        tableName = "prayer_records",
        primaryKeys = {"recordDate", "prayerType"}
)

public class PrayerRecord {

    /**
     * Stored using ISO format: YYYY-MM-DD.
     * Example: 2026-07-31
     */
    @NonNull
    public String recordDate;

    @NonNull
    public PrayerType prayerType;

    @NonNull
    public PrayerStatus status;

    /**
     * Expected notification time as milliseconds since Unix epoch.
     */
    public long scheduledAt;

    /**
     * Null until the user records a result.
     */
    @Nullable
    public Long answeredAt;

    /**
     * Null while the prayer is still unrecorded.
     */
    @Nullable
    public AnswerSource answerSource;

    public int notificationId;

    public long createdAt;

    public long updatedAt;

    public PrayerRecord(
            @NonNull String recordDate,
            @NonNull PrayerType prayerType,
            @NonNull PrayerStatus status,
            long scheduledAt,
            @Nullable Long answeredAt,
            @Nullable AnswerSource answerSource,
            int notificationId,
            long createdAt,
            long updatedAt
    ) {
        this.recordDate = recordDate;
        this.prayerType = prayerType;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.answeredAt = answeredAt;
        this.answerSource = answerSource;
        this.notificationId = notificationId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
