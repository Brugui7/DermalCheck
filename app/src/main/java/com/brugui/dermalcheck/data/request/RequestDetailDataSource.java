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

    public void sendRequest(Request request, ArrayList<Uri> images, OnRequestCreated onRequestCreated) {
        try {
            DocumentReference ref = db.collection("requests").document();
            request.setId(ref.getId());
            Map<String, Object> mapping = request.toMap();
            mapping.put("creationDate", FieldValue.serverTimestamp());
            Log.d(TAG, mapping.toString());


            //gets the request receiver
            Task<QuerySnapshot> receiverTask = fetchOptimalReceiver();
            if (receiverTask == null) {
                Log.e(TAG, "Error obtaining request receiver");
                result = new Result.Error(new IOException("Error obtaining request receiver"));
                onRequestCreated.OnRequestCreated(result);
                return;
            }


            receiverTask.addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.size() == 0) {
                    result = new Result.Error(new IOException("No valid receivers"));
                    onRequestCreated.OnRequestCreated(result);
                }

                String receiverId = queryDocumentSnapshots.iterator().next().getId();
                mapping.put("receiver", receiverId);

                //Sends the request
                db.collection("requests")
                        .document(ref.getId())
                        .set(mapping)
                        .addOnSuccessListener(documentReference -> {
                            result = new Result.Success<>(request);

                            if (images != null) {
                                //this should always be true, but just to ensure
                                this.uploadImages(request, images);
                                this.updateReceiverPendingRequests(receiverId, INCREASE);
                                Result result = new Result.Success(prepareNotification(request));
                                onRequestCreated.OnRequestCreated(result);
                            }

                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, e.getMessage(), e);
                            result = new Result.Error(new IOException("Error creating Request", e));
                            onRequestCreated.OnRequestCreated(result);
                        });
            }).addOnFailureListener(e -> {
                Log.e(TAG, e.getMessage(), e);
                result = new Result.Error(new IOException("Error obtaining request receiver", e));
                onRequestCreated.OnRequestCreated(result);
            });
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            result = new Result.Error(new IOException("Error creating Request", e));
            onRequestCreated.OnRequestCreated(result);
        }
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
        db.collection("requests")
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
}