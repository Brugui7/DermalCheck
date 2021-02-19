package com.brugui.dermalcheck.data.request;

import android.util.Log;

import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.interfaces.OnDataFetched;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Rol;
import com.brugui.dermalcheck.data.model.Status;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestListDataSource {
    private static final String TAG = "Logger RequestListDS";
    private Result<List<Request>> result;

    public void fetchRequests(LoggedInUser loggedInUser, OnDataFetched callback) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            if (loggedInUser.getRole() == null) {
                Log.e(TAG, "No rol");
                callback.OnDataFetched(new Result.Error(new IOException("User with no rol")));
                return;
            }
            Query query = db.collection("requests")
                    .whereEqualTo("status", Status.PENDING_STATUS_NAME);

            if (loggedInUser.getRole().equalsIgnoreCase(Rol.SPECIALIST_ROL)) {
                query = query.whereEqualTo("receiver", loggedInUser.getUserId());
            } else {
                query = query.whereEqualTo("sender", loggedInUser.getUserId());
            }

            query.orderBy("estimatedProbability", Query.Direction.DESCENDING)
                    .limit(30)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        List<Request> requests = queryDocumentSnapshots.toObjects(Request.class);
                        result = new Result.Success<>(requests);
                        callback.OnDataFetched(result);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, e.getMessage(), e);
                        result = new Result.Error(new IOException("Error retrieving requests"));
                        callback.OnDataFetched(result);
                    });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            callback.OnDataFetched(new Result.Error(new IOException("Error", e)));

        }
    }


    /**
     * Assigns a pending request to the given user
     *
     * @param loggedInUser the user logged, will be always a specialist
     * @param callback     OnDataFetched
     */
    public void getNewRequest(LoggedInUser loggedInUser, OnDataFetched callback) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("requests")
                    .whereEqualTo("status", Status.PENDING_STATUS_NAME)
                    .whereEqualTo("receiver", null)

                    .orderBy("estimatedProbability", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.size() == 0) {
                            result = new Result.Error(new IOException("No pending requests"));
                            callback.OnDataFetched(result);
                            return;
                        }

                        Request request = queryDocumentSnapshots.iterator().next().toObject(Request.class);
                        request.setReceiver(loggedInUser.getUserId());
                        db.collection("requests")
                                .document(request.getId())
                                .set(request.toMap(), SetOptions.merge())
                                .addOnSuccessListener(documentReference -> {
                                    callback.OnDataFetched(new Result.Success<>(request));
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, e.getMessage(), e);
                                    callback.OnDataFetched(new Result.Error(new IOException("Error updating request", e)));
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, e.getMessage(), e);
                        result = new Result.Error(new IOException("Error retrieving requests"));
                        callback.OnDataFetched(result);
                    });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            callback.OnDataFetched(new Result.Error(new IOException("Error", e)));

        }
    }

}
