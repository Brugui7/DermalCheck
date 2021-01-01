package com.brugui.dermalcheck.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.RequestListDataSource;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.ui.adapters.RequestAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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
    private RecyclerView rvRequests;
    private RequestListDataSource dataSource;
    private LoggedInUser userLogged;
    private List<Request> requests;
    private RequestAdapter adapter;
    private static final String TAG = "Logger RequestList";



    public RequestsFragment() {}


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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        fabNewRequest = view.findViewById(R.id.fabNewRequest);
        rvRequests = view.findViewById(R.id.rvRequests);
        fabNewRequest.setOnClickListener(listenerFabNewRequest);
        getRequests();
        return view;
    }

    private void setUpRecyclerView(Result<List<Request>> result) {
        if (result instanceof Result.Success) {
            requests = ((Result.Success<List<Request>>) result).getData();
        } else {
            //TODO display empty list
        }
        rvRequests.setHasFixedSize(true);
        adapter = new RequestAdapter(requests);
        rvRequests.setAdapter(adapter);
    }

    private void getRequests() {
        Result<List<Request>> result = dataSource.getRequests(userLogged);
    }

    // ########## Listeners ##########
    private final View.OnClickListener listenerFabNewRequest = view -> {
        Intent intent = new Intent(getActivity(), NewRequestActivity.class);
        startActivity(intent);
    };
}