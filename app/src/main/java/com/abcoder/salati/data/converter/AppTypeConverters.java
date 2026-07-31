package com.abcoder.salati.data.converter;

import androidx.room.TypeConverter;

import com.abcoder.salati.data.model.AnswerSource;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
public final class AppTypeConverters {
    private AppTypeConverters() {
        // Prevent this utility class from being instantiated.
    }

    @TypeConverter
    public static String fromPrayerType(PrayerType value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static PrayerType toPrayerType(String value) {
        return value == null ? null : PrayerType.valueOf(value);
    }

    @TypeConverter
    public static String fromPrayerStatus(PrayerStatus value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static PrayerStatus toPrayerStatus(String value) {
        return value == null ? null : PrayerStatus.valueOf(value);
    }

    @TypeConverter
    public static String fromHabitStatus(HabitStatus value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static HabitStatus toHabitStatus(String value) {
        return value == null ? null : HabitStatus.valueOf(value);
    }

    @TypeConverter
    public static String fromAnswerSource(AnswerSource value) {
        return value == null ? null : value.name();
    }

    @TypeConverter
    public static AnswerSource toAnswerSource(String value) {
        return value == null ? null : AnswerSource.valueOf(value);
    }

}
