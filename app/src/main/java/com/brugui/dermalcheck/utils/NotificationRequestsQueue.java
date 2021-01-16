package com.brugui.dermalcheck.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


public class NotificationRequestsQueue {
    private  static NotificationRequestsQueue instance;
    private RequestQueue requestQueue;
    private Context ctx;

    private NotificationRequestsQueue(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized NotificationRequestsQueue getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationRequestsQueue(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() keeps it from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
