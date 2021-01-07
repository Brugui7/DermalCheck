package com.brugui.dermalcheck.ui.request.detail;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.RequestDetailDataSource;
import com.brugui.dermalcheck.data.model.Request;


import java.util.ArrayList;
import java.util.Collections;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

public class RequestDetailActivity extends AppCompatActivity {

    private static final String TAG = "Logger RequestDetAc";
    public static final String IMAGES_ARRAY = "IMAGES_ARRAY";
    public static final String REQUEST = "REQUEST";
    private Button btnSendRequest, btnCancel;
    private TextView tvEstimatedProbability;
    private RequestDetailDataSource dataSource;
    private Request request;
    private DonutProgressView dpvChart;

    private ArrayList<Uri> images;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        setTitle(R.string.request_detail);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        btnCancel = findViewById(R.id.btnCancel);
        dpvChart = findViewById(R.id.dpvChart);
        tvEstimatedProbability = findViewById(R.id.tvEstimatedProbability);



        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            request = (Request) bundle.getSerializable(REQUEST);
            images = bundle.getParcelableArrayList(IMAGES_ARRAY);
        }

        setValues();

        btnCancel.setOnClickListener(view -> {finish();});
        btnSendRequest.setOnClickListener(listenerBtnSendRequest);
        dataSource = new RequestDetailDataSource();
    }

    private void setValues(){
        float estimatedProbability = (float)request.getEstimatedProbability();
        tvEstimatedProbability.setText(estimatedProbability + "%");
        int color = Color.parseColor("#f44336");
        if (estimatedProbability < 30) {
            color  = Color.parseColor("#4caf50");
        } else if (estimatedProbability < 70) {
            color  = Color.parseColor("#ff9800");
        }
        DonutSection section = new DonutSection("", color, estimatedProbability);
        dpvChart.setCap(100f);
        dpvChart.submitData(new ArrayList<>(Collections.singleton(section)));
    }

    //########## Listeners ##########
    private final View.OnClickListener listenerBtnSendRequest = view -> {
        dataSource.sendRequest(request, images);
    };




}