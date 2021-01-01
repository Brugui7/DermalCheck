package com.brugui.dermalcheck.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

    public Result<LoggedInUser> login(String username, String password) {
        try {
            auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            result = new Result.Success<>(new LoggedInUser(user.getUid(), user.getDisplayName()));
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("email", user.getEmail());
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(user.getUid())
                                    .set(map);
                        } else {
                            Log.d(TAG, "Login error " + task.getException().getMessage(), task.getException());
                            result = new Result.Error(new IOException("Error logging in", task.getException()));
                        }
                    });
            return result;

        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
            return new Result.Error(new IOException("Error", e));
        }
    }


    public void logout() {
        // TODO: revoke authentication
    }
}