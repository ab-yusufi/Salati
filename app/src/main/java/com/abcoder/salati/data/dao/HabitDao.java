package com.abcoder.salati.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.abcoder.salati.data.entity.Habit;

@Dao
public interface HabitDao {

    @Insert
    long insert(Habit habit);

    @Update
    int update(Habit habit);

    @Delete
    int delete(Habit habit);

    @Query(
            "SELECT * FROM habits " +
                    "WHERE id = :habitId " +
                    "LIMIT 1"
    )
    Habit getById(long habitId);

    @Query(
            "SELECT * FROM habits " +
                    "ORDER BY createdAt ASC"
    )
    List<Habit> getAll();

    @Query(
            "SELECT * FROM habits " +
                    "ORDER BY createdAt ASC"
    )
    LiveData<List<Habit>> observeAll();

    @Query(
            "SELECT * FROM habits " +
                    "WHERE enabled = 1 " +
                    "ORDER BY reminderHour ASC, reminderMinute ASC"
    )
    List<Habit> getEnabled();

    @Query(
            "SELECT * FROM habits " +
                    "WHERE enabled = 1 " +
                    "ORDER BY reminderHour ASC, reminderMinute ASC"
    )
    LiveData<List<Habit>> observeEnabled();

    @Query(
            "SELECT COUNT(*) FROM habits " +
                    "WHERE enabled = 1"
    )
    int countEnabled();

    @Query(
            "DELETE FROM habits " +
                    "WHERE id = :habitId"
    )
    int deleteById(long habitId);

    @Query("DELETE FROM habits")
    void deleteAll();
}