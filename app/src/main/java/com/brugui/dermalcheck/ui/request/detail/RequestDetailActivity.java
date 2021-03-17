package com.brugui.dermalcheck.ui.request.detail;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.interfaces.OnRequestUpdated;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Rol;
import com.brugui.dermalcheck.ui.adapters.PhototypeAdapter;
import com.brugui.dermalcheck.ui.components.ImageDetailActivity;
import com.brugui.dermalcheck.ui.components.snackbar.CustomSnackbar;
import com.brugui.dermalcheck.utils.Classifier;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

public class RequestDetailActivity extends AppCompatActivity {

    private static final String TAG = "Logger RequestDetAc";
    public static final String IMAGES_ARRAY = "IMAGES_ARRAY";
    public static final String REQUEST = "REQUEST";

    private TextView tvEstimatedProbability, tvLabel;
    private Request request;
    private DonutProgressView dpvChart;
    private Spinner spDiagnostics, spPathologistDiagnostic;
    private ConstraintLayout clContainer;
    private Button btnDiagnose;
    private EditText etPatientId, etNotes, etAge;
    private String selectedGender = null;
    private int selectedBodyPartIndex = 0;
    private boolean familiarAntecedents = false;
    private boolean personalAntecedents = false;
    private TextView tvSpecialistDiagnostic;
    private ImageView ivImage, ivGenderMale, ivGenderFemale, ivPersonalAntecedents, ivFamiliarAntecedents;
    private ArrayList<ImageView> bodyParts;
    private RequestDetailViewModel requestDetailViewModel;
    private RecyclerView rvPhototype;

    //For visualization
    private List<String> imageUrls;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);
        setTitle(R.string.request_detail);
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            finish();
            return;
        }

        clContainer = findViewById(R.id.clContainer);
        spPathologistDiagnostic = findViewById(R.id.spPathologistDiagnostic);
        spDiagnostics = findViewById(R.id.spDiagnostics);
        btnDiagnose = findViewById(R.id.btnDiagnose);
        Button btnUpdateData = findViewById(R.id.btnUpdateData);
        dpvChart = findViewById(R.id.dpvChart);
        tvEstimatedProbability = findViewById(R.id.tvEstimatedProbability);
        tvLabel = findViewById(R.id.tvLabel);
        ivImage = findViewById(R.id.ivImage);
        rvPhototype = findViewById(R.id.rvPhototype);
        etPatientId = findViewById(R.id.etPatientId);
        etNotes = findViewById(R.id.etNotes);
        etAge = findViewById(R.id.etAge);
        tvSpecialistDiagnostic = findViewById(R.id.tvSpecialistDiagnostic);
        ivGenderMale = findViewById(R.id.ivGenderMale);
        ivGenderFemale = findViewById(R.id.ivGenderFemale);
        ivGenderMale.setOnClickListener(listenerIvGender);
        ivGenderFemale.setOnClickListener(listenerIvGender);
        ivPersonalAntecedents = findViewById(R.id.ivPersonalAntecedents);
        ivFamiliarAntecedents = findViewById(R.id.ivFamiliarAntecedents);
        ivPersonalAntecedents.setOnClickListener(listenerIvPersonalAntecedents);
        ivFamiliarAntecedents.setOnClickListener(listenerIvFamiliarAntecedents);

        // Body parts
        ImageView ivNeck = findViewById(R.id.ivNeck);
        ImageView ivFace = findViewById(R.id.ivFace);
        ImageView ivAbdomen = findViewById(R.id.ivAbdomen);
        ImageView ivBack = findViewById(R.id.ivBack);
        ImageView ivChest = findViewById(R.id.ivChest);
        ImageView ivEar = findViewById(R.id.ivEar);
        ImageView ivFoot = findViewById(R.id.ivFoot);
        ImageView ivGenitals = findViewById(R.id.ivGenitals);
        ImageView ivHand = findViewById(R.id.ivHand);
        ImageView ivLowerExtremity = findViewById(R.id.ivLowerExtremity);
        ImageView ivScalp = findViewById(R.id.ivScalp);
        ImageView ivUpperExtremity = findViewById(R.id.ivUpperExtremity);
        bodyParts = new ArrayList<>(Arrays.asList(null, ivBack, ivLowerExtremity, ivUpperExtremity, ivAbdomen, ivChest, ivScalp, ivFace, ivEar, ivNeck, ivHand, ivFoot, ivGenitals));
        for (ImageView part : bodyParts) {
            if (part == null) continue;
            part.setOnClickListener(listenerBodyParts);
        }

        rvPhototype.setAdapter(new PhototypeAdapter());
        requestDetailViewModel = new RequestDetailViewModel();
        requestDetailViewModel.loadUserData(this);

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
        spDiagnostics.setOnItemSelectedListener(listenerSpDiagnostics);

        setFormValues();
        if (request.getDiagnosedLabelIndex() != -1) {
            setEstimatedDiagnosticValues();
            btnDiagnose.setVisibility(View.GONE);
        }
        btnDiagnose.setOnClickListener(view -> spDiagnostics.performClick());

    }


    //########## Init Functions ##########

    private void setEstimatedDiagnosticValues() {
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
        if (request.getLabelIndex() != -1) {
            tvLabel.setText(Classifier.LABELS[request.getLabelIndex()]);
        }
        tvLabel.setVisibility(View.VISIBLE);
        dpvChart.setVisibility(View.VISIBLE);
        tvEstimatedProbability.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the form values based on the request obtained
     */
    private void setFormValues() {
        if (request.isFamiliarAntecedents()) {
            ivFamiliarAntecedents.performClick();
        }

        if (request.isPersonalAntecedents()) {
            ivPersonalAntecedents.performClick();
        }

        if (request.getPhototype() != -1) {
            ((PhototypeAdapter) rvPhototype.getAdapter()).setPositionSelected(request.getPhototype() - 1);
        }

        if (request.getAge() != -1) {
            etAge.setText(String.valueOf(request.getAge()));
        }

        etPatientId.setText(request.getPatientId());
        etNotes.setText(request.getNotes());
        if (request.getSex() != null) {
            selectedGender = request.getSex();
            if (selectedGender.equals("male")) {
                ivGenderMale.performClick();
            } else {
                ivGenderFemale.performClick();
            }
        }

        if (request.getDiagnosedLabelIndex() != -1) {
            tvSpecialistDiagnostic.setText(getString(Classifier.LABELS[request.getDiagnosedLabelIndex()]));
        }

        // In theory this should work without the if, but to ensure...
        if (request.getPathologistDiagnosticLabelIndex() != -1) {
            spPathologistDiagnostic.setSelection(request.getPathologistDiagnosticLabelIndex());
        }
        if (request.getLocalizationIndex() != 0) {
            bodyParts.get(request.getLocalizationIndex()).performClick();
        }
    }

    private void setUpDiagnosticUI() {
        btnDiagnose.setVisibility(View.VISIBLE);
    }

    //TODO formstate y al viewmodel
    private boolean validateInput() {
        etPatientId.setError(null);
        // etPhototype.setError(null);
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

        return true;
    }

    //########## Listeners ##########

    private final View.OnClickListener listenerIvImageFromUrl = view -> {
        Intent intent = new Intent(RequestDetailActivity.this, ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMAGE_URL, imageUrls.get(0));
        startActivity(intent);
    };

    private final OnRequestUpdated onRequestUpdated = result -> {
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

    };


    private final AdapterView.OnItemSelectedListener listenerSpDiagnostics = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (i == 0) {
                return;
            }
            btnDiagnose.setEnabled(false);
            request.setDiagnosedLabelIndex(i - 1);
            tvSpecialistDiagnostic.setText(getString(Classifier.LABELS[request.getDiagnosedLabelIndex()]));
            requestDetailViewModel.diagnose(request, result -> {
                onRequestUpdated.onRequestUpdated(result);
                setEstimatedDiagnosticValues();
            });
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            Log.d(TAG, "Nothing selected");
        }
    };

    private final View.OnClickListener listenerBtnUpdateData = view -> {
        if (!validateInput()) {
            return;
        }

        request.setPatientId(etPatientId.getText().toString());
        if (etAge.getText().toString().length() > 0) {
            request.setAge(Integer.parseInt(etAge.getText().toString()));
        }

        if (selectedGender != null) {
            request.setSex(selectedGender);
        }

        request.setPathologistDiagnosticLabelIndex(spPathologistDiagnostic.getSelectedItemPosition() - 1);
        request.setLocalizationIndex(selectedBodyPartIndex);

        int phototypeIndex = ((PhototypeAdapter) rvPhototype.getAdapter()).getPositionSelected();
        if (phototypeIndex != -1) {
            request.setPhototype(phototypeIndex + 1);
        }

        request.setFamiliarAntecedents(familiarAntecedents);
        request.setPersonalAntecedents(personalAntecedents);
        request.setNotes(etNotes.getText().toString());

        requestDetailViewModel.updateRequest(request, onRequestUpdated);
    };

    private final View.OnClickListener listenerIvGender = view -> {
        if (view.getId() == R.id.ivGenderFemale) {
            ImageViewCompat.setImageTintList(ivGenderFemale, ColorStateList.valueOf(getColor(R.color.accent)));
            ImageViewCompat.setImageTintList(ivGenderMale, ColorStateList.valueOf(getColor(R.color.white)));
            selectedGender = "female";
            return;
        }
        ImageViewCompat.setImageTintList(ivGenderMale, ColorStateList.valueOf(getColor(R.color.accent)));
        ImageViewCompat.setImageTintList(ivGenderFemale, ColorStateList.valueOf(getColor(R.color.white)));
        selectedGender = "male";
    };

    private final View.OnClickListener listenerIvPersonalAntecedents = view -> {
        if (!personalAntecedents) {
            personalAntecedents = true;
            ImageViewCompat.setImageTintList(ivPersonalAntecedents, ColorStateList.valueOf(getColor(R.color.accent)));
            return;
        }
        personalAntecedents = false;
        ImageViewCompat.setImageTintList(ivPersonalAntecedents, ColorStateList.valueOf(getColor(R.color.white)));
    };

    private final View.OnClickListener listenerIvFamiliarAntecedents = view -> {
        if (!familiarAntecedents) {
            familiarAntecedents = true;
            ImageViewCompat.setImageTintList(ivFamiliarAntecedents, ColorStateList.valueOf(getColor(R.color.accent)));
            return;
        }
        familiarAntecedents = false;
        ImageViewCompat.setImageTintList(ivFamiliarAntecedents, ColorStateList.valueOf(getColor(R.color.white)));
    };

    private final View.OnClickListener listenerBodyParts = view -> {
        for (ImageView part : bodyParts) {
            if (part == null) continue;
            ImageViewCompat.setImageTintList(part, ColorStateList.valueOf(getColor(R.color.black)));
        }
        selectedBodyPartIndex = bodyParts.indexOf((ImageView) view);
        ImageViewCompat.setImageTintList((ImageView) view, ColorStateList.valueOf(getColor(R.color.accent)));


    };

}