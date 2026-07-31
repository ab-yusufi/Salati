package com.abcoder.salati.data.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import androidx.lifecycle.LiveData;

import java.util.List;

import com.abcoder.salati.data.entity.PrayerReminderSetting;
import com.abcoder.salati.data.model.PrayerType;

@Dao
public interface PrayerReminderSettingDao {
    @Upsert
    void upsert(PrayerReminderSetting setting);

    @Upsert
    void upsertAll(List<PrayerReminderSetting> settings);

    @Query(
            "SELECT * FROM prayer_reminder_settings " +
                    "WHERE prayerType = :prayerType " +
                    "LIMIT 1"
    )
    PrayerReminderSetting getByPrayerType(PrayerType prayerType);

    @Query(
            "SELECT * FROM prayer_reminder_settings " +
                    "ORDER BY CASE prayerType " +
                    "WHEN 'FAJR' THEN 1 " +
                    "WHEN 'DHUHR' THEN 2 " +
                    "WHEN 'ASR' THEN 3 " +
                    "WHEN 'MAGHRIB' THEN 4 " +
                    "WHEN 'ISHA' THEN 5 " +
                    "ELSE 6 END"
    )
    List<PrayerReminderSetting> getAll();

    @Query(
            "SELECT * FROM prayer_reminder_settings " +
                    "WHERE enabled = 1"
    )
    List<PrayerReminderSetting> getEnabled();

    @Query("DELETE FROM prayer_reminder_settings")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAllIgnore(List<PrayerReminderSetting> settings);

    @Query(
            "SELECT * FROM prayer_reminder_settings " +
                    "ORDER BY CASE prayerType " +
                    "WHEN 'FAJR' THEN 1 " +
                    "WHEN 'DHUHR' THEN 2 " +
                    "WHEN 'ASR' THEN 3 " +
                    "WHEN 'MAGHRIB' THEN 4 " +
                    "WHEN 'ISHA' THEN 5 " +
                    "ELSE 6 END"
    )
    LiveData<List<PrayerReminderSetting>> observeAll();
}
