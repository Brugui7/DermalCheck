package com.brugui.dermalcheck.data;

import android.util.Log;

import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.Map;

/**
 * Class that handles the creation an retrieving requests
 */
public class RequestDetailDataSource {

    private FirebaseAuth auth;
    private Result<Request> result;
    private static final String TAG = "Logger RequestDS";

    public Result<Request> sendRequest(Request request) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> mapping = request.toMap();
            mapping.put("creationDate", FieldValue.serverTimestamp());
            mapping.put("status", db.document("status/" + Status.PENDING_STATUS_REFERENCE));
            mapping.put("sender", db.document("users/" + request.getSender().getUserId()));

            db.collection("requests")
                    .add(mapping)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Todo ok " + documentReference.getId());
                        result = new Result.Success<>(request);
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



    /*public void retreive() {

    }*/
}