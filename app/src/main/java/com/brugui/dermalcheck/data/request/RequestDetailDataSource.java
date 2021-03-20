package com.brugui.dermalcheck.data.request;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.interfaces.OnDataFetched;
import com.brugui.dermalcheck.data.interfaces.OnRequestCreated;
import com.brugui.dermalcheck.data.interfaces.OnRequestUpdated;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Rol;
import com.brugui.dermalcheck.data.model.Status;
import com.brugui.dermalcheck.utils.NotificationRequestsQueue;
import com.brugui.dermalcheck.utils.PrivateConstants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that handles the creation an retrieving requests
 */
public class RequestDetailDataSource {

    private Result<Request> result;
    private static final String TAG = "Logger RequestDetDS";
    private FirebaseFirestore db;
    private static final int INCREASE = 1;
    private static final int DECREASE = -1;

    public RequestDetailDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    public void fetchImages(String requestId, OnDataFetched callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("requests/" + requestId + "/images")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> images = new ArrayList<>();
                    for (DocumentSnapshot image : queryDocumentSnapshots) {
                        images.add(image.getString("remoteUri"));
                    }
                    result = new Result.Success<>(images);
                    callback.OnDataFetched(result);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, e.getMessage(), e);
                    result = new Result.Error(new IOException("Error retrieving requests"));
                    callback.OnDataFetched(result);
                });
    }


    /**
     * Gets the user id with less pending requests assigned
     */
    private Task<QuerySnapshot> fetchOptimalReceiver() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            return db.collection("users")
                    .whereEqualTo("role", Rol.SPECIALIST_ROL)
                    .orderBy("pendingRequests", Query.Direction.ASCENDING)
                    .limit(1)
                    .get();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Increases by one the user pending requests
     *
     * @param receiverId String
     * @param mode       INCREASE | DECREASE
     */
    private void updateReceiverPendingRequests(String receiverId, int mode) {

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(receiverId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        //this must never happen
                        return;
                    }
                    long actualValue = 0;
                    if (documentSnapshot.get("pendingRequests") != null) {
                        actualValue = (long) documentSnapshot.get("pendingRequests");
                    }

                    Map<String, Object> mapping = new HashMap<>();
                    mapping.put("pendingRequests", actualValue + mode);
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(receiverId)
                            .update(mapping);

                });
    }

    /**
     * Adds the diagnosed request to the user diagnosed requests array
     *
     * @param user    LoggedInUser
     */
    public void updateUserData(LoggedInUser user) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("requestsDiagnosed", user.getRequestsDiagnosed());
        map.put("matchingRequestsDiagnosed", user.getMatchingRequestsDiagnosed());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .update(map);

    }

    /**
     * @param request          Request
     * @param onRequestUpdated OnRequestUpdated
     */
    public void updateRequest(Request request, OnRequestUpdated onRequestUpdated) {
        try {
            Map<String, Object> mapping = request.toMap();
            //Sends the request
            db.collection("requests")
                    .document(request.getId())
                    .set(mapping, SetOptions.merge())
                    .addOnSuccessListener(documentReference -> {
                        onRequestUpdated.onRequestUpdated(new Result.Success<>(request));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, e.getMessage(), e);
                        onRequestUpdated.onRequestUpdated(new Result.Error(new IOException("Error updating request", e)));
                    });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result = new Result.Error(new IOException("Error updating Request", e));
            onRequestUpdated.onRequestUpdated(result);
        }
    }

    private void updateRequestsDiagnosedStatistic(Request request) {
        FirebaseFirestore.getInstance()
                .document("statistics/0")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        //this must never happen
                        return;
                    }

                    Map<String, Object> mapping = new HashMap<>();
                    //Requests diagnosed
                    long requestsDiagnosed = 0;
                    if (documentSnapshot.contains("requestsDiagnosed")) {
                        requestsDiagnosed = (long) documentSnapshot.get("requestsDiagnosed");
                    }
                    requestsDiagnosed++;

                    //Matching diagnostics
                    if (request.getDiagnosedLabelIndex() == request.getPathologistDiagnosticLabelIndex()) {
                        long matchingDiagnostics = 0;
                        if (documentSnapshot.contains("matchingDiagnostics")) {
                            matchingDiagnostics = (long) documentSnapshot.get("matchingDiagnostics");
                        }
                        mapping.put("matchingDiagnostics", matchingDiagnostics + 1);
                    }

                    //Average diagnose time in hours
                    long hoursDiff = request.getDiagnosticDate().getTime() - request.getCreationDate().getTime();
                    hoursDiff /= (60 * 60 * 1000);

                    double oldAverageDiagnoseHours = 0;

                    if (documentSnapshot.contains("averageDiagnoseHours")) {
                        oldAverageDiagnoseHours = (double) documentSnapshot.get("averageDiagnoseHours");
                    }
                    double averageDiagnoseHours = (oldAverageDiagnoseHours * (requestsDiagnosed - 1) + hoursDiff) / requestsDiagnosed;

                    //Standard Deviation diagnose time in hours
                    double stdDiagnoseHours = 0;
                    if (documentSnapshot.contains("stdDiagnoseHours")) {
                        stdDiagnoseHours = (double) documentSnapshot.get("stdDiagnoseHours");
                    }

                    stdDiagnoseHours = getNewStd(hoursDiff, requestsDiagnosed, stdDiagnoseHours, oldAverageDiagnoseHours, averageDiagnoseHours);


                    mapping.put("requestsDiagnosed", requestsDiagnosed);
                    mapping.put("averageDiagnoseHours", averageDiagnoseHours);
                    mapping.put("stdDiagnoseHours", stdDiagnoseHours);

                    FirebaseFirestore.getInstance()
                            .document("statistics/0")
                            .update(mapping);

                });
    }

    /**
     * https://www.johndcook.com/blog/standard_deviation/
     *
     * @param x       new value
     * @param oldStd  old std
     * @param oldMean old mean
     * @param newMean new mean
     * @return new Standard Deviation
     */
    private double getNewStd(double x, long n, double oldStd, double oldMean, double newMean) {
        return Math.sqrt((oldStd + (x - oldMean) * (x - newMean)) / (n - 1));
    }

}