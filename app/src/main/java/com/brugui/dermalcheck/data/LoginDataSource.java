package com.brugui.dermalcheck.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.brugui.dermalcheck.data.interfaces.OnLoginFinished;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Rol;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private FirebaseAuth auth;
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
                        updateUserData(user);

                        LoggedInUser loggedInUser = new LoggedInUser(user.getUid(), user.getEmail());


                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getUid())
                                .get()
                                .addOnCompleteListener(userDataTask -> {
                                    if (userDataTask.isSuccessful()) {
                                        DocumentSnapshot userDocument = userDataTask.getResult();
                                        if (userDocument != null) {
                                            if (userDocument.contains("role")) {
                                                loggedInUser.setRole(userDocument.getString("role"));
                                            }

                                            if (userDocument.contains("displayName")) {
                                                loggedInUser.setDisplayName(userDocument.getString("displayName"));
                                            }
                                        }
                                    }

                                    callback.onLoginFinished(new Result.Success<>(loggedInUser));

                                });


                        subscribeToNotification(user.getUid());

                    });
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
            callback.onLoginFinished(new Result.Error(new IOException("Error", e)));
        }
    }


    public void logout() {
        // TODO: revoke authentication
    }

    private void subscribeToNotification(String uid) {
        // The user subscribes to its own notification channel
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();

        //for new Requests
        firebaseMessaging.subscribeToTopic("/topics/" + uid);

        //for general purposes
        firebaseMessaging.subscribeToTopic("/topics/dermalcheck");
    }

    private void updateUserData(FirebaseUser user) {
        Map<String, Object> map = new HashMap<>();
        map.put("email", user.getEmail());
        map.put("uid", user.getUid());



        // Updates the user data
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        map.put("role", Rol.GENERAL_ROL);
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(user.getUid())
                                .set(map);
                        return;
                    }
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.getUid())
                            .update(map);
                });

    }
}