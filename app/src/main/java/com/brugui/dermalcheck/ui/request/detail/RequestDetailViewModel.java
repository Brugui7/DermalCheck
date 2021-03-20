package com.brugui.dermalcheck.ui.request.detail;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
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
public class RequestDetailViewModel extends AndroidViewModel {
    private static final String TAG = "Logger RequestDetVM";
    private LoggedInUser userLogged;
    private MutableLiveData<List<String>> images = new MutableLiveData<>();
    private RequestDetailDataSource dataSource;


    public RequestDetailViewModel(Application application) {
        super(application);
        FirebaseUser userTmp = FirebaseAuth.getInstance().getCurrentUser();
        //Todo if null al login
        if (userTmp != null) {
            userLogged = new LoggedInUser(userTmp.getUid(), userTmp.getEmail());
        }

        dataSource = new RequestDetailDataSource();
    }

    public void fetchImages(String requestId) {
        dataSource.fetchImages(requestId, result -> {
            if (result instanceof Result.Success) {
                images.setValue(((Result.Success<List<String>>) result).getData());

            }
        });
    }

    public MutableLiveData<List<String>> getImages() {
        return images;
    }

    public void loadUserData() {
        userLogged = new SharedPreferencesRepository(getApplication()).getUserLogged();
    }

    public void persistUserData(){
        new SharedPreferencesRepository(getApplication()).saveUserLogged(userLogged);
    }

    /**
     * @param request          Request
     * @param onRequestUpdated OnRequestUpdated
     */
    public void updateRequest(Request request, OnRequestUpdated onRequestUpdated) {
        dataSource.updateRequest(request, onRequestUpdated);
    }

    /**
     * This method is called when a general medic sets the diagnostic of a request
     *
     * @param request          Request
     * @param onRequestUpdated callback
     */
    public void diagnose(Request request, boolean success, OnRequestUpdated onRequestUpdated) {
        request.setStatus(Status.DIAGNOSED_STATUS_NAME);
        request.setReceiver(null);

        List<String> requestsDiagnosed = userLogged.getRequestsDiagnosed();
        requestsDiagnosed.add(request.getId());
        userLogged.setRequestsDiagnosed(requestsDiagnosed);
        if (success) {
            userLogged.setMatchingRequestsDiagnosed(userLogged.getMatchingRequestsDiagnosed() + 1);
        }
        dataSource.updateUserData(userLogged);
        dataSource.updateRequest(request, onRequestUpdated);

    }

    public LoggedInUser getUserLogged() {
        return userLogged;
    }
}
