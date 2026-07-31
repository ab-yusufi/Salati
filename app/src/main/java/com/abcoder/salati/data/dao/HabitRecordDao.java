package com.abcoder.salati.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import com.abcoder.salati.data.entity.HabitRecord;
import com.abcoder.salati.data.entity.PrayerRecord;
import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.HabitStatus;

@Dao
public interface HabitRecordDao {

    @Upsert
    void upsert(HabitRecord habitRecord);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertIgnore(HabitRecord habitRecord);

    @Query(
            "SELECT * FROM habit_records " +
                    "WHERE habitId = :habitId " +
                    "AND recordDate = :recordDate " +
                    "LIMIT 1"
    )
    HabitRecord getByHabitAndDate(
            long habitId,
            String recordDate
    );

    @Query(
            "SELECT * FROM habit_records " +
                    "WHERE recordDate = :recordDate"
    )
    List<HabitRecord> getForDate(String recordDate);

    @Query(
            "SELECT * FROM habit_records " +
                    "WHERE recordDate = :recordDate"
    )
    LiveData<List<HabitRecord>> observeForDate(
            String recordDate
    );

    @Query(
            "SELECT * FROM habit_records " +
                    "WHERE recordDate BETWEEN :startDate AND :endDate " +
                    "ORDER BY recordDate ASC"
    )
    List<HabitRecord> getBetweenDates(
            String startDate,
            String endDate
    );

    @Query(
            "UPDATE habit_records " +
                    "SET status = :status, " +
                    "answeredAt = :answeredAt, " +
                    "answerSource = :answerSource, " +
                    "snoozeCount = CASE " +
                    "WHEN :status = 'PENDING' THEN 0 " +
                    "ELSE snoozeCount END, " +
                    "updatedAt = :updatedAt " +
                    "WHERE habitId = :habitId " +
                    "AND recordDate = :recordDate"
    )
    int updateStatus(
            long habitId,
            String recordDate,
            HabitStatus status,
            Long answeredAt,
            AnswerSource answerSource,
            long updatedAt
    );

    @Query(
            "UPDATE habit_records " +
                    "SET snoozeCount = snoozeCount + 1, " +
                    "updatedAt = :updatedAt " +
                    "WHERE habitId = :habitId " +
                    "AND recordDate = :recordDate " +
                    "AND status = 'PENDING' " +
                    "AND snoozeCount < :maximumSnoozes"
    )
    int incrementSnoozeCountIfAllowed(
            long habitId,
            String recordDate,
            int maximumSnoozes,
            long updatedAt
    );

    @Query(
            "DELETE FROM habit_records " +
                    "WHERE habitId = :habitId"
    )
    void deleteForHabit(long habitId);

    @Query("DELETE FROM habit_records")
    void deleteAll();

    @Query(
            "SELECT * FROM habit_records " +
                    "WHERE recordDate BETWEEN :startDate AND :endDate " +
                    "ORDER BY recordDate ASC, habitId ASC"
    )
    LiveData<List<HabitRecord>> observeBetweenDates(
            String startDate,
            String endDate
    );
}