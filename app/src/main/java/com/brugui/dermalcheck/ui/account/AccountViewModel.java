package com.brugui.dermalcheck.ui.account;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.brugui.dermalcheck.data.AccountDataSource;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.SharedPreferencesRepository;
import com.brugui.dermalcheck.data.interfaces.OnDataUpdated;
import com.brugui.dermalcheck.data.interfaces.OnRequestUpdated;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.List;

public class AccountViewModel extends ViewModel {
    private static final String TAG = "Logger RequestDetVM";
    private LoggedInUser userLogged;
    private MutableLiveData<LoggedInUser> loggedInUserMutableLiveData = new MutableLiveData<>();
    private AccountDataSource dataSource;



    public AccountViewModel() {
        FirebaseUser userTmp = FirebaseAuth.getInstance().getCurrentUser();
        if (userTmp != null){
            userLogged = new LoggedInUser(userTmp.getUid(), userTmp.getEmail());
        }

        dataSource = new AccountDataSource();
    }

    public void fetchUserdata(String uid){
        dataSource.fetchUserData(uid, result -> {
            if (result instanceof Result.Success) {
                loggedInUserMutableLiveData.setValue(((Result.Success<LoggedInUser>) result).getData());
            }
        });
    }

    public MutableLiveData<LoggedInUser> getUserData() {
        return loggedInUserMutableLiveData;
    }

    /**
     *
     * @param loggedInUser LoggedInUser
     * @param onDataUpdated OnDataUpdated
     */
    public void updateUserData(LoggedInUser loggedInUser, OnDataUpdated onDataUpdated){
        dataSource.updateUserData(loggedInUser, onDataUpdated);
    }

    public void closeSession(){
        FirebaseAuth.getInstance().signOut();
    }

    public LoggedInUser getUserLogged() {
        return userLogged;
    }

}
