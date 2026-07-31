package com.abcoder.salati.ui.reports;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.abcoder.salati.R;
import com.abcoder.salati.databinding.ItemHabitStatisticsBinding;

public final class HabitStatisticsAdapter
        extends RecyclerView.Adapter<
        HabitStatisticsAdapter.StatisticsViewHolder> {

    private final List<
            ReportsUiState.HabitBreakdownItem
            > items =
            new ArrayList<>();

    public void submitList(
            List<ReportsUiState.HabitBreakdownItem>
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
        ItemHabitStatisticsBinding binding =
                ItemHabitStatisticsBinding.inflate(
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

        private final ItemHabitStatisticsBinding binding;

        StatisticsViewHolder(
                ItemHabitStatisticsBinding binding
        ) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(
                ReportsUiState.HabitBreakdownItem item
        ) {
            binding.habitTitleText.setText(
                    item.title
            );

            binding.statisticsText.setText(
                    binding.getRoot()
                            .getContext()
                            .getString(
                                    R.string
                                            .habit_breakdown_format,
                                    item.trackedDays,
                                    item.completed,
                                    item.notCompleted,
                                    item.pending,
                                    item.completionPercentage
                            )
            );
        }
    }
}