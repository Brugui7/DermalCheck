package com.brugui.dermalcheck.ui.request.list;

import android.content.Intent;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.interfaces.OnItemClick;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Rol;
import com.brugui.dermalcheck.ui.request.creation.NewRequestActivity;
import com.brugui.dermalcheck.ui.adapters.RequestAdapter;
import com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
    private List<Request> requests;
    private RequestAdapter adapter;
    private RequestsViewModel requestsViewModel;
    private SwipeRefreshLayout srLayout;
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

        requests = new ArrayList<>();
        requestsViewModel = new RequestsViewModel();
        requestsViewModel.loadUserData(getContext());
        requestsViewModel.fetchRequests();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);
        fabNewRequest = view.findViewById(R.id.fabNewRequest);
        rvRequests = view.findViewById(R.id.rvRequests);
        clEmptyList = view.findViewById(R.id.clEmptyList);
        srLayout = view.findViewById(R.id.srLayout);
        fabNewRequest.setOnClickListener(listenerFabNewRequest);

        LoggedInUser loggedInUser = requestsViewModel.getUserLogged();
        if (loggedInUser != null && loggedInUser.getRole() != null) {
            if (loggedInUser.getRole().equalsIgnoreCase(Rol.SPECIALIST_ROL)) {
                fabNewRequest.setVisibility(View.GONE);
            }
        }

        setUpRecyclerView();
        requestsViewModel.getRequests().observe(getViewLifecycleOwner(), fetchedRequests -> {
            requests.clear();
            requests.addAll(fetchedRequests);
            adapter.notifyDataSetChanged();
            showEmptyListMessage(fetchedRequests.size() == 0);
            srLayout.setRefreshing(false);
        });

        srLayout.setOnRefreshListener(() -> requestsViewModel.fetchRequests());
        return view;
    }

    private void setUpRecyclerView() {
        rvRequests.setHasFixedSize(true);
        rvRequests.setItemAnimator(new DefaultItemAnimator());
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RequestAdapter(requests, onItemClick);
        rvRequests.setAdapter(adapter);
    }

    // ########## Listeners ##########
    private final View.OnClickListener listenerFabNewRequest = view -> {
        Intent intent = new Intent(getActivity(), NewRequestActivity.class);
        startActivity(intent);
    };

    private void showEmptyListMessage(boolean show) {
        clEmptyList.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private final OnItemClick onItemClick = position -> {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), RequestDetailActivity.class);
        intent.putExtra(RequestDetailActivity.REQUEST, requests.get(position));
        getActivity().startActivity(intent);
    };
}