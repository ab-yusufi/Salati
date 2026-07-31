package com.abcoder.salati.ui.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.abcoder.salati.R;
import com.abcoder.salati.data.entity.PrayerReminderSetting;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.databinding.ItemPrayerReminderBinding;

public final class PrayerReminderAdapter
        extends RecyclerView.Adapter<
        PrayerReminderAdapter.ReminderViewHolder> {

    public interface ReminderActionListener {

        void onEnabledChanged(
                PrayerReminderSetting setting,
                boolean enabled
        );

        void onTimeClicked(
                PrayerReminderSetting setting
        );
    }

    private final List<PrayerReminderSetting> settings =
            new ArrayList<>();

    private final ReminderActionListener listener;

    public PrayerReminderAdapter(
            ReminderActionListener listener
    ) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitList(
            List<PrayerReminderSetting> newSettings
    ) {
        settings.clear();

        if (newSettings != null) {
            settings.addAll(newSettings);
        }

        /*
         * There are only five rows, so a complete refresh is
         * acceptable here.
         */
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemPrayerReminderBinding binding =
                ItemPrayerReminderBinding.inflate(
                        LayoutInflater.from(
                                parent.getContext()
                        ),
                        parent,
                        false
                );

        return new ReminderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ReminderViewHolder holder,
            int position
    ) {
        holder.bind(settings.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    static final class ReminderViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemPrayerReminderBinding binding;

        ReminderViewHolder(
                ItemPrayerReminderBinding binding
        ) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(
                PrayerReminderSetting setting,
                ReminderActionListener listener
        ) {
            Context context =
                    binding.getRoot().getContext();

            binding.prayerNameText.setText(
                    getPrayerName(
                            context,
                            setting.prayerType
                    )
            );

            String formattedTime =
                    formatTime(
                            context,
                            setting.hour,
                            setting.minute
                    );

            binding.reminderTimeText.setText(
                    context.getString(
                            R.string.reminder_time_format,
                            formattedTime
                    )
            );

            /*
             * Remove the old listener first. RecyclerView reuses
             * row views, and setChecked() must not accidentally
             * trigger the previous row's listener.
             */
            binding.enabledSwitch
                    .setOnCheckedChangeListener(null);

            binding.enabledSwitch.setChecked(
                    setting.enabled
            );

            binding.enabledSwitch
                    .setOnCheckedChangeListener(
                            (buttonView, isChecked) ->
                                    listener.onEnabledChanged(
                                            setting,
                                            isChecked
                                    )
                    );

            binding.changeTimeButton
                    .setOnClickListener(view ->
                            listener.onTimeClicked(setting)
                    );
        }

        private static String formatTime(
                Context context,
                int hour,
                int minute
        ) {
            Calendar calendar = Calendar.getInstance();

            calendar.set(
                    Calendar.HOUR_OF_DAY,
                    hour
            );

            calendar.set(
                    Calendar.MINUTE,
                    minute
            );

            return DateFormat
                    .getTimeFormat(context)
                    .format(calendar.getTime());
        }

        private static String getPrayerName(
                Context context,
                PrayerType prayerType
        ) {
            switch (prayerType) {
                case FAJR:
                    return context.getString(
                            R.string.prayer_fajr
                    );

                case DHUHR:
                    return context.getString(
                            R.string.prayer_dhuhr
                    );

                case ASR:
                    return context.getString(
                            R.string.prayer_asr
                    );

                case MAGHRIB:
                    return context.getString(
                            R.string.prayer_maghrib
                    );

                case ISHA:
                    return context.getString(
                            R.string.prayer_isha
                    );

                default:
                    throw new IllegalArgumentException(
                            "Unknown prayer type: "
                                    + prayerType
                    );
            }
        }
    }
}