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

import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.databinding.FragmentTodayBinding;

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
                        (prayerType, prayerStatus) ->
                                todayViewModel
                                        .setPrayerStatus(
                                                prayerType,
                                                prayerStatus
                                        )
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