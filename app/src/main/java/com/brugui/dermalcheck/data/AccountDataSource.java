package com.brugui.dermalcheck.data;

import android.util.Log;

import com.brugui.dermalcheck.data.interfaces.OnDataFetched;
import com.brugui.dermalcheck.data.interfaces.OnDataUpdated;
import com.brugui.dermalcheck.data.interfaces.OnRequestUpdated;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Rol;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that handles the creation an retrieving requests
 */
public class AccountDataSource {

    private Result<LoggedInUser> result;
    private static final String TAG = "Logger AccountDS";
    private FirebaseFirestore db;

    public AccountDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     *
     * @param uid String
     * @param callback OnDataFetched
     */
    public void fetchUserData(String uid, OnDataFetched callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshot -> {
                    result = new Result.Success<>(queryDocumentSnapshot.toObject(LoggedInUser.class));
                    callback.OnDataFetched(result);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, e.getMessage(), e);
                    result = new Result.Error(new IOException("Error retrieving user"));
                    callback.OnDataFetched(result);
                });
    }


    /**
     *
     * @param loggedInUser LoggedInUser
     * @param callback OnDataUpdated
     */
    public void updateUserData(LoggedInUser loggedInUser, OnDataUpdated callback) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(loggedInUser.getUid())
                .update(loggedInUser.toMap())
                .addOnSuccessListener(result -> {
                    Log.d(TAG, "Success updating user data");
                    callback.onDataUpdated(new Result.Success(null));
                })
                .addOnFailureListener(result -> {
                   Log.e(TAG, "Error updating user data");
                    callback.onDataUpdated(new Result.Error(new IOException("Error updating user data")));
                });
}

}