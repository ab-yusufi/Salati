package com.abcoder.salati.ui.today;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abcoder.salati.R;
import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.model.PrayerStatus;
import com.abcoder.salati.data.model.PrayerType;
import com.abcoder.salati.data.repository.PrayerRepository;
import com.abcoder.salati.databinding.BottomSheetPrayerStatusBinding;
import com.abcoder.salati.databinding.FragmentTodayBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

public class TodayFragment extends Fragment {

    private FragmentTodayBinding binding;

    private TodayViewModel todayViewModel;

    private PrayerListAdapter prayerListAdapter;
    private TodayHabitAdapter habitAdapter;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding =
                FragmentTodayBinding.inflate(
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

        configureViewModel();
        configurePrayerList();
        configureHabitList();
        configureDate();
        observePrayerRecords();
        observeHabitRecords();
    }

    private void configureViewModel() {
        SalatiApplication application =
                (SalatiApplication)
                        requireActivity()
                                .getApplication();

        TodayViewModelFactory factory =
                new TodayViewModelFactory(
                        application
                                .getPrayerRepository(),
                        application
                                .getHabitRepository()
                );

        todayViewModel =
                new ViewModelProvider(
                        this,
                        factory
                ).get(TodayViewModel.class);
    }

    private void configurePrayerList() {
        prayerListAdapter =
                new PrayerListAdapter(
                        this::showPrayerStatusSheet
                );

        binding.prayerList.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        binding.prayerList.setAdapter(
                prayerListAdapter
        );

        binding.prayerList.setNestedScrollingEnabled(
                false
        );
    }

    private void configureHabitList() {
        habitAdapter =
                new TodayHabitAdapter(
                        (habitId, status) ->
                                todayViewModel
                                        .setHabitStatus(
                                                habitId,
                                                status
                                        )
                );

        binding.habitList.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        binding.habitList.setAdapter(
                habitAdapter
        );

        binding.habitList.setNestedScrollingEnabled(
                false
        );
    }

    private void configureDate() {
        binding.todayDateText.setText(
                todayViewModel.getDisplayDate()
        );
    }

    private void observePrayerRecords() {
        todayViewModel
                .getTodayPrayerRecords()
                .observe(
                        getViewLifecycleOwner(),
                        prayerListAdapter::submitList
                );
    }

    private void observeHabitRecords() {
        todayViewModel
                .getTodayHabitItems()
                .observe(
                        getViewLifecycleOwner(),
                        items -> {
                            habitAdapter.submitList(items);

                            boolean empty =
                                    items == null
                                            || items.isEmpty();

                            binding.noHabitsText
                                    .setVisibility(
                                            empty
                                                    ? View.VISIBLE
                                                    : View.GONE
                                    );

                            binding.habitList
                                    .setVisibility(
                                            empty
                                                    ? View.GONE
                                                    : View.VISIBLE
                                    );
                        }
                );
    }

