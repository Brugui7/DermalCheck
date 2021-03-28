package com.brugui.dermalcheck.ui.registration;

import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.RegisterDataSource;
import com.brugui.dermalcheck.data.Result;


public class RegisterViewModel extends ViewModel {
    private MutableLiveData<RegisterFormState> registerFormState = new MutableLiveData<>();
    private MutableLiveData<RegisterResult> registerResult = new MutableLiveData<>();
    private RegisterDataSource registerDataSource;

    public RegisterViewModel() {
        registerDataSource = new RegisterDataSource();
    }



    public void register(String name, String nick, String email, String password) {
        registerDataSource.register(name, nick, email, password, result -> {
            if (result instanceof Result.Success){
                registerResult.setValue(new RegisterResult(null));
            } else {
                registerResult.setValue(new RegisterResult(R.string.email_already_registered));
            }
        });
    }


    public void registerDataChanged(String email, String password, String confirmPassword) {
        if (!isUserNameValid(email)) {
            registerFormState.setValue(new RegisterFormState(R.string.invalid_username, null));
            return;
        }

        if (!isPasswordValid(password)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.invalid_password));
            return;
        }

        if (!password.equals(confirmPassword)) {
            registerFormState.setValue(new RegisterFormState(null, R.string.passwords_do_no_match));
            return;
        }

        registerFormState.setValue(new RegisterFormState(true));
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }

        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public LiveData<RegisterFormState> getRegisterFormState() {
        return registerFormState;
    }

    public LiveData<RegisterResult> getRegisterResult() {
        return registerResult;
    }
}