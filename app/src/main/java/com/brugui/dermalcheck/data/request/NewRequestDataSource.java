package com.brugui.dermalcheck.data.request;

import android.util.Log;

import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.interfaces.OnDataFetched;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Rol;
import com.brugui.dermalcheck.data.model.Status;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.List;

public class NewRequestDataSource {
    private static final String TAG = "Logger NewRequestDS";

    public void fetchRequestsNumber(OnDataFetched callback) {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.document("statistics/0")
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


}
