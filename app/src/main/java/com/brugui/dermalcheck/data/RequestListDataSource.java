package com.brugui.dermalcheck.data;

import android.util.Log;

import com.brugui.dermalcheck.data.interfaces.OnDataFetched;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

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

            db.collection("requests")
                    .whereEqualTo("status", Status.PENDING_STATUS_NAME)
                    .whereEqualTo("receiver", loggedInUser.getUserId())
                    .orderBy("estimatedProbability", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d(TAG, "ok " + queryDocumentSnapshots.size() + " resultados");
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

}
