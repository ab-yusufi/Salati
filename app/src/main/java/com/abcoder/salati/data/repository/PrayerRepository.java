package com.abcoder.salati.data.repository;

import androidx.lifecycle.LiveData;
import androidx.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.abcoder.salati.data.dao.PrayerRecordDao;
import com.abcoder.salati.data.dao.PrayerReminderSettingDao;
import com.abcoder.salati.data.database.AppDatabase;
import com.abcoder.salati.data.entity.PrayerRecord;
import com.abcoder.salati.data.entity.PrayerReminderSetting;
import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;

public final class PrayerRepository {

    private final PrayerRecordDao prayerRecordDao;
    private final PrayerReminderSettingDao prayerReminderSettingDao;
    private final ExecutorService databaseExecutor;

    public PrayerRepository(AppDatabase database) {
        prayerRecordDao = database.prayerRecordDao();
        prayerReminderSettingDao =
                database.prayerReminderSettingDao();

        databaseExecutor = AppDatabase.databaseExecutor;
    }

    public LiveData<List<PrayerRecord>> observeForDate(
            String recordDate
    ) {
        return prayerRecordDao.observeForDate(recordDate);
    }

    /**
     * Inserts default reminder settings only when a prayer does
     * not already have a settings row.
     *
     * These are suggested check-in times, not calculated prayer
     * beginning or ending times.
     */
    public void initializeDefaultReminderSettings() {
        databaseExecutor.execute(
                this::initializeDefaultReminderSettingsBlocking
        );
    }

    @WorkerThread
    public void initializeDefaultReminderSettingsBlocking() {
        prayerReminderSettingDao.insertAllIgnore(
                createDefaultReminderSettings()
        );
    }

    private List<PrayerReminderSetting>
    createDefaultReminderSettings() {
        return Arrays.asList(
                new PrayerReminderSetting(
                        PrayerType.FAJR,
                        7,
                        0,
                        true
                ),
                new PrayerReminderSetting(
                        PrayerType.DHUHR,
                        14,
                        0,
                        true
                ),
                new PrayerReminderSetting(
                        PrayerType.ASR,
                        18,
                        0,
                        true
                ),
                new PrayerReminderSetting(
                        PrayerType.MAGHRIB,
                        20,
                        0,
                        true
                ),
                new PrayerReminderSetting(
                        PrayerType.ISHA,
                        22,
                        0,
                        true
                )
        );
    }

    /**
     * Ensures all five prayers exist for the supplied date.
     *
     * Existing prayer records are not overwritten.
     */
    public void ensurePrayerRecordsForDate(String recordDate) {
        databaseExecutor.execute(() -> {
            long currentTime = System.currentTimeMillis();

            List<PrayerRecord> defaultRecords =
                    new ArrayList<>();

            for (PrayerType prayerType : PrayerType.values()) {
                PrayerRecord prayerRecord =
                        new PrayerRecord(
                                recordDate,
                                prayerType,
                                PrayerStatus.UNRECORDED,
                                0L,
                                null,
                                null,
                                0,
                                currentTime,
                                currentTime
                        );

                defaultRecords.add(prayerRecord);
            }

            prayerRecordDao.insertAllIgnore(defaultRecords);
        });
    }

    public void setPrayerStatus(
            String recordDate,
            PrayerType prayerType,
            PrayerStatus prayerStatus,
            AnswerSource answerSource
    ) {
        databaseExecutor.execute(() ->
                setPrayerStatusBlocking(
                        recordDate,
                        prayerType,
                        prayerStatus,
                        answerSource
                )
        );
    }

    /**
     * Must only be called from a background thread.
     *
     * This version is used by notification action receivers so that
     * the receiver can wait until the database operation is complete.
     */
    @WorkerThread
    public void setPrayerStatusBlocking(
            String recordDate,
            PrayerType prayerType,
            PrayerStatus prayerStatus,
            AnswerSource answerSource
    ) {
        long currentTime = System.currentTimeMillis();

        Long answeredAt;
        AnswerSource storedAnswerSource;

        if (prayerStatus == PrayerStatus.UNRECORDED) {
            answeredAt = null;
            storedAnswerSource = null;
        } else {
            answeredAt = currentTime;
            storedAnswerSource = answerSource;
        }

        int updatedRows = prayerRecordDao.updateStatus(
                recordDate,
                prayerType,
                prayerStatus,
                answeredAt,
                storedAnswerSource,
                currentTime
        );

        /*
         * This fallback creates the record when it does not already
         * exist, such as when a reminder fires before Today is opened.
         */
        if (updatedRows == 0) {
            PrayerRecord newRecord = new PrayerRecord(
                    recordDate,
                    prayerType,
                    prayerStatus,
                    0L,
                    answeredAt,
                    storedAnswerSource,
                    0,
                    currentTime,
                    currentTime
            );

            prayerRecordDao.upsert(newRecord);
        }
    }

    public void upsert(PrayerRecord prayerRecord) {
        databaseExecutor.execute(() ->
                prayerRecordDao.upsert(prayerRecord)
        );
    }

    public void deleteAll() {
        databaseExecutor.execute(
                prayerRecordDao::deleteAll
        );
    }

    public LiveData<List<PrayerReminderSetting>>
    observeReminderSettings() {
        return prayerReminderSettingDao.observeAll();
    }

    public void saveReminderSetting(
            PrayerReminderSetting setting
    ) {
        databaseExecutor.execute(() ->
                prayerReminderSettingDao.upsert(setting)
        );
    }
    @WorkerThread
    public List<PrayerReminderSetting>
    getReminderSettingsBlocking() {
        return prayerReminderSettingDao.getAll();
    }

    @WorkerThread
    public PrayerReminderSetting
    getReminderSettingBlocking(PrayerType prayerType) {
        return prayerReminderSettingDao.getByPrayerType(
                prayerType
        );
    }

    @WorkerThread
    public void ensurePrayerRecordBlocking(
            String recordDate,
            PrayerType prayerType,
            long scheduledAt,
            int notificationId
    ) {
        long currentTime = System.currentTimeMillis();

        PrayerRecord prayerRecord = new PrayerRecord(
                recordDate,
                prayerType,
                PrayerStatus.UNRECORDED,
                scheduledAt,
                null,
                null,
                notificationId,
                currentTime,
                currentTime
        );

        /*
         * Insert only when this prayer does not already exist.
         */
        prayerRecordDao.insertIgnore(prayerRecord);

        /*
         * Update scheduling details without modifying the
         * user's current prayer status.
         */
        prayerRecordDao.updateReminderMetadata(
                recordDate,
                prayerType,
                scheduledAt,
                notificationId,
                currentTime
        );
    }

    public LiveData<List<PrayerRecord>>
    observeBetweenDates(
            String startDate,
            String endDate
    ) {
        return prayerRecordDao.observeBetweenDates(
                startDate,
                endDate
        );
    }
}