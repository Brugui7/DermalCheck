package com.brugui.dermalcheck.ui.request.detail;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.brugui.dermalcheck.data.RequestDetailDataSource;
import com.brugui.dermalcheck.data.RequestListDataSource;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

//TODO implement
public class RequestDetailViewModel extends ViewModel {
    private static final String TAG = "Logger RequestDetVM";
    private LoggedInUser userLogged;
    private MutableLiveData<List<String>> images = new MutableLiveData<>();
    private RequestDetailDataSource dataSource;

    public RequestDetailViewModel() {
        FirebaseUser userTmp = FirebaseAuth.getInstance().getCurrentUser();
        userLogged = new LoggedInUser(userTmp.getUid(), userTmp.getDisplayName());
        dataSource = new RequestDetailDataSource();
    }

    public void fetchImages(String requestId){
        dataSource.fetchImages(requestId, result -> {
            if (result instanceof Result.Success) {
                images.setValue(((Result.Success<List<String>>) result).getData());

            }
        });
    }

    public MutableLiveData<List<String>> getImages() {
        return images;
    }
}