    private void showPrayerStatusSheet(
            PrayerType prayerType,
            PrayerStatus currentStatus
    ) {
        BottomSheetPrayerStatusBinding
                sheetBinding =
                BottomSheetPrayerStatusBinding
                        .inflate(
                                getLayoutInflater()
                        );

        BottomSheetDialog dialog =
                new BottomSheetDialog(
                        requireContext()
                );

        dialog.setContentView(
                sheetBinding.getRoot()
        );

        String prayerName =
                getPrayerName(prayerType);

        boolean recorded =
                currentStatus
                        != PrayerStatus.UNRECORDED;

        String title =
                getString(
                        recorded
                                ? R.string
                                  .edit_prayer_title_format
                                : R.string
                                  .log_prayer_title_format,
                        prayerName
                );

        dialog.setTitle(title);

        sheetBinding.sheetTitleText.setText(
                title
        );

        sheetBinding.currentStatusText.setText(
                getString(
                        R.string
                                .current_prayer_status_format,
                        getStatusName(currentStatus)
                )
        );

        sheetBinding.onTimeButton.setEnabled(
                currentStatus
                        != PrayerStatus.ON_TIME
        );

        sheetBinding.lateButton.setEnabled(
                currentStatus
                        != PrayerStatus.LATE
        );

        sheetBinding.missedButton.setEnabled(
                currentStatus
                        != PrayerStatus.MISSED
        );

        sheetBinding.clearRecordButton
                .setVisibility(
                        recorded
                                ? View.VISIBLE
                                : View.GONE
                );

        sheetBinding.onTimeButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                prayerType,
                                currentStatus,
                                PrayerStatus.ON_TIME
                        )
                );

        sheetBinding.lateButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                prayerType,
                                currentStatus,
                                PrayerStatus.LATE
                        )
                );

        sheetBinding.missedButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                prayerType,
                                currentStatus,
                                PrayerStatus.MISSED
                        )
                );

        sheetBinding.clearRecordButton
                .setOnClickListener(
                        view -> savePrayerStatus(
                                dialog,
                                prayerType,
                                currentStatus,
                                PrayerStatus.UNRECORDED
                        )
                );

        sheetBinding.cancelButton
                .setOnClickListener(
                        view -> dialog.dismiss()
                );

        dialog.show();
    }

    private void savePrayerStatus(
            BottomSheetDialog dialog,
            PrayerType prayerType,
            PrayerStatus previousStatus,
            PrayerStatus newStatus
    ) {
        dialog.dismiss();

        todayViewModel.setPrayerStatus(
                prayerType,
                newStatus,
                new PrayerRepository
                        .OperationCallback() {

                    @Override
                    public void onSuccess() {
                        showPrayerSavedMessage(
                                prayerType,
                                previousStatus,
                                newStatus
                        );
                    }

                    @Override
                    public void onError(
                            Exception exception
                    ) {
                        showSnackbar(
                                getString(
                                        R.string
                                                .prayer_save_failed_message,
                                        getPrayerName(
                                                prayerType
                                        )
                                )
                        );
                    }
                }
        );
    }

    private void showPrayerSavedMessage(
            PrayerType prayerType,
            PrayerStatus previousStatus,
            PrayerStatus newStatus
    ) {
        if (binding == null || !isAdded()) {
            return;
        }

        String message;

        if (newStatus
                == PrayerStatus.UNRECORDED) {

            message =
                    getString(
                            R.string
                                    .prayer_record_cleared_message,
                            getPrayerName(prayerType)
                    );

        } else {
            message =
                    getString(
                            R.string
                                    .prayer_saved_message,
                            getPrayerName(prayerType),
                            getStatusName(newStatus)
                    );
        }

        Snackbar snackbar =
                Snackbar.make(
                        binding.todayRoot,
                        message,
                        Snackbar.LENGTH_LONG
                );

        snackbar.setAction(
                R.string.action_undo,
                view -> undoPrayerStatus(
                        prayerType,
                        previousStatus
                )
        );

        snackbar.show();
    }

    private void undoPrayerStatus(
            PrayerType prayerType,
            PrayerStatus previousStatus
    ) {
        todayViewModel.setPrayerStatus(
                prayerType,
                previousStatus,
                new PrayerRepository
                        .OperationCallback() {

                    @Override
                    public void onSuccess() {
                        showSnackbar(
                                getString(
                                        R.string
                                                .prayer_change_undone_message,
                                        getPrayerName(
                                                prayerType
                                        )
                                )
                        );
                    }

                    @Override
                    public void onError(
                            Exception exception
                    ) {
                        showSnackbar(
                                getString(
                                        R.string
                                                .prayer_undo_failed_message,
                                        getPrayerName(
                                                prayerType
                                        )
                                )
                        );
                    }
                }
        );
    }

    private void showSnackbar(
            String message
    ) {
        if (binding == null || !isAdded()) {
            return;
        }

        Snackbar.make(
                binding.todayRoot,
                message,
                Snackbar.LENGTH_LONG
        ).show();
    }

    private String getPrayerName(
            PrayerType prayerType
    ) {
        switch (prayerType) {
            case FAJR:
                return getString(
                        R.string.prayer_fajr
                );

            case DHUHR:
                return getString(
                        R.string.prayer_dhuhr
                );

            case ASR:
                return getString(
                        R.string.prayer_asr
                );

            case MAGHRIB:
                return getString(
                        R.string.prayer_maghrib
                );

            case ISHA:
                return getString(
                        R.string.prayer_isha
                );

            default:
                throw new IllegalArgumentException(
                        "Unknown prayer type: "
                                + prayerType
                );
        }
    }

    private String getStatusName(
            PrayerStatus status
    ) {
        switch (status) {
            case ON_TIME:
                return getString(
                        R.string.status_on_time
                );

            case LATE:
                return getString(
                        R.string.status_late
                );

            case MISSED:
                return getString(
                        R.string.status_missed
                );

            case UNRECORDED:
            default:
                return getString(
                        R.string.status_unrecorded
                );
        }
    }

    @Override
    public void onDestroyView() {
        binding.prayerList.setAdapter(null);
        binding.habitList.setAdapter(null);

        prayerListAdapter = null;
        habitAdapter = null;
        binding = null;

        super.onDestroyView();
    }
}