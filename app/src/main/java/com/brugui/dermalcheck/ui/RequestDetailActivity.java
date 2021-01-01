package com.brugui.dermalcheck.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.RequestDetailDataSource;
import com.brugui.dermalcheck.data.model.Request;

public class RequestDetailActivity extends AppCompatActivity {

    public static final String IMAGES_ARRAY = "IMAGES_ARRAY";
    public static final String REQUEST = "REQUEST";
    private Button btnSendRequest, btnCancel;
    private RequestDetailDataSource dataSource;
    private Request request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        btnCancel = findViewById(R.id.btnCancel);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            request = (Request) bundle.getSerializable(REQUEST);
        }

        btnCancel.setOnClickListener(view -> {finish();});
        btnSendRequest.setOnClickListener(listenerBtnSendRequest);
        dataSource = new RequestDetailDataSource();
    }

    private View.OnClickListener listenerBtnSendRequest = view -> {
        dataSource.sendRequest(request);
    };


}