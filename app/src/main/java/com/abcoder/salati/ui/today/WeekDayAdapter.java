package com.abcoder.salati.ui.today;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.abcoder.salati.R;
import com.abcoder.salati.databinding.ItemWeekDayBinding;
import com.google.android.material.color.MaterialColors;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WeekDayAdapter
        extends RecyclerView.Adapter<
        WeekDayAdapter.WeekDayViewHolder> {

    public interface OnDateSelectedListener {

        void onDateSelected(LocalDate date);
    }

    private final List<WeekDayItem> items =
            new ArrayList<>();

    private final OnDateSelectedListener listener;

    public WeekDayAdapter(
            OnDateSelectedListener listener
    ) {
        this.listener = listener;
    }

    public void submitList(
            List<WeekDayItem> newItems
    ) {
        items.clear();

        if (newItems != null) {
            items.addAll(newItems);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeekDayViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemWeekDayBinding binding =
                ItemWeekDayBinding.inflate(
                        LayoutInflater.from(
                                parent.getContext()
                        ),
                        parent,
                        false
                );

        return new WeekDayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull WeekDayViewHolder holder,
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

    static final class WeekDayViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemWeekDayBinding binding;

        WeekDayViewHolder(
                ItemWeekDayBinding binding
        ) {
            super(binding.getRoot());

            this.binding = binding;
        }

        void bind(
                WeekDayItem item,
                OnDateSelectedListener listener
        ) {
            LocalDate date = item.getDate();

            DateTimeFormatter dayFormatter =
                    DateTimeFormatter.ofPattern(
                            "EEE",
                            Locale.getDefault()
                    );

            binding.weekDayNameText.setText(
                    date.format(dayFormatter)
            );

            binding.weekDayNumberText.setText(
                    String.valueOf(
                            date.getDayOfMonth()
                    )
            );

            applyAppearance(item.isToday());

            String contentDescription;

            if (item.isToday()) {
                contentDescription =
                        binding.getRoot()
                                .getContext()
                                .getString(
                                        R.string
                                                .week_day_today_content_description,
                                        date.format(
                                                DateTimeFormatter
                                                        .ofPattern(
                                                                "EEEE, d MMMM",
                                                                Locale.getDefault()
                                                        )
                                        )
                                );

            } else {
                contentDescription =
                        binding.getRoot()
                                .getContext()
                                .getString(
                                        R.string
                                                .week_day_content_description,
                                        date.format(
                                                DateTimeFormatter
                                                        .ofPattern(
                                                                "EEEE, d MMMM",
                                                                Locale.getDefault()
                                                        )
                                        )
                                );
            }

            binding.getRoot().setContentDescription(
                    contentDescription
            );

            binding.getRoot().setOnClickListener(
                    view -> listener.onDateSelected(
                            date
                    )
            );
        }

        private void applyAppearance(
                boolean today
        ) {
            int cardColor;
            int textColor;
            int strokeColor;

            if (today) {
                cardColor =
                        MaterialColors.getColor(
                                binding.getRoot(),
                                com.google.android.material
                                        .R.attr
                                        .colorPrimaryContainer,
                                Color.TRANSPARENT
                        );

                textColor =
                        MaterialColors.getColor(
                                binding.getRoot(),
                                com.google.android.material
                                        .R.attr
                                        .colorOnPrimaryContainer,
                                Color.BLACK
                        );

                strokeColor =
                        MaterialColors.getColor(
                                binding.getRoot(),
                                com.google.android.material
                                        .R.attr
                                        .colorPrimary,
                                Color.TRANSPARENT
                        );

                binding.weekDayCard.setStrokeWidth(
                        dpToPixels(2)
                );

            } else {
                cardColor =
                        MaterialColors.getColor(
                                binding.getRoot(),
                                com.google.android.material
                                        .R.attr
                                        .colorSurface,
                                Color.TRANSPARENT
                        );

                textColor =
                        MaterialColors.getColor(
                                binding.getRoot(),
                                com.google.android.material
                                        .R.attr
                                        .colorOnSurface,
                                Color.BLACK
                        );

                strokeColor =
                        MaterialColors.getColor(
                                binding.getRoot(),
                                com.google.android.material
                                        .R.attr
                                        .colorOutline,
                                Color.TRANSPARENT
                        );

                binding.weekDayCard.setStrokeWidth(
                        dpToPixels(1)
                );
            }

            binding.weekDayCard.setCardBackgroundColor(
                    cardColor
            );

            binding.weekDayCard.setStrokeColor(
                    strokeColor
            );

            binding.weekDayNameText.setTextColor(
                    textColor
            );

            binding.weekDayNumberText.setTextColor(
                    textColor
            );
        }

        private int dpToPixels(
                int dp
        ) {
            float density =
                    binding.getRoot()
                            .getResources()
                            .getDisplayMetrics()
                            .density;

            return Math.round(dp * density);
        }
    }
}