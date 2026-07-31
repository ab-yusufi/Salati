package com.abcoder.salati.ui.reports;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.abcoder.salati.R;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.databinding.ItemPrayerStatisticsBinding;

public final class PrayerStatisticsAdapter
        extends RecyclerView.Adapter<
        PrayerStatisticsAdapter.StatisticsViewHolder> {

    private final List<
            ReportsUiState.PrayerBreakdownItem
            > items =
            new ArrayList<>();

    public void submitList(
            List<ReportsUiState.PrayerBreakdownItem>
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
        ItemPrayerStatisticsBinding binding =
                ItemPrayerStatisticsBinding.inflate(
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

        private final ItemPrayerStatisticsBinding binding;

        StatisticsViewHolder(
                ItemPrayerStatisticsBinding binding
        ) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(
                ReportsUiState.PrayerBreakdownItem item
        ) {
            Context context =
                    binding.getRoot().getContext();

            binding.prayerNameText.setText(
                    getPrayerName(
                            context,
                            item.prayerType
                    )
            );

            binding.statisticsText.setText(
                    context.getString(
                            R.string
                                    .prayer_breakdown_format,
                            item.onTime,
                            item.late,
                            item.missed,
                            item.unrecorded
                    )
            );
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
                            "Unknown prayer type"
                    );
            }
        }
    }
}