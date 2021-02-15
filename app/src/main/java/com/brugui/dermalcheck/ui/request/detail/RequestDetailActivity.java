package com.brugui.dermalcheck.ui.request.detail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.toolbox.JsonObjectRequest;
import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.model.Status;
import com.brugui.dermalcheck.data.request.RequestDetailDataSource;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.interfaces.OnRequestCreated;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Rol;
import com.brugui.dermalcheck.ui.MainActivity;
import com.brugui.dermalcheck.ui.components.ImageDetailActivity;
import com.brugui.dermalcheck.ui.components.snackbar.CustomSnackbar;
import com.brugui.dermalcheck.utils.NotificationRequestsQueue;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

public class RequestDetailActivity extends AppCompatActivity {

    private static final String TAG = "Logger RequestDetAc";
    public static final String IMAGES_ARRAY = "IMAGES_ARRAY";
    public static final String REQUEST = "REQUEST";

    private TextView tvEstimatedProbability, tvLabel;
    private Request request;
    private DonutProgressView dpvChart;
    private ConstraintLayout clContainer;
    private Button btnDiagnose, btnUpdateData;
    private EditText etPhototype, etPatientId, etNotes, etAge;
    private RadioGroup rgSex;
    private CheckBox chPersonalAntecedents, chFamiliarAntecedents;
    private ImageView ivImage;
    private RequestDetailViewModel requestDetailViewModel;

    //For visualization
    private List<String> imageUrls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        setTitle(R.string.request_detail);
        clContainer = findViewById(R.id.clContainer);

        btnDiagnose = findViewById(R.id.btnDiagnose);
        btnUpdateData = findViewById(R.id.btnUpdateData);
        dpvChart = findViewById(R.id.dpvChart);
        tvEstimatedProbability = findViewById(R.id.tvEstimatedProbability);
        tvLabel = findViewById(R.id.tvLabel);
        ivImage = findViewById(R.id.ivImage);

        chFamiliarAntecedents = findViewById(R.id.chFamiliarAntecedents);
        chPersonalAntecedents = findViewById(R.id.chPersonalAntecedents);
        etPhototype = findViewById(R.id.etPhototype);
        etPatientId = findViewById(R.id.etPatientId);
        etNotes = findViewById(R.id.etNotes);
        etAge = findViewById(R.id.etAge);
        rgSex = findViewById(R.id.rgSex);

