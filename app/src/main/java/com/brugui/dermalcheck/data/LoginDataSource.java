package com.brugui.dermalcheck.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.brugui.dermalcheck.data.interfaces.OnLoginFinished;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private FirebaseAuth auth;
    private Result<LoggedInUser> result;
    private static final String TAG = "Logger LoginDS";

    public void login(String username, String password, OnLoginFinished callback) {
        try {
            auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "Login error " + task.getException().getMessage(), task.getException());
                            callback.onLoginFinished(new Result.Error(new IOException("Error logging in", task.getException())));
                            return;
                        }

                        FirebaseUser user = auth.getCurrentUser();

                        Map<String, Object> map = new HashMap<>();
                        map.put("email", user.getEmail());
                        map.put("uid", user.getUid());

                        // Updates the user data
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getUid())
                                .set(map);

                        // The user subscribes to its own notification channel
                        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();

                        firebaseMessaging.subscribeToTopic("/topics/" + user.getUid());
                        //for general purposes
                        firebaseMessaging.subscribeToTopic("/topics/dermalcheck");

                        callback.onLoginFinished(new Result.Success<>(new LoggedInUser(user.getUid(), user.getDisplayName())));

                    });
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
            callback.onLoginFinished(new Result.Error(new IOException("Error", e)));
        }
    }


    public void logout() {
        // TODO: revoke authentication
    }
}