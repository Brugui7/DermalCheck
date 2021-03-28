package com.brugui.dermalcheck.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.content.Context;
import android.util.Patterns;

import com.brugui.dermalcheck.data.LoginRepository;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.SharedPreferencesRepository;
import com.brugui.dermalcheck.data.interfaces.OnLoginFinished;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.R;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final LoginRepository loginRepository;
    private String password;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        this.password = password;
        loginRepository.login(username, password, onLoginFinished);
    }

    private final OnLoginFinished onLoginFinished = result -> {
        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            loginResult.setValue(new LoginResult(data));
        } else {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    };

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
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

    public void persistUserData(Context context, LoggedInUser user){
        user.setPassword(this.password);
        SharedPreferencesRepository sharedPreferencesRepository = new SharedPreferencesRepository(context);
        sharedPreferencesRepository.saveUserLogged(user);
    }

    public LoggedInUser getPersistedUserEmail(Context context){
        SharedPreferencesRepository sharedPreferencesRepository = new SharedPreferencesRepository(context);
        LoggedInUser loggedInUser = sharedPreferencesRepository.getUserLogged();
        return loggedInUser;
    }
}