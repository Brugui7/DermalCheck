package com.brugui.dermalcheck.data.request;

import android.net.Uri;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.interfaces.OnDataFetched;
import com.brugui.dermalcheck.data.interfaces.OnRequestCreated;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Rol;
import com.brugui.dermalcheck.data.model.Status;
import com.brugui.dermalcheck.utils.PrivateConstants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
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

public class NewRequestDataSource {
    private static final String TAG = "Logger NewRequestDS";
    private Result<Request> result;


    public void fetchRequestsNumber(OnDataFetched callback) {
        try {


            FirebaseFirestore.getInstance().document("statistics/0")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshot -> {
                        callback.OnDataFetched(new Result.Success<>(queryDocumentSnapshot.get("requestsCreated")));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, e.getMessage(), e);
                        callback.OnDataFetched(new Result.Error(new IOException("Error retrieving requests number")));
                    });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            callback.OnDataFetched(new Result.Error(new IOException("Error", e)));
        }
    }

    public void sendRequest(Request request, ArrayList<Uri> images, OnRequestCreated onRequestCreated) {
        try {
            DocumentReference ref = FirebaseFirestore.getInstance()
                    .collection("requests")
                    .document();

            request.setId(ref.getId());
            Map<String, Object> mapping = request.toMap();
            mapping.put("creationDate", FieldValue.serverTimestamp());
            mapping.put("random", Math.random());
            Log.d(TAG, mapping.toString());


            //Sends the request
            FirebaseFirestore.getInstance()
                    .collection("requests")
                    .document(ref.getId())
                    .set(mapping)
                    .addOnSuccessListener(documentReference -> {
                        result = new Result.Success<>(request);

                        if (images != null) {
                            //this should always be true, but just to ensure
                            this.uploadImages(request, images);
                            this.updateStatistics(request);
                            Result result = new Result.Success(prepareNotification(request));
                            onRequestCreated.onRequestCreated(result);
                        }

                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, e.getMessage(), e);
                        onRequestCreated.onRequestCreated(new Result.Error(new IOException("Error creating Request", e)));
                    });

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result = new Result.Error(new IOException("Error creating Request", e));
            onRequestCreated.onRequestCreated(result);
        }
    }

    private void updateStatistics(Request newRequest) {
        // Updates the general statistics
        FirebaseFirestore.getInstance()
                .document("statistics/0")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        //this must never happen
                        return;
                    }

                    Map<String, Object> mapping = new HashMap<>();
                    mapping.put("requestsCreated", ((long) documentSnapshot.get("requestsCreated")) + 1);
                    if (newRequest.getDiagnosedLabelIndex() == newRequest.getLabelIndex()) {
                        mapping.put("matchingDiagnostics", ((long) documentSnapshot.get("matchingDiagnostics")) + 1);
                    }

                    FirebaseFirestore.getInstance()
                            .document("statistics/0")
                            .update(mapping);

                });

        // Updates the aggregated statistics of amount of requests by diagnostics
        FirebaseFirestore.getInstance()
                .document("statistics/requestsByLabelIndex")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        //this must never happen
                        return;
                    }

                    Map<String, Object> mapping = new HashMap<>();
                    String labelIndexString = String.valueOf(newRequest.getDiagnosedLabelIndex());
                    if (!documentSnapshot.contains(labelIndexString)) {
                        mapping.put(labelIndexString, 1);
                    } else {
                        mapping.put(labelIndexString, (long) documentSnapshot.get(labelIndexString) + 1);
                    }

                    FirebaseFirestore.getInstance()
                            .document("statistics/requestsByLabelIndex")
                            .update(mapping);

                });
    }

    private void uploadImages(Request request, ArrayList<Uri> images) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        for (Uri image : images) {
            StorageReference imageRef = storageReference.child("images/" + request.getId() + "/" + image.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(image);
            uploadTask.addOnSuccessListener(result -> {
                Task<Uri> downloadUrl = imageRef.getDownloadUrl();
                downloadUrl.addOnSuccessListener(url -> {
                    Log.d(TAG, "Url: " + url.toString());
                    updateImageDatabase(request, url.toString());
                });
            });
            uploadTask.addOnFailureListener(e -> {
                Log.e(TAG, e.getMessage(), e);
            });
        }
    }

    private void updateImageDatabase(Request request, String url) {
        Map<String, String> document = new HashMap<>();
        document.put("remoteUri", url);
        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(request.getId())
                .collection("images")
                .add(document);

    }

    /**
     * @param request Request
     * @return request notification http request
     */
    private JsonObjectRequest prepareNotification(Request request) {
        String title = "Nueva consulta recibida";
        String message = "Te han asignado una nueva consulta.";

        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            Gson gson = new Gson();
            notifcationBody
                    .put("title", title)
                    .put("message", message)
                    .put("request", gson.toJson(request));

            notification.put("to", "/topics/" + request.getReceiver());
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage());
        }

        return new JsonObjectRequest(
                "https://fcm.googleapis.com/fcm/send",
                notification,
                response -> Log.i(TAG, "onResponse: " + response.toString()),
                error -> Log.e(TAG, error.getMessage() + " " + error.getMessage(), error)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + PrivateConstants.CM_SERVER_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
    }


}
