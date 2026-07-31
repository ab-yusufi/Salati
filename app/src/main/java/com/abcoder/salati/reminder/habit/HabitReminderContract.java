package com.abcoder.salati.reminder.habit;

public final class HabitReminderContract {

    public static final String CHANNEL_ID =
            "habit_reminders";

    public static final String ACTION_SHOW_HABIT =
            "com.abcoder.salati.action.SHOW_HABIT";

    public static final String ACTION_RECORD_HABIT =
            "com.abcoder.salati.action.RECORD_HABIT";

    public static final String ACTION_SNOOZE_HABIT =
            "com.abcoder.salati.action.SNOOZE_HABIT";

    public static final String EXTRA_HABIT_ID =
            "extra_habit_id";

    public static final String EXTRA_RECORD_DATE =
            "extra_record_date";

    public static final String EXTRA_HABIT_STATUS =
            "extra_habit_status";

    public static final String EXTRA_NOTIFICATION_ID =
            "extra_notification_id";

    public static final String EXTRA_TRIGGER_AT =
            "extra_trigger_at";

    public static final String EXTRA_IS_SNOOZE =
            "extra_is_snooze";

    private HabitReminderContract() {
    }
}