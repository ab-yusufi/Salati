package com.abcoder.salati.ui.calendar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.abcoder.salati.R;
import com.abcoder.salati.databinding.ItemCalendarDayBinding;
import com.google.android.material.color.MaterialColors;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CalendarDayAdapter
        extends RecyclerView.Adapter<
        CalendarDayAdapter.DayViewHolder> {

    public interface OnDateSelectedListener {

        void onDateSelected(LocalDate date);
    }

    private final List<CalendarDayItem> items =
            new ArrayList<>();

    private final OnDateSelectedListener listener;

    public CalendarDayAdapter(
            OnDateSelectedListener listener
    ) {
        this.listener = listener;
    }

    public void submitList(
            List<CalendarDayItem> newItems
    ) {
        items.clear();

        if (newItems != null) {
            items.addAll(newItems);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        ItemCalendarDayBinding binding =
                ItemCalendarDayBinding.inflate(
                        LayoutInflater.from(
                                parent.getContext()
                        ),
                        parent,
                        false
                );

        return new DayViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull DayViewHolder holder,
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

    static final class DayViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemCalendarDayBinding binding;

        DayViewHolder(
                ItemCalendarDayBinding binding
        ) {
            super(binding.getRoot());

            this.binding = binding;
        }

        void bind(
                CalendarDayItem item,
                OnDateSelectedListener listener
        ) {
            if (!item.inDisplayedMonth) {
                showEmptyCell();
                return;
            }

            Context context =
                    binding.getRoot().getContext();

            binding.dayContent.setVisibility(
                    View.VISIBLE
            );

            binding.dayNumberText.setText(
                    String.valueOf(
                            item.date.getDayOfMonth()
                    )
            );

            binding.dayCard.setEnabled(
                    !item.future
            );

            binding.dayCard.setClickable(
                    !item.future
            );

            binding.dayCard.setAlpha(
                    item.future
                            ? 0.38f
                            : 1f
            );

            applyCardAppearance(item);
            applyIndicators(context, item);
            applyContentDescription(context, item);

            if (item.future) {
                binding.dayCard.setOnClickListener(
                        null
                );

            } else {
                binding.dayCard.setOnClickListener(
                        view -> listener.onDateSelected(
                                item.date
                        )
                );
            }
        }

        private void showEmptyCell() {
            binding.dayContent.setVisibility(
                    View.INVISIBLE
            );

            binding.dayCard.setEnabled(false);
            binding.dayCard.setClickable(false);
            binding.dayCard.setOnClickListener(null);
            binding.dayCard.setAlpha(1f);

            binding.dayCard.setCardBackgroundColor(
                    Color.TRANSPARENT
            );

            binding.dayCard.setStrokeWidth(0);
        }

        private void applyCardAppearance(
                CalendarDayItem item
        ) {
            int backgroundColor;
            int textColor;
            int strokeColor;
            int strokeWidth;

            if (item.selected) {
                backgroundColor =
                        getThemeColor(
                                com.google.android.material
                                        .R.attr
                                        .colorPrimaryContainer
                        );

                textColor =
                        getThemeColor(
                                com.google.android.material
                                        .R.attr
                                        .colorOnPrimaryContainer
                        );

                strokeColor =
                        getThemeColor(
                                com.google.android.material
                                        .R.attr
                                        .colorPrimary
                        );

                strokeWidth = dpToPixels(2);

            } else {
                backgroundColor =
                        getThemeColor(
                                com.google.android.material
                                        .R.attr
                                        .colorSurface
                        );

                textColor =
                        getThemeColor(
                                com.google.android.material
                                        .R.attr
                                        .colorOnSurface
                        );

                if (item.today) {
                    strokeColor =
                            getThemeColor(
                                    com.google.android.material
                                            .R.attr
                                            .colorPrimary
                            );

                    strokeWidth = dpToPixels(2);

                } else {
                    strokeColor =
                            getThemeColor(
                                    com.google.android.material
                                            .R.attr
                                            .colorOutline
                            );

                    strokeWidth = dpToPixels(1);
                }
            }

            binding.dayCard.setCardBackgroundColor(
                    backgroundColor
            );

            binding.dayCard.setStrokeColor(
                    strokeColor
            );

            binding.dayCard.setStrokeWidth(
                    strokeWidth
            );

            binding.dayNumberText.setTextColor(
                    textColor
            );
        }

        private void applyIndicators(
                Context context,
                CalendarDayItem item
        ) {
            if (item.hasPrayerRecords) {
                binding.prayerIndicator.setVisibility(
                        View.VISIBLE
                );

                binding.prayerIndicator
                        .setBackgroundTintList(
                                ColorStateList.valueOf(
                                        ContextCompat.getColor(
                                                context,
                                                getPrayerIndicatorColor(
                                                        item.recordedPrayerCount
                                                )
                                        )
                                )
                        );

            } else {
                binding.prayerIndicator.setVisibility(
                        View.GONE
                );
            }

            if (item.habitRecordCount > 0) {
                binding.habitIndicator.setVisibility(
                        View.VISIBLE
                );

                binding.habitIndicator
                        .setBackgroundTintList(
                                ColorStateList.valueOf(
                                        ContextCompat.getColor(
                                                context,
                                                getHabitIndicatorColor(
                                                        item.completedHabitCount,
                                                        item.habitRecordCount
                                                )
                                        )
                                )
                        );

            } else {
                binding.habitIndicator.setVisibility(
                        View.GONE
                );
            }
        }

        @ColorRes
        private static int getPrayerIndicatorColor(
                int recordedCount
        ) {
            if (recordedCount >= 5) {
                return R.color
                        .prayer_status_on_time;
            }

            if (recordedCount > 0) {
                return R.color.salati_primary;
            }

            return R.color
                    .prayer_status_unrecorded;
        }

        @ColorRes
        private static int getHabitIndicatorColor(
                int completedCount,
                int totalCount
        ) {
            if (completedCount >= totalCount) {
                return R.color
                        .habit_status_completed;
            }

            if (completedCount > 0) {
                return R.color.salati_secondary;
            }

            return R.color
                    .habit_status_pending;
        }

        private void applyContentDescription(
                Context context,
                CalendarDayItem item
        ) {
            String formattedDate =
                    item.date.format(
                            DateTimeFormatter.ofPattern(
                                    "EEEE, d MMMM yyyy",
                                    Locale.getDefault()
                            )
                    );

            int stringResource;

            if (item.future) {
                stringResource =
                        R.string
                                .calendar_future_day_content_description;

            } else if (item.selected) {
                stringResource =
                        R.string
                                .calendar_selected_day_content_description;

            } else {
                stringResource =
                        R.string
                                .calendar_day_content_description;
            }

            binding.dayCard.setContentDescription(
                    context.getString(
                            stringResource,
                            formattedDate
                    )
            );
        }

        private int getThemeColor(
                int attribute
        ) {
            return MaterialColors.getColor(
                    binding.getRoot(),
                    attribute,
                    Color.TRANSPARENT
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