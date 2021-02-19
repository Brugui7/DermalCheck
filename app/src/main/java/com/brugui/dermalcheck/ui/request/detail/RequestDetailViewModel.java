package com.brugui.dermalcheck.ui.request.detail;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.brugui.dermalcheck.data.interfaces.OnRequestUpdated;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
import com.brugui.dermalcheck.data.request.RequestDetailDataSource;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.SharedPreferencesRepository;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.List;

//TODO implement
public class RequestDetailViewModel extends ViewModel {
    private static final String TAG = "Logger RequestDetVM";
    private LoggedInUser userLogged;
    private MutableLiveData<List<String>> images = new MutableLiveData<>();
    private RequestDetailDataSource dataSource;


    public RequestDetailViewModel() {
        FirebaseUser userTmp = FirebaseAuth.getInstance().getCurrentUser();
        //Todo if null al login
        if (userTmp != null){
            userLogged = new LoggedInUser(userTmp.getUid(), userTmp.getEmail());
        }

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

    public void loadUserData(Context context) {
        userLogged = new SharedPreferencesRepository(context).getUserLogged();
    }

    /**
     *
     * @param request Request
     * @param onRequestUpdated OnRequestUpdated
     */
    public void updateRequest(Request request, OnRequestUpdated onRequestUpdated){
        dataSource.updateRequest(request, onRequestUpdated);
    }

    public void diagnose(Request request, OnRequestUpdated onRequestUpdated){
        request.setStatus(Status.DIAGNOSED_STATUS_NAME);
        request.setDiagnosticDate(Calendar.getInstance().getTime());
        dataSource.diagnose(request, onRequestUpdated);
    }


        public LoggedInUser getUserLogged() {
        return userLogged;
    }
}
