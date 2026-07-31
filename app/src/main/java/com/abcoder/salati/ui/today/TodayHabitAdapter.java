package com.abcoder.salati.ui.today;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.abcoder.salati.R;
import com.abcoder.salati.data.model.HabitStatus;
import com.abcoder.salati.databinding.ItemHabitTodayBinding;

public final class TodayHabitAdapter
        extends RecyclerView.Adapter<
        TodayHabitAdapter.HabitViewHolder> {

    public interface HabitStatusListener {

        void onStatusSelected(
                long habitId,
                HabitStatus status
        );
    }

    private final List<HabitTodayItem> items =
            new ArrayList<>();

    private final HabitStatusListener listener;

    public TodayHabitAdapter(
            HabitStatusListener listener
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
        holder.bind(items.get(position), listener);
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
                HabitStatusListener listener
        ) {
            Context context =
                    binding.getRoot().getContext();

            binding.habitTitleText.setText(
                    item.habit.title
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

            binding.habitStatusText.setText(
                    context.getString(
                            R.string.habit_status_format,
                            getStatusName(
                                    context,
                                    item.status
                            )
                    )
            );

            binding.completedButton.setEnabled(
                    item.status
                            != HabitStatus.COMPLETED
            );

            binding.notCompletedButton.setEnabled(
                    item.status
                            != HabitStatus.NOT_COMPLETED
            );

            binding.clearButton.setVisibility(
                    item.status == HabitStatus.PENDING
                            ? View.GONE
                            : View.VISIBLE
            );

            binding.completedButton
                    .setOnClickListener(view ->
                            listener.onStatusSelected(
                                    item.habit.id,
                                    HabitStatus.COMPLETED
                            )
                    );

            binding.notCompletedButton
                    .setOnClickListener(view ->
                            listener.onStatusSelected(
                                    item.habit.id,
                                    HabitStatus.NOT_COMPLETED
                            )
                    );

            binding.clearButton
                    .setOnClickListener(view ->
                            listener.onStatusSelected(
                                    item.habit.id,
                                    HabitStatus.PENDING
                            )
                    );
        }

        private static String getStatusName(
                Context context,
                HabitStatus status
        ) {
            switch (status) {
                case PENDING:
                    return context.getString(
                            R.string.habit_status_pending
                    );

                case COMPLETED:
                    return context.getString(
                            R.string.habit_status_completed
                    );

                case NOT_COMPLETED:
                    return context.getString(
                            R.string
                                    .habit_status_not_completed
                    );

                default:
                    throw new IllegalArgumentException(
                            "Unknown habit status: " + status
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