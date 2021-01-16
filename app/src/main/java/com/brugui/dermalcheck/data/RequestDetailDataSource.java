package com.brugui.dermalcheck.data;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
import com.brugui.dermalcheck.utils.NotificationRequestsQueue;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that handles the creation an retrieving requests
 */
public class RequestDetailDataSource {

    private Result<Request> result;
    private static final String TAG = "Logger RequestDetDS";
    private FirebaseFirestore db;

    public RequestDetailDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    //TODO callback
    public Result<Request> sendRequest(Request request, ArrayList<Uri> images) {
        try {
            DocumentReference ref = db.collection("requests").document();
            request.setId(ref.getId());
            Map<String, Object> mapping = request.toMap();
            mapping.put("creationDate", FieldValue.serverTimestamp());

            db.collection("requests").document(ref.getId())
                    .set(mapping)
                    .addOnSuccessListener(documentReference -> {
                        result = new Result.Success<>(request);
                        if (images != null){
                            //this should always be true, but just to ensure
                            this.uploadImages(request, images);
                        }

                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, e.getMessage(), e);
                        result = new Result.Error(new IOException("Error creating Request", e));
                    });
            return  result;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return new Result.Error(new IOException("Error", e));
        }
    }

    private void uploadImages(Request request, ArrayList<Uri> images){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        for (Uri image : images){
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




    /*public void retreive() {
//todo
    }*/
}