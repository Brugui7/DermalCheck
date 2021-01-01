package com.brugui.dermalcheck.ui.request.list;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.brugui.dermalcheck.data.RequestListDataSource;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.interfaces.OnDataFetched;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class RequestsViewModel  extends ViewModel {
    private static final String TAG = "Logger RequestListVM";
    private LoggedInUser userLogged;
    private MutableLiveData<List<Request>> requests = new MutableLiveData<>();
    private RequestListDataSource dataSource;

    public RequestsViewModel() {
        FirebaseUser userTmp = FirebaseAuth.getInstance().getCurrentUser();
        userLogged = new LoggedInUser(userTmp.getUid(), userTmp.getDisplayName());
        dataSource = new RequestListDataSource();
    }

    public void fetchRequests(){
        dataSource.fetchRequests(userLogged, result -> {
            if (result instanceof Result.Success) {
                Log.d(TAG, "Todo okey");
                requests.setValue(((Result.Success<List<Request>>) result).getData());
            }
        });
    }

    public MutableLiveData<List<Request>> getRequests() {
        return requests;
    }
}
