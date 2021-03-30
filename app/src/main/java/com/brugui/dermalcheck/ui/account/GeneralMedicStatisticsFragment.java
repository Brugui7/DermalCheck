package com.brugui.dermalcheck.ui.account;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.utils.Classifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

public class GeneralMedicStatisticsFragment extends Fragment {

    private GeneralMedicStatisticsViewModel viewModel;
    private DonutProgressView dvStatisticsChart;
    private TextView tvRequestsDiagnosed, tvMatchingDiagnostics, tvFailedDiagnostics;
    private LoggedInUser user;

    public static GeneralMedicStatisticsFragment newInstance(LoggedInUser user) {
        GeneralMedicStatisticsFragment fragment =  new GeneralMedicStatisticsFragment();
        Bundle args = new Bundle();
        args.putSerializable("EXTRA_USER", user);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.general_medic_statistics_fragment, container, false);
        dvStatisticsChart = view.findViewById(R.id.dpvStatisticsChart);
        tvRequestsDiagnosed = view.findViewById(R.id.tvRequestsDiagnosed);
        tvMatchingDiagnostics = view.findViewById(R.id.tvMatchingDiagnostics);
        tvFailedDiagnostics = view.findViewById(R.id.tvFailedDiagnostics);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GeneralMedicStatisticsViewModel.class);
        user = (LoggedInUser) getArguments().getSerializable("EXTRA_USER");
        setChart();
    }

    private void setChart() {
        int failedRequests = user.getRequestsDiagnosed() - user.getMatchingRequestsDiagnosed();
        DonutSection section = new DonutSection("1", getContext().getColor(R.color.success), user.getMatchingRequestsDiagnosed());
        DonutSection section2 = new DonutSection("2", getContext().getColor(R.color.accent), failedRequests);
        dvStatisticsChart.setCap(user.getRequestsDiagnosed());
        List<DonutSection> sectionList = new ArrayList<>();
        sectionList.add(section);
        sectionList.add(section2);
        dvStatisticsChart.submitData(sectionList);
        tvRequestsDiagnosed.setText(getString(R.string.placeholder_diagnosed_requests, user.getRequestsDiagnosed()));
        tvMatchingDiagnostics.setText(String.valueOf(user.getMatchingRequestsDiagnosed()));
        tvFailedDiagnostics.setText(String.valueOf(failedRequests));


    }

}