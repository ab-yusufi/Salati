package com.abcoder.salati.ui.today;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.abcoder.salati.R;
import com.abcoder.salati.data.entity.PrayerRecord;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.databinding.ItemPrayerBinding;

public final class PrayerListAdapter
        extends RecyclerView.Adapter<
        PrayerListAdapter.PrayerViewHolder> {

    public interface PrayerActionListener {

        void onPrayerActionRequested(
                PrayerType prayerType,
                PrayerStatus currentStatus
        );
    }

    private final List<PrayerRecord> prayerRecords =
            new ArrayList<>();

    private final PrayerActionListener listener;

    public PrayerListAdapter(
            PrayerActionListener listener
    ) {
        this.listener = listener;
    }

    public void submitList(
            List<PrayerRecord> newPrayerRecords
    ) {
        prayerRecords.clear();

        if (newPrayerRecords != null) {
            prayerRecords.addAll(newPrayerRecords);
        }

        /*
         * There are always only five prayer rows.
         */
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PrayerViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LayoutInflater inflater =
                LayoutInflater.from(
                        parent.getContext()
                );

        ItemPrayerBinding binding =
                ItemPrayerBinding.inflate(
                        inflater,
                        parent,
                        false
                );

        return new PrayerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PrayerViewHolder holder,
            int position
    ) {
        holder.bind(
                prayerRecords.get(position),
                listener
        );
    }

    @Override
    public int getItemCount() {
        return prayerRecords.size();
    }

    static final class PrayerViewHolder
            extends RecyclerView.ViewHolder {

        private final ItemPrayerBinding binding;

        PrayerViewHolder(
                ItemPrayerBinding binding
        ) {
            super(binding.getRoot());

            this.binding = binding;
        }

        void bind(
                PrayerRecord prayerRecord,
                PrayerActionListener listener
        ) {
            Context context =
                    binding.getRoot().getContext();

            String prayerName =
                    getPrayerName(
                            context,
                            prayerRecord.prayerType
                    );

            String statusName =
                    getStatusName(
                            context,
                            prayerRecord.status
                    );

            binding.prayerNameText.setText(
                    prayerName
            );

            binding.prayerStatusChip.setText(
                    statusName
            );

            applyStatusColor(
                    context,
                    prayerRecord.status
            );

            boolean recorded =
                    prayerRecord.status
                            != PrayerStatus.UNRECORDED;

            binding.lockedStatusText.setVisibility(
                    recorded
                            ? View.VISIBLE
                            : View.GONE
            );

            binding.logPrayerButton.setVisibility(
                    recorded
                            ? View.GONE
                            : View.VISIBLE
            );

            binding.editPrayerButton.setVisibility(
                    recorded
                            ? View.VISIBLE
                            : View.GONE
            );

            View.OnClickListener actionListener =
                    view ->
                            listener
                                    .onPrayerActionRequested(
                                            prayerRecord
                                                    .prayerType,
                                            prayerRecord
                                                    .status
                                    );

            binding.logPrayerButton
                    .setOnClickListener(
                            actionListener
                    );

            binding.editPrayerButton
                    .setOnClickListener(
                            actionListener
                    );

            binding.logPrayerButton
                    .setContentDescription(
                            context.getString(
                                    R.string
                                            .log_prayer_content_description,
                                    prayerName
                            )
                    );

            binding.editPrayerButton
                    .setContentDescription(
                            context.getString(
                                    R.string
                                            .edit_prayer_content_description,
                                    prayerName,
                                    statusName
                            )
                    );
        }

        private void applyStatusColor(
                Context context,
                PrayerStatus status
        ) {
            int statusColor =
                    ContextCompat.getColor(
                            context,
                            getStatusColorResource(status)
                    );

            ColorStateList colorStateList =
                    ColorStateList.valueOf(
                            statusColor
                    );

            binding.prayerStatusChip
                    .setTextColor(colorStateList);

            binding.prayerStatusChip
                    .setChipStrokeColor(
                            colorStateList
                    );
        }

        @ColorRes
        private static int getStatusColorResource(
                PrayerStatus status
        ) {
            switch (status) {
                case ON_TIME:
                    return R.color
                            .prayer_status_on_time;

                case LATE:
                    return R.color
                            .prayer_status_late;

                case MISSED:
                    return R.color
                            .prayer_status_missed;

                case UNRECORDED:
                default:
                    return R.color
                            .prayer_status_unrecorded;
            }
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

        private static String getStatusName(
                Context context,
                PrayerStatus status
        ) {
            switch (status) {
                case ON_TIME:
                    return context.getString(
                            R.string.status_on_time
                    );

                case LATE:
                    return context.getString(
                            R.string.status_late
                    );

                case MISSED:
                    return context.getString(
                            R.string.status_missed
                    );

                case UNRECORDED:
                default:
                    return context.getString(
                            R.string.status_unrecorded
                    );
            }
        }
    }
}