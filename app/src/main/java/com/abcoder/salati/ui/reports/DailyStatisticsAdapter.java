package com.abcoder.salati.ui.reports;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.abcoder.salati.R;
import com.abcoder.salati.databinding.ItemDailyStatisticsBinding;

public final class DailyStatisticsAdapter
        extends RecyclerView.Adapter<
        DailyStatisticsAdapter.StatisticsViewHolder> {

    private final List<
            ReportsUiState.DailyBreakdownItem
            > items =
            new ArrayList<>();

    public void submitList(
            List<ReportsUiState.DailyBreakdownItem>
                    newItems
    ) {
        items.clear();

        if (newItems != null) {
            items.addAll(newItems);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StatisticsViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemDailyStatisticsBinding binding =
                ItemDailyStatisticsBinding.inflate(
                        LayoutInflater.from(
                                parent.getContext()
                        ),
                        parent,
                        false
                );

        return new StatisticsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull StatisticsViewHolder holder,
            int position
    ) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static final class StatisticsViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemDailyStatisticsBinding binding;

        StatisticsViewHolder(
                ItemDailyStatisticsBinding binding
        ) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(
                ReportsUiState.DailyBreakdownItem item
        ) {
            binding.dateText.setText(
                    item.displayDate
            );

            binding.prayerStatisticsText.setText(
                    binding.getRoot()
                            .getContext()
                            .getString(
                                    R.string
                                            .daily_prayer_format,
                                    item.prayerOnTime,
                                    item.prayerLate,
                                    item.prayerMissed,
                                    item.prayerUnrecorded
                            )
            );

            binding.habitStatisticsText.setText(
                    binding.getRoot()
                            .getContext()
                            .getString(
                                    R.string
                                            .daily_habit_format,
                                    item.habitsCompleted,
                                    item.habitsNotCompleted,
                                    item.habitsPending
                            )
            );
        }
    }
}