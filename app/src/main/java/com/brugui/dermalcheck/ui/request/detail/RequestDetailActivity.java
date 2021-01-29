package com.brugui.dermalcheck.ui.request.detail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.RequestDetailDataSource;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.interfaces.OnRequestCreated;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.ui.MainActivity;
import com.brugui.dermalcheck.ui.components.ImageDetailActivity;
import com.brugui.dermalcheck.ui.components.snackbar.CustomSnackbar;
import com.brugui.dermalcheck.utils.NotificationRequestsQueue;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

public class RequestDetailActivity extends AppCompatActivity {

    private static final String TAG = "Logger RequestDetAc";
    public static final String IMAGES_ARRAY = "IMAGES_ARRAY";
    public static final String REQUEST = "REQUEST";

    private TextView tvEstimatedProbability, tvLabel;
    private RequestDetailDataSource dataSource;
    private Request request;
    private DonutProgressView dpvChart;
    private ConstraintLayout clContainer;
    private Button btnSendRequest, btnCancel;
    private EditText etPhototype, etPatientId, etNotes;
    private CheckBox chPersonalAntecedents, chFamiliarAntecedents;
    private ImageView ivImage;

    private ArrayList<Uri> images;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        setTitle(R.string.request_detail);
        clContainer = findViewById(R.id.clContainer);

        btnSendRequest = findViewById(R.id.btnSendRequest);
        btnCancel = findViewById(R.id.btnCancel);
        dpvChart = findViewById(R.id.dpvChart);
        tvEstimatedProbability = findViewById(R.id.tvEstimatedProbability);
        tvLabel = findViewById(R.id.tvLabel);
        ivImage = findViewById(R.id.ivImage);

        chFamiliarAntecedents = findViewById(R.id.chFamiliarAntecedents);
        chPersonalAntecedents = findViewById(R.id.chPersonalAntecedents);
        etPhototype = findViewById(R.id.etPhototype);
        etPatientId = findViewById(R.id.etPatientId);
        etNotes = findViewById(R.id.etNotes);


        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            finish();
            return;
        }

        request = (Request) bundle.getSerializable(REQUEST);

        if (bundle.getSerializable(IMAGES_ARRAY) != null) {
            //Creation
            images = bundle.getParcelableArrayList(IMAGES_ARRAY);
            setUpCreationUI();
        } else {
            //Consultation
            //todo getImages
        }

        setFormValues();
        setChartValues();

        dataSource = new RequestDetailDataSource();
    }

    private void setChartValues() {
        float estimatedProbability = (float) request.getEstimatedProbability();
        tvEstimatedProbability.setText(estimatedProbability + "%");
        int color = Color.parseColor("#f44336");
        if (estimatedProbability < 30) {
            color = Color.parseColor("#4caf50");
        } else if (estimatedProbability < 70) {
            color = Color.parseColor("#ff9800");
        }
        DonutSection section = new DonutSection("", color, estimatedProbability);
        dpvChart.setCap(100f);
        dpvChart.submitData(new ArrayList<>(Collections.singleton(section)));
    }

    private void setFormValues(){
        chFamiliarAntecedents.setChecked(request.isFamiliarAntecedents());
        chPersonalAntecedents.setChecked(request.isPersonalAntecedents());
        etPhototype.setText(String.valueOf(request.getPhototype()));
        etPatientId.setText(request.getPatientId());
        etNotes.setText(request.getNotes());

        if (request.getLabel() != 0){
            tvLabel.setText(getString(request.getLabel()));
        }
        if (images != null && images.size() > 0){
            ivImage.setImageURI(images.get(0));
            ivImage.setOnClickListener(listenerIvImage);
        }
    }

    private void setUpCreationUI(){

        btnCancel.setOnClickListener(view -> finish());
        btnSendRequest.setOnClickListener(listenerBtnSendRequest);
        btnSendRequest.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
    }
    //########## Listeners ##########


    private final OnRequestCreated onRequestCreated = result -> {
        if (result instanceof Result.Error) {
            btnSendRequest.setEnabled(true);
            CustomSnackbar.make(clContainer, getString(R.string.error_creating_request),
                    Snackbar.LENGTH_SHORT,
                    null,
                    R.drawable.ic_error_outline,
                    null,
                    getColor(R.color.accent)
            ).show();
            return;
        }

        //Sends the notification to the receiver
        NotificationRequestsQueue
                .getInstance(getApplicationContext())
                .addToRequestQueue(((Result.Success<JsonObjectRequest>) result).getData());


        Objects.requireNonNull(CustomSnackbar.make(clContainer,
                getString(R.string.request_created_successfully),
                BaseTransientBottomBar.LENGTH_SHORT,
                null,
                R.drawable.ic_check_circle_outline,
                null,
                getColor(R.color.success)
        ))
                .addCallback(new BaseTransientBottomBar.BaseCallback<CustomSnackbar>() {
                    @Override
                    public void onDismissed(CustomSnackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        Intent intent = new Intent(RequestDetailActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .show();

    };

    private final View.OnClickListener listenerBtnSendRequest = view -> {
        btnSendRequest.setEnabled(false);
        dataSource.sendRequest(request, images, onRequestCreated);
    };

    private final View.OnClickListener listenerIvImage = view -> {
        Intent intent = new Intent(RequestDetailActivity.this, ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMAGE_URI, images.get(0));
        startActivity(intent);
    };
}