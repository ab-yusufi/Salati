package com.abcoder.salati.data.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import java.util.List;

import androidx.lifecycle.LiveData;

import com.abcoder.salati.data.entity.PrayerRecord;
import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
@Dao
public interface PrayerRecordDao {
    @Upsert
    void upsert(PrayerRecord prayerRecord);

    @Query(
            "SELECT * FROM prayer_records " +
                    "WHERE recordDate = :recordDate " +
                    "AND prayerType = :prayerType " +
                    "LIMIT 1"
    )
    PrayerRecord getByDateAndType(
            String recordDate,
            PrayerType prayerType
    );
    @Query(
            "SELECT * FROM prayer_records " +
                    "WHERE recordDate = :recordDate " +
                    "ORDER BY CASE prayerType " +
                    "WHEN 'FAJR' THEN 1 " +
                    "WHEN 'DHUHR' THEN 2 " +
                    "WHEN 'ASR' THEN 3 " +
                    "WHEN 'MAGHRIB' THEN 4 " +
                    "WHEN 'ISHA' THEN 5 " +
                    "ELSE 6 END"
    )
    List<PrayerRecord> getForDate(String recordDate);

    @Query(
            "SELECT * FROM prayer_records " +
                    "WHERE recordDate BETWEEN :startDate AND :endDate " +
                    "ORDER BY recordDate ASC"
    )
    List<PrayerRecord> getBetweenDates(
            String startDate,
            String endDate
    );

    @Query(
            "UPDATE prayer_records " +
                    "SET status = :status, " +
                    "answeredAt = :answeredAt, " +
                    "answerSource = :answerSource, " +
                    "updatedAt = :updatedAt " +
                    "WHERE recordDate = :recordDate " +
                    "AND prayerType = :prayerType"
    )
    int updateStatus(
            String recordDate,
            PrayerType prayerType,
            PrayerStatus status,
            Long answeredAt,
            AnswerSource answerSource,
            long updatedAt
    );

    @Query("DELETE FROM prayer_records")
    void deleteAll();

    @Query(
            "SELECT * FROM prayer_records " +
                    "WHERE recordDate = :recordDate " +
                    "ORDER BY CASE prayerType " +
                    "WHEN 'FAJR' THEN 1 " +
                    "WHEN 'DHUHR' THEN 2 " +
                    "WHEN 'ASR' THEN 3 " +
                    "WHEN 'MAGHRIB' THEN 4 " +
                    "WHEN 'ISHA' THEN 5 " +
                    "ELSE 6 END"
    )
    LiveData<List<PrayerRecord>> observeForDate(String recordDate);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAllIgnore(List<PrayerRecord> prayerRecords);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertIgnore(PrayerRecord prayerRecord);

    @Query(
            "UPDATE prayer_records " +
                    "SET scheduledAt = :scheduledAt, " +
                    "notificationId = :notificationId, " +
                    "updatedAt = :updatedAt " +
                    "WHERE recordDate = :recordDate " +
                    "AND prayerType = :prayerType"
    )
    void updateReminderMetadata(
            String recordDate,
            PrayerType prayerType,
            long scheduledAt,
            int notificationId,
            long updatedAt
    );

    @Query(
            "SELECT * FROM prayer_records " +
                    "WHERE recordDate BETWEEN :startDate AND :endDate " +
                    "ORDER BY recordDate ASC, " +
                    "CASE prayerType " +
                    "WHEN 'FAJR' THEN 1 " +
                    "WHEN 'DHUHR' THEN 2 " +
                    "WHEN 'ASR' THEN 3 " +
                    "WHEN 'MAGHRIB' THEN 4 " +
                    "WHEN 'ISHA' THEN 5 " +
                    "ELSE 6 END"
    )
    LiveData<List<PrayerRecord>> observeBetweenDates(
            String startDate,
            String endDate
    );

}
