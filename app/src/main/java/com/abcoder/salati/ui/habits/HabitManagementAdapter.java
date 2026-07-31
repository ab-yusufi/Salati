package com.abcoder.salati.ui.habits;

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
import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.databinding.ItemHabitManagementBinding;

public final class HabitManagementAdapter
        extends RecyclerView.Adapter<
        HabitManagementAdapter.HabitViewHolder> {

    public interface HabitActionListener {

        void onEnabledChanged(
                Habit habit,
                boolean enabled
        );

        void onEditClicked(Habit habit);

        void onDeleteClicked(Habit habit);
    }

    private final List<Habit> habits =
            new ArrayList<>();

    private final HabitActionListener listener;

    public HabitManagementAdapter(
            HabitActionListener listener
    ) {
        this.listener = listener;
    }

    public void submitList(List<Habit> newHabits) {
        habits.clear();

        if (newHabits != null) {
            habits.addAll(newHabits);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemHabitManagementBinding binding =
                ItemHabitManagementBinding.inflate(
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
        holder.bind(habits.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static final class HabitViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemHabitManagementBinding binding;

        HabitViewHolder(
                ItemHabitManagementBinding binding
        ) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(
                Habit habit,
                HabitActionListener listener
        ) {
            Context context =
                    binding.getRoot().getContext();

            binding.habitTitleText.setText(habit.title);

            binding.reminderTimeText.setText(
                    context.getString(
                            R.string.habit_reminder_format,
                            formatTime(
                                    context,
                                    habit.reminderHour,
                                    habit.reminderMinute
                            )
                    )
            );

            binding.enabledSwitch
                    .setOnCheckedChangeListener(null);

            binding.enabledSwitch.setChecked(
                    habit.enabled
            );

            binding.enabledSwitch
                    .setOnCheckedChangeListener(
                            (buttonView, checked) ->
                                    listener
                                            .onEnabledChanged(
                                                    habit,
                                                    checked
                                            )
                    );

            binding.editButton
                    .setOnClickListener(view ->
                            listener.onEditClicked(habit)
                    );

            binding.deleteButton
                    .setOnClickListener(view ->
                            listener.onDeleteClicked(habit)
                    );
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