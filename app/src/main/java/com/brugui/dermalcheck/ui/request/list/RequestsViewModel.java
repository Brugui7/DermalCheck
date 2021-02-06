package com.brugui.dermalcheck.ui.request.list;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.brugui.dermalcheck.data.RequestListDataSource;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.SharedPreferencesRepository;
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
        //Todo if null al login
        if (userTmp != null){
            userLogged = new LoggedInUser(userTmp.getUid(), userTmp.getEmail());
        }

        dataSource = new RequestListDataSource();
    }

    public void fetchRequests(){
        dataSource.fetchRequests(userLogged, result -> {
            if (result instanceof Result.Success) {
                requests.setValue(((Result.Success<List<Request>>) result).getData());
            }
        });
    }

    public MutableLiveData<List<Request>> getRequests() {
        return requests;
    }

    public void loadUserData(Context context) {
        userLogged = new SharedPreferencesRepository(context).getUserLogged();
    }

    public LoggedInUser getUserLogged() {
        return userLogged;
    }

}
