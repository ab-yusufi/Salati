package com.abcoder.salati.ui.today;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.abcoder.salati.R;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.databinding.ItemHabitTodayBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class TodayHabitAdapter
        extends RecyclerView.Adapter<
        TodayHabitAdapter.HabitViewHolder> {

    public interface HabitActionListener {

        void onHabitActionRequested(
                long habitId,
                String habitTitle,
                HabitStatus currentStatus
        );
    }

    private final List<HabitTodayItem> items =
            new ArrayList<>();

    private final HabitActionListener listener;

    public TodayHabitAdapter(
            HabitActionListener listener
    ) {
        this.listener = listener;
    }

    public void submitList(
            List<HabitTodayItem> newItems
    ) {
        items.clear();

        if (newItems != null) {
            items.addAll(newItems);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemHabitTodayBinding binding =
                ItemHabitTodayBinding.inflate(
                        LayoutInflater.from(
                                parent.getContext()
                        ),
                        parent,
                        false
                );

        return new HabitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull HabitViewHolder holder,
            int position
    ) {
        holder.bind(
                items.get(position),
                listener
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class HabitViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemHabitTodayBinding binding;

        HabitViewHolder(
                ItemHabitTodayBinding binding
        ) {
            super(binding.getRoot());

            this.binding = binding;
        }

        void bind(
                HabitTodayItem item,
                HabitActionListener listener
        ) {
            Context context =
                    binding.getRoot().getContext();

            String habitTitle =
                    item.habit.title;

            String statusName =
                    getStatusName(
                            context,
                            item.status
                    );

            binding.habitTitleText.setText(
                    habitTitle
            );

            binding.habitReminderText.setText(
                    context.getString(
                            R.string.habit_reminder_format,
                            formatTime(
                                    context,
                                    item.habit.reminderHour,
                                    item.habit.reminderMinute
                            )
                    )
            );

            binding.habitStatusChip.setText(
                    statusName
            );

            applyStatusColor(
                    context,
                    item.status
            );

            boolean recorded =
                    item.status
                            != HabitStatus.PENDING;

            binding.lockedStatusText.setVisibility(
                    recorded
                            ? View.VISIBLE
                            : View.GONE
            );

            binding.logHabitButton.setVisibility(
                    recorded
                            ? View.GONE
                            : View.VISIBLE
            );

            binding.editHabitButton.setVisibility(
                    recorded
                            ? View.VISIBLE
                            : View.GONE
            );

            if (item.snoozeCount > 0) {
                binding.habitSnoozeText.setVisibility(
                        View.VISIBLE
                );

                binding.habitSnoozeText.setText(
                        context.getString(
                                R.string
                                        .habit_snooze_count_format,
                                item.snoozeCount,
                                3
                        )
                );

            } else {
                binding.habitSnoozeText.setVisibility(
                        View.GONE
                );
            }

            View.OnClickListener actionListener =
                    view ->
                            listener
                                    .onHabitActionRequested(
                                            item.habit.id,
                                            habitTitle,
                                            item.status
                                    );

            binding.logHabitButton.setOnClickListener(
                    actionListener
            );

            binding.editHabitButton.setOnClickListener(
                    actionListener
            );

            binding.logHabitButton
                    .setContentDescription(
                            context.getString(
                                    R.string
                                            .log_habit_content_description,
                                    habitTitle
                            )
                    );

            binding.editHabitButton
                    .setContentDescription(
                            context.getString(
                                    R.string
                                            .edit_habit_content_description,
                                    habitTitle,
                                    statusName
                            )
                    );
        }

        private void applyStatusColor(
                Context context,
                HabitStatus status
        ) {
            int statusColor =
                    ContextCompat.getColor(
                            context,
                            getStatusColorResource(
                                    status
                            )
                    );

            ColorStateList colorStateList =
                    ColorStateList.valueOf(
                            statusColor
                    );

            binding.habitStatusChip.setTextColor(
                    colorStateList
            );

            binding.habitStatusChip.setChipStrokeColor(
                    colorStateList
            );
        }

        @ColorRes
        private static int getStatusColorResource(
                HabitStatus status
        ) {
            switch (status) {
                case COMPLETED:
                    return R.color
                            .habit_status_completed;

                case NOT_COMPLETED:
                    return R.color
                            .habit_status_not_completed;

                case PENDING:
                default:
                    return R.color
                            .habit_status_pending;
            }
        }

        private static String getStatusName(
                Context context,
                HabitStatus status
        ) {
            switch (status) {
                case COMPLETED:
                    return context.getString(
                            R.string
                                    .habit_status_completed
                    );

                case NOT_COMPLETED:
                    return context.getString(
                            R.string
                                    .habit_status_not_completed
                    );

                case PENDING:
                default:
                    return context.getString(
                            R.string
                                    .habit_status_pending
                    );
            }
        }

        private static String formatTime(
                Context context,
                int hour,
                int minute
        ) {
            Calendar calendar =
                    Calendar.getInstance();

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
    }
}