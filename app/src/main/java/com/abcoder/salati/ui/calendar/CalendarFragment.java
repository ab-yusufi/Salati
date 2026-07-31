package com.abcoder.salati.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.abcoder.salati.R;
import com.abcoder.salati.databinding.FragmentCalendarBinding;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    public static final String
            REQUEST_KEY_SELECTED_DATE =
            "calendar_selected_date_request";

    public static final String
            BUNDLE_KEY_SELECTED_DATE =
            "calendar_selected_date";

    private FragmentCalendarBinding binding;

    private LocalDate selectedDate =
            LocalDate.now();

    @Override
    public void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        getParentFragmentManager()
                .setFragmentResultListener(
                        REQUEST_KEY_SELECTED_DATE,
                        this,
                        (requestKey, result) -> {
                            String dateValue =
                                    result.getString(
                                            BUNDLE_KEY_SELECTED_DATE
                                    );

                            if (dateValue == null) {
                                return;
                            }

                            try {
                                selectedDate =
                                        LocalDate.parse(
                                                dateValue
                                        );

                                updateSelectedDate();

                            } catch (
                                    IllegalArgumentException
                                            exception
                            ) {
                                selectedDate =
                                        LocalDate.now();

                                updateSelectedDate();
                            }
                        }
                );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding =
                FragmentCalendarBinding.inflate(
                        inflater,
                        container,
                        false
                );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(
                view,
                savedInstanceState
        );

        updateSelectedDate();
    }

    private void updateSelectedDate() {
        if (binding == null) {
            return;
        }

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "EEEE, d MMMM yyyy",
                        Locale.getDefault()
                );

        binding.selectedDateText.setText(
                getString(
                        R.string
                                .calendar_selected_date_format,
                        selectedDate.format(
                                formatter
                        )
                )
        );
    }

    @Override
    public void onDestroyView() {
        binding = null;

        super.onDestroyView();
    }
}