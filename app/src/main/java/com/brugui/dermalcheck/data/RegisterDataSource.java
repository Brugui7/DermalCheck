package com.brugui.dermalcheck.data;

import android.util.Log;

import com.brugui.dermalcheck.data.interfaces.OnLoginFinished;
import com.brugui.dermalcheck.data.interfaces.OnRegisterFinished;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Rol;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class RegisterDataSource {

    private FirebaseAuth auth;
    private static final String TAG = "Logger RegDS";

    public void register(String name, String nick, String email, String password, OnRegisterFinished callback) {
        try {
            auth = FirebaseAuth.getInstance();
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.d(TAG, "Register error " + task.getException().getMessage(), task.getException());
                            callback.onRegisterFinished(new Result.Error(new IOException("Error registering", task.getException())));
                            return;
                        }
                        createUserOnDb(auth.getCurrentUser(), name, nick);
                        callback.onRegisterFinished(new Result.Success<>(null));
                    });
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
            callback.onRegisterFinished(new Result.Error(new IOException("Error", e)));
        }
    }

    private void createUserOnDb(FirebaseUser user, String name, String nick) {
        Map<String, Object> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("uid", user.getUid());
        map.put("displayName", name);
        map.put("nick", nick);
        map.put("role", Rol.GENERAL_ROL);


        //Creates the user on Firebase Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .set(map);

    }
}