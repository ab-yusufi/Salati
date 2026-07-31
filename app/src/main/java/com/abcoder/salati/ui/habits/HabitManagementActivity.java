package com.abcoder.salati.ui.habits;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.abcoder.salati.R;
import com.abcoder.salati.SalatiApplication;
import com.abcoder.salati.data.entity.Habit;
import com.abcoder.salati.data.repository.HabitRepository;
import com.abcoder.salati.databinding.ActivityHabitManagementBinding;
import com.abcoder.salati.databinding.DialogHabitEditorBinding;

public class HabitManagementActivity
        extends AppCompatActivity {

    private ActivityHabitManagementBinding binding;

    private HabitManagementViewModel viewModel;

    private HabitManagementAdapter adapter;

    private List<Habit> latestHabits =
            new ArrayList<>();

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        binding =
                ActivityHabitManagementBinding.inflate(
                        getLayoutInflater()
                );

        setContentView(binding.getRoot());

        configureSystemBars();
        configureViewModel();
        configureList();
        configureButtons();
        observeHabits();
    }

    private void configureSystemBars() {
        ViewCompat.setOnApplyWindowInsetsListener(
                binding.main,
                (view, insets) -> {
                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat
                                            .Type
                                            .systemBars()
                            );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );
    }

    private void configureViewModel() {
        SalatiApplication application =
                (SalatiApplication)
                        getApplication();

        HabitManagementViewModelFactory factory =
                new HabitManagementViewModelFactory(
                        application
                );

        viewModel =
                new ViewModelProvider(this, factory)
                        .get(
                                HabitManagementViewModel.class
                        );
    }

    private void configureList() {
        adapter = new HabitManagementAdapter(
                new HabitManagementAdapter
                        .HabitActionListener() {

                    @Override
                    public void onEnabledChanged(
                            Habit habit,
                            boolean enabled
                    ) {
                        Habit changedHabit =
                                copyHabit(
                                        habit,
                                        enabled
                                );

                        saveHabit(
                                changedHabit,
                                null
                        );
                    }

                    @Override
                    public void onEditClicked(
                            Habit habit
                    ) {
                        showHabitEditor(habit);
                    }

                    @Override
                    public void onDeleteClicked(
                            Habit habit
                    ) {
                        showDeleteConfirmation(habit);
                    }
                }
        );

        binding.habitList.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.habitList.setAdapter(adapter);
    }

    private void configureButtons() {
        binding.backButton.setOnClickListener(
                view -> finish()
        );

        binding.addHabitButton.setOnClickListener(
                view -> showHabitEditor(null)
        );
    }

    private void observeHabits() {
        viewModel.getHabits().observe(
                this,
                habits -> {
                    latestHabits = habits == null
                            ? new ArrayList<>()
                            : new ArrayList<>(habits);

                    adapter.submitList(latestHabits);

                    binding.emptyText.setVisibility(
                            latestHabits.isEmpty()
                                    ? View.VISIBLE
                                    : View.GONE
                    );
                }
        );
    }

    private void showHabitEditor(
            @Nullable Habit existingHabit
    ) {
        DialogHabitEditorBinding dialogBinding =
                DialogHabitEditorBinding.inflate(
                        getLayoutInflater()
                );

        int defaultHour = existingHabit == null
                ? 21
                : existingHabit.reminderHour;

        int defaultMinute = existingHabit == null
                ? 0
                : existingHabit.reminderMinute;

        int[] selectedTime = {
                defaultHour,
                defaultMinute
        };

        if (existingHabit != null) {
            dialogBinding.habitTitleInput.setText(
                    existingHabit.title
            );
        }

        dialogBinding.enabledSwitch.setChecked(
                existingHabit == null
                        || existingHabit.enabled
        );

        updateTimeButton(
                dialogBinding,
                selectedTime[0],
                selectedTime[1]
        );

        dialogBinding.timeButton
                .setOnClickListener(view -> {
                    boolean use24HourClock =
                            DateFormat
                                    .is24HourFormat(this);

                    new TimePickerDialog(
                            this,
                            (picker, hour, minute) -> {
                                selectedTime[0] = hour;
                                selectedTime[1] = minute;

                                updateTimeButton(
                                        dialogBinding,
                                        hour,
                                        minute
                                );
                            },
                            selectedTime[0],
                            selectedTime[1],
                            use24HourClock
                    ).show();
                });

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(this)
                        .setTitle(
                                existingHabit == null
                                        ? R.string.add_habit
                                        : R.string.edit_habit
                        )
                        .setView(dialogBinding.getRoot())
                        .setNegativeButton(
                                R.string.cancel,
                                null
                        )
                        .setPositiveButton(
                                R.string.save,
                                null
                        )
                        .create();

        dialog.setOnShowListener(ignored ->
                dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).setOnClickListener(view -> {

                    String title =
                            dialogBinding
                                    .habitTitleInput
                                    .getText() == null
                                    ? ""
                                    : dialogBinding
                                    .habitTitleInput
                                    .getText()
                                    .toString()
                                    .trim();

                    if (title.isEmpty()) {
                        dialogBinding
                                .habitTitleInput
                                .setError(
                                        getString(
                                                R.string
                                                        .habit_title_required
                                        )
                                );

                        return;
                    }

                    long now =
                            System.currentTimeMillis();

                    Habit habit = new Habit(
                            existingHabit == null
                                    ? 0
                                    : existingHabit.id,
                            title,
                            selectedTime[0],
                            selectedTime[1],
                            HabitRepository
                                    .DEFAULT_SNOOZE_MINUTES,
                            dialogBinding
                                    .enabledSwitch
                                    .isChecked(),
                            existingHabit == null
                                    ? now
                                    : existingHabit.createdAt,
                            now
                    );

                    saveHabit(
                            habit,
                            dialog
                    );
                })
        );

        dialog.show();
    }

    private void saveHabit(
            Habit habit,
            @Nullable AlertDialog dialogToClose
    ) {
        viewModel.saveHabit(
                habit,
                new HabitRepository.SaveHabitCallback() {

                    @Override
                    public void onSuccess(
                            Habit savedHabit
                    ) {
                        if (dialogToClose != null) {
                            dialogToClose.dismiss();
                        }
                    }

                    @Override
                    public void onLimitReached() {
                        Toast.makeText(
                                HabitManagementActivity.this,
                                R.string.habit_limit_reached,
                                Toast.LENGTH_LONG
                        ).show();

                        /*
                         * Restore a switch that was toggled even
                         * though the save was rejected.
                         */
                        adapter.submitList(latestHabits);
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                HabitManagementActivity.this,
                                getString(
                                        R.string.habit_save_failed,
                                        message
                                ),
                                Toast.LENGTH_LONG
                        ).show();

                        adapter.submitList(latestHabits);
                    }
                }
        );
    }

    private void showDeleteConfirmation(
            Habit habit
    ) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_habit)
                .setMessage(
                        getString(
                                R.string
                                        .delete_habit_confirmation,
                                habit.title
                        )
                )
                .setNegativeButton(
                        R.string.cancel,
                        null
                )
                .setPositiveButton(
                        R.string.delete,
                        (dialog, which) ->
                                deleteHabit(habit)
                )
                .show();
    }

    private void deleteHabit(Habit habit) {
        viewModel.deleteHabit(
                habit,
                new HabitRepository
                        .DeleteHabitCallback() {

                    @Override
                    public void onSuccess() {
                        // LiveData updates the list.
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(
                                HabitManagementActivity.this,
                                getString(
                                        R.string
                                                .habit_delete_failed,
                                        message
                                ),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    private Habit copyHabit(
            Habit habit,
            boolean enabled
    ) {
        return new Habit(
                habit.id,
                habit.title,
                habit.reminderHour,
                habit.reminderMinute,
                habit.snoozeMinutes,
                enabled,
                habit.createdAt,
                System.currentTimeMillis()
        );
    }

    private void updateTimeButton(
            DialogHabitEditorBinding binding,
            int hour,
            int minute
    ) {
        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                hour
        );

        calendar.set(
                Calendar.MINUTE,
                minute
        );

        String time =
                DateFormat.getTimeFormat(this)
                        .format(calendar.getTime());

        binding.timeButton.setText(
                getString(
                        R.string.habit_reminder_format,
                        time
                )
        );
    }
}