        requestDetailViewModel = new RequestDetailViewModel();
        requestDetailViewModel.loadUserData(this);


        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            finish();
            return;
        }

        request = (Request) bundle.getSerializable(REQUEST);

        requestDetailViewModel.fetchImages(request.getId());
        requestDetailViewModel.getImages().observe(this, images -> {
            imageUrls = images;
            if (imageUrls == null || imageUrls.size() == 0) {
                return;
            }
            Glide.with(this).load(imageUrls.get(0)).into(ivImage);
            ivImage.setOnClickListener(listenerIvImageFromUrl);
        });

        LoggedInUser loggedInUser = requestDetailViewModel.getUserLogged();
        if (loggedInUser != null && loggedInUser.getRole() != null) {
            if (loggedInUser.getRole().equalsIgnoreCase(Rol.SPECIALIST_ROL)) {
                setUpDiagnosticUI();
            }
        }

        btnUpdateData.setOnClickListener(listenerBtnUpdateData);

        setFormValues();
        setChartValues();
    }


    //########## Init Functions ##########

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

    private void setFormValues() {
        chFamiliarAntecedents.setChecked(request.isFamiliarAntecedents());
        chPersonalAntecedents.setChecked(request.isPersonalAntecedents());
        if (request.getPhototype() != -1) {
            etPhototype.setText(String.valueOf(request.getPhototype()));
        }

        if (request.getAge() != -1) {
            etAge.setText(String.valueOf(request.getAge()));
        }

        etPatientId.setText(request.getPatientId());
        etNotes.setText(request.getNotes());
        if (request.getSex() != null) {
            if (request.getSex().equals("male")) {
                rgSex.check(R.id.rbMale);
            } else {
                rgSex.check(R.id.rbFemale);
            }
        }

        if (request.getLabel() != 0) {
            tvLabel.setText(getString(request.getLabel()));
        }
    }

    private void setUpDiagnosticUI() {
        btnDiagnose.setVisibility(View.VISIBLE);
    }

    //TODO formstate y al viewmodel
    private boolean validateInput() {
        etPatientId.setError(null);
        etPhototype.setError(null);
        etAge.setError(null);

        if (etPatientId.getText().toString().trim().length() == 0) {
            etPatientId.setError(getString(R.string.required_field));
            etPatientId.requestFocus();
            return false;
        }

        String stringAge = etAge.getText().toString().trim();
        if (stringAge.length() == 0) {
            etAge.setError(getString(R.string.required_field));
            etAge.requestFocus();
            return false;
        }

        String stringPhototype = etPhototype.getText().toString().trim();
        if (stringPhototype.length() > 0) {
            try {
                int phototype = Integer.parseInt(stringPhototype);
                if (phototype < 1 || phototype > 5) {
                    etPhototype.setError(getString(R.string.invalid_phototype));
                    etPhototype.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                etPhototype.setError(getString(R.string.invalid_phototype));
                etPhototype.requestFocus();
                return false;
            }
        }

        return true;
    }

    //########## Listeners ##########

    private final View.OnClickListener listenerIvImageFromUrl = view -> {
        Intent intent = new Intent(RequestDetailActivity.this, ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMAGE_URL, imageUrls.get(0));
        startActivity(intent);
    };

    private View.OnClickListener listenerBtnUpdateData = view -> {
        if (!validateInput()) {
            return;
        }

        request.setPatientId(etPatientId.getText().toString());
        if (etAge.getText().toString().length() > 0) {
            request.setAge(Integer.parseInt(etAge.getText().toString()));
        }

        if (rgSex.getCheckedRadioButtonId() != -1) {
            request.setSex(rgSex.getCheckedRadioButtonId() == R.id.rbMale ? "male" : "female");
        }

        if (etPhototype.getText().toString().length() > 0) {
            request.setPhototype(Integer.parseInt(etPhototype.getText().toString()));
        }

        request.setFamiliarAntecedents(chFamiliarAntecedents.isChecked());
        request.setPersonalAntecedents(chPersonalAntecedents.isChecked());
        request.setNotes(etNotes.getText().toString());

        requestDetailViewModel.updateRequest(request, result -> {
            if (result instanceof Result.Error) {
                CustomSnackbar.make(clContainer, getString(R.string.error_creating_request),
                        Snackbar.LENGTH_SHORT,
                        null,
                        R.drawable.ic_error_outline,
                        null,
                        getColor(R.color.accent)
                ).show();
                return;
            }

            CustomSnackbar.make(clContainer, getString(R.string.request_updated_successfully),
                    Snackbar.LENGTH_SHORT,
                    null,
                    R.drawable.ic_check_circle_outline,
                    null,
                    getColor(R.color.success)
            ).show();

        });

        /*
         newRequest = new Request(
                imageSelected.getEstimatedProbability(),
                Integer.parseInt(etAge.getText().toString()),
                sex,
                chFamiliarAntecedents.isChecked(),
                chPersonalAntecedents.isChecked(),
                Integer.parseInt(etPhototype.getText().toString()),
                etNotes.getText().toString(),
                etPatientId.getText().toString(),
                userLogged.getUserId(),
                userLogged.getUserId(), //TODO receiver
                null, //automatically assigned before sending
                Status.PENDING_STATUS_NAME,
                Calendar.getInstance().getTime(),
                imageSelected.getLabel()
        );
        * */
    };
}