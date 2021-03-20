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
import java.util.Map;

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
            Query query = db.collection("requests");
            //.whereEqualTo("status", Status.PENDING_STATUS_NAME);

            if (loggedInUser.getRole().equalsIgnoreCase(Rol.SPECIALIST_ROL)) {
                query = query.whereEqualTo("sender", loggedInUser.getUid());
            } else {
                // TODO
                query = query.whereEqualTo("receiver", loggedInUser.getUid());
            }

            query.orderBy("status", Query.Direction.DESCENDING)
                    .orderBy("estimatedProbability", Query.Direction.DESCENDING)
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
    public void getNewRequest(LoggedInUser loggedInUser, String offsetId, OnDataFetched callback) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("requests")
                    .whereEqualTo("receiver", null)
                    .orderBy("id")
                    .startAfter(offsetId)
                    .limit(10)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.size() == 0) {
                            result = new Result.Error(new IOException("No pending requests"));
                            callback.OnDataFetched(result);
                            return;
                        }

                        Request request = null;
                        String lastId = null;
                        for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                            lastId = queryDocumentSnapshot.getId();
                            if (!loggedInUser.getRequestsDiagnosed().contains(queryDocumentSnapshot.getId())) {
                                request = queryDocumentSnapshot.toObject(Request.class);
                                break;
                            }
                        }

                        if (request == null) {
                            this.getNewRequest(loggedInUser, lastId, callback);
                            return;
                        }

                        request.setReceiver(loggedInUser.getUid());
                        Request finalRequest = request;
                        db.collection("requests")
                                .document(finalRequest.getId())
                                .set(finalRequest.toMap(), SetOptions.merge())
                                .addOnSuccessListener(documentReference -> {
                                    callback.OnDataFetched(new Result.Success<>(finalRequest));
                                    updateUserRequestsAssigned(loggedInUser);
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


    /**
     * Increments by 1 the number of requests the given user had has assigned from the beginning of the times
     *
     * @param loggedInUser Logged user
     */
    private void updateUserRequestsAssigned(LoggedInUser loggedInUser) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(loggedInUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        //this must never happen
                        return;
                    }

                    long actualValue = 0;
                    if (documentSnapshot.contains("requestsAssigned")) {
                        actualValue = (long) documentSnapshot.get("requestsAssigned");
                    }

                    Map<String, Object> mapping = new HashMap<>();
                    mapping.put("requestsAssigned", actualValue + 1);

                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(loggedInUser.getUid())
                            .update(mapping);

                });
    }
}
