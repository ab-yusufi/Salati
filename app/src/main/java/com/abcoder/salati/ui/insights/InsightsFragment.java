package com.abcoder.salati.ui.insights;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.abcoder.salati.databinding.FragmentInsightsBinding;
import com.abcoder.salati.ui.reports.ReportsActivity;

public class InsightsFragment extends Fragment {

    private FragmentInsightsBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding =
                FragmentInsightsBinding.inflate(
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

        binding.openReportsButton
                .setOnClickListener(
                        button -> startActivity(
                                new Intent(
                                        requireContext(),
                                        ReportsActivity.class
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