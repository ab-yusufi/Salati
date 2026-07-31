package com.abcoder.salati.ui.today;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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

    public interface OnPrayerStatusSelectedListener {

        void onPrayerStatusSelected(
                PrayerType prayerType,
                PrayerStatus prayerStatus
        );
    }

    private final List<PrayerRecord> prayerRecords =
            new ArrayList<>();

    private final OnPrayerStatusSelectedListener listener;

    public PrayerListAdapter(
            OnPrayerStatusSelectedListener listener
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
         * There are only five prayer rows, so a full refresh is
         * acceptable for Version 1.
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
                LayoutInflater.from(parent.getContext());

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
        PrayerRecord prayerRecord =
                prayerRecords.get(position);

        holder.bind(prayerRecord, listener);
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
                OnPrayerStatusSelectedListener listener
        ) {
            Context context =
                    binding.getRoot().getContext();

            binding.prayerNameText.setText(
                    getPrayerName(
                            context,
                            prayerRecord.prayerType
                    )
            );

            String statusName =
                    getStatusName(
                            context,
                            prayerRecord.status
                    );

            binding.prayerStatusText.setText(
                    context.getString(
                            R.string.prayer_status_format,
                            statusName
                    )
            );

            /*
             * Disable the currently selected status button.
             * The other buttons remain available for corrections.
             */
            binding.onTimeButton.setEnabled(
                    prayerRecord.status
                            != PrayerStatus.ON_TIME
            );

            binding.lateButton.setEnabled(
                    prayerRecord.status
                            != PrayerStatus.LATE
            );

            binding.missedButton.setEnabled(
                    prayerRecord.status
                            != PrayerStatus.MISSED
            );

            binding.clearButton.setVisibility(
                    prayerRecord.status
                            == PrayerStatus.UNRECORDED
                            ? View.GONE
                            : View.VISIBLE
            );

            binding.onTimeButton.setOnClickListener(view ->
                    listener.onPrayerStatusSelected(
                            prayerRecord.prayerType,
                            PrayerStatus.ON_TIME
                    )
            );

            binding.lateButton.setOnClickListener(view ->
                    listener.onPrayerStatusSelected(
                            prayerRecord.prayerType,
                            PrayerStatus.LATE
                    )
            );

            binding.missedButton.setOnClickListener(view ->
                    listener.onPrayerStatusSelected(
                            prayerRecord.prayerType,
                            PrayerStatus.MISSED
                    )
            );

            binding.clearButton.setOnClickListener(view ->
                    listener.onPrayerStatusSelected(
                            prayerRecord.prayerType,
                            PrayerStatus.UNRECORDED
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
                            "Unknown prayer type: "
                                    + prayerType
                    );
            }
        }

        private static String getStatusName(
                Context context,
                PrayerStatus prayerStatus
        ) {
            switch (prayerStatus) {
                case UNRECORDED:
                    return context.getString(
                            R.string.status_unrecorded
                    );

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

                default:
                    throw new IllegalArgumentException(
                            "Unknown prayer status: "
                                    + prayerStatus
                    );
            }
        }
    }
}