package com.brugui.dermalcheck.ui.request.list;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.RequestListDataSource;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.ui.NewRequestActivity;
import com.brugui.dermalcheck.ui.adapters.RequestAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RequestsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RequestsFragment extends Fragment {
    private FloatingActionButton fabNewRequest;
    private ConstraintLayout clEmptyList;
    private RecyclerView rvRequests;
    private RequestListDataSource dataSource;
    private LoggedInUser userLogged;
    private List<Request> requests;
    private RequestAdapter adapter;
    private RequestsViewModel requestsViewModel;
    private static final String TAG = "Logger RequestList";


    public RequestsFragment() {
    }


    public static RequestsFragment newInstance() {
        RequestsFragment fragment = new RequestsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataSource = new RequestListDataSource();
        FirebaseUser userTmp = FirebaseAuth.getInstance().getCurrentUser();
        userLogged = new LoggedInUser(userTmp.getUid(), userTmp.getDisplayName());
        requests = new ArrayList<>();
        requestsViewModel = new RequestsViewModel();
        requestsViewModel.fetchRequests();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        fabNewRequest = view.findViewById(R.id.fabNewRequest);
        rvRequests = view.findViewById(R.id.rvRequests);
        clEmptyList = view.findViewById(R.id.clEmptyList);
        fabNewRequest.setOnClickListener(listenerFabNewRequest);
        setUpRecyclerView();
        requestsViewModel.getRequests().observe(getViewLifecycleOwner(), fetchedRequests -> {
            requests.clear();
            requests.addAll(fetchedRequests);
            adapter.notifyDataSetChanged();
            showEmptyListMessage(fetchedRequests.size() == 0);
        });
        return view;
    }

    private void setUpRecyclerView() {
        rvRequests.setHasFixedSize(true);
        rvRequests.setItemAnimator(new DefaultItemAnimator());
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RequestAdapter(requests);
        rvRequests.setAdapter(adapter);
    }

    // ########## Listeners ##########
    private final View.OnClickListener listenerFabNewRequest = view -> {
        Intent intent = new Intent(getActivity(), NewRequestActivity.class);
        startActivity(intent);
    };

    private void showEmptyListMessage(boolean show){
        clEmptyList.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}