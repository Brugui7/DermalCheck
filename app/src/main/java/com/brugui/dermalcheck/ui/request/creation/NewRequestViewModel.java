package com.brugui.dermalcheck.ui.request.creation;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.request.NewRequestDataSource;

import java.util.List;

//TODO implement
public class NewRequestViewModel extends ViewModel {
    private static final String TAG = "Logger NewRequestVM";
    private MutableLiveData<Long> requestNumber = new MutableLiveData<>();
    private NewRequestDataSource dataSource;

    public NewRequestViewModel() {
        dataSource = new NewRequestDataSource();
    }


    public void fetchNewRequestNumber() {
        dataSource.fetchRequestsNumber(result -> {
            if (result instanceof Result.Success) {
                Long number = ((Result.Success<Long>) result).getData();
                if (number != null){
                    requestNumber.setValue(++number);
                }

            }
        });
    }

    public MutableLiveData<Long> getRequestNumber() {
        return requestNumber;
    }
}
