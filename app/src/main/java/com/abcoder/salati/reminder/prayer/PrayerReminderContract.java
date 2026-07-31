package com.abcoder.salati.reminder.prayer;

public final class PrayerReminderContract {

    public static final String CHANNEL_ID =
            "prayer_reminders";

    public static final String ACTION_SHOW_REMINDER =
            "com.abcoder.salati.action.SHOW_PRAYER_REMINDER";

    public static final String ACTION_RECORD_PRAYER =
            "com.abcoder.salati.action.RECORD_PRAYER";

    public static final String EXTRA_RECORD_DATE =
            "extra_record_date";

    public static final String EXTRA_PRAYER_TYPE =
            "extra_prayer_type";

    public static final String EXTRA_PRAYER_STATUS =
            "extra_prayer_status";

    public static final String EXTRA_NOTIFICATION_ID =
            "extra_notification_id";

    public static final String EXTRA_TRIGGER_AT =
            "extra_trigger_at";

    public static final int PRAYER_ALARM_REQUEST_CODE_BASE =
            53000;

    private PrayerReminderContract() {
        // Prevent instantiation.
    }
}