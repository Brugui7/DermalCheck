package com.brugui.dermalcheck.ui.request.detail;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.interfaces.OnRequestUpdated;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Rol;
import com.brugui.dermalcheck.ui.MainActivity;
import com.brugui.dermalcheck.ui.adapters.PhototypeAdapter;
import com.brugui.dermalcheck.ui.components.ImageDetailActivity;
import com.brugui.dermalcheck.ui.components.snackbar.CustomSnackbar;
import com.brugui.dermalcheck.ui.login.LoginActivity;
import com.brugui.dermalcheck.ui.request.creation.NewRequestActivity;
import com.brugui.dermalcheck.utils.Classifier;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

public class RequestDetailActivity extends AppCompatActivity {

    private static final String TAG = "Logger RequestDetAc";
    public static final String IMAGES_ARRAY = "IMAGES_ARRAY";
    public static final String REQUEST = "REQUEST";

    private TextView tvEstimatedProbability, tvLabel;
    private Request request;
    private DonutProgressView dpvChart;
    private Spinner spPathologistDiagnostic;
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
    private NumberPicker npDiagnostics;

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
        btnDiagnose = findViewById(R.id.btnDiagnose);
        Button btnUpdateData = findViewById(R.id.btnUpdateData);
        LinearLayout llExtraData = findViewById(R.id.llExtraData);
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
        npDiagnostics = findViewById(R.id.npDiagnostics);
        bodyParts = new ArrayList<>(Arrays.asList(null, ivBack, ivLowerExtremity, ivUpperExtremity, ivAbdomen, ivChest, ivScalp, ivFace, ivEar, ivNeck, ivHand, ivFoot, ivGenitals));
        for (ImageView part : bodyParts) {
            if (part == null) continue;
            part.setOnClickListener(listenerBodyParts);
        }

        rvPhototype.setAdapter(new PhototypeAdapter());
        requestDetailViewModel = new RequestDetailViewModel(getApplication());
        requestDetailViewModel.loadUserData();

        request = (Request) bundle.getSerializable(REQUEST);

        requestDetailViewModel.fetchImages(request.getId());
        requestDetailViewModel.getImages().observe(this, images -> {
            imageUrls = images;
            if (imageUrls == null || imageUrls.size() == 0) {
                return;
            }
            Glide.with(this).load(imageUrls.get(0)).into(ivImage);
            float rotation =  new Random().nextFloat();

            // Pseudo random rotation
            if (rotation < 0.3){
                ivImage.setRotation(0f);
            } else if (rotation < 0.7) {
                ivImage.setRotation(90f);
            } else {
                ivImage.setRotation(180f);
            }

            ivImage.setOnClickListener(listenerIvImageFromUrl);
        });

        LoggedInUser loggedInUser = requestDetailViewModel.getUserLogged();
        if (loggedInUser != null && loggedInUser.getRole() != null) {
            if (loggedInUser.getRole().equalsIgnoreCase(Rol.GENERAL_ROL)) {
                llExtraData.setVisibility(View.GONE);
                btnUpdateData.setVisibility(View.GONE);
                spPathologistDiagnostic.setEnabled(false);
                setUpDiagnosticUI();
            } else {
                setDiagnosticsValues();
            }
        }

        btnUpdateData.setOnClickListener(listenerBtnUpdateData);

        setFormValues();
        btnDiagnose.setOnClickListener(listenerBtnDiagnose);
    }


    //########## Init Functions ##########

    private void setDiagnosticsValues() {
        float estimatedProbability = (float) request.getEstimatedProbability();
        tvEstimatedProbability.setText(Math.round(estimatedProbability) + "%");
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

        if (request.getDiagnosedLabelIndex() != -1) {
            tvSpecialistDiagnostic.setText(getString(Classifier.LABELS[request.getDiagnosedLabelIndex()]));
        }

        // In theory this should work without the if, but to ensure...
        if (request.getPathologistDiagnosticLabelIndex() != -1) {
            spPathologistDiagnostic.setSelection(request.getPathologistDiagnosticLabelIndex());
        }
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


        if (request.getLocalizationIndex() != 0) {
            bodyParts.get(request.getLocalizationIndex()).performClick();
        }
    }

    private void setUpDiagnosticUI() {
        btnDiagnose.setVisibility(View.VISIBLE);
        npDiagnostics.setMinValue(0);
        npDiagnostics.setMaxValue(Classifier.LABELS.length);
        npDiagnostics.setDisplayedValues(getResources().getStringArray(R.array.labels_select));
        npDiagnostics.setVisibility(View.VISIBLE);
    }

    private boolean validateInput() {
        etPatientId.setError(null);
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


    /**
     * Search for a new request and opens it if there exist one
     * If not, it shows a message
     */
    private void goToNextRequest() {

        requestDetailViewModel.nextRequest.observe(RequestDetailActivity.this, requestResult -> {
            if (requestResult.getError() != null) {
                Objects.requireNonNull(CustomSnackbar.make(clContainer,
                        getString(requestResult.getError()),
                        Snackbar.LENGTH_SHORT,
                        null,
                        R.drawable.ic_error_outline,
                        null,
                        getColor(R.color.accent)
                )).show();
                requestDetailViewModel.nextRequest.removeObservers(RequestDetailActivity.this);
                return;
            }

            Intent intent = new Intent(RequestDetailActivity.this, RequestDetailActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(RequestDetailActivity.REQUEST, requestResult.getSuccess());
            startActivity(intent);
        });
        requestDetailViewModel.getNewRequest();
    }

    ;

    private final View.OnClickListener listenerBtnDiagnose = view -> {
        if (npDiagnostics.getValue() == 0) {
            Objects.requireNonNull(CustomSnackbar.make(clContainer,
                    getString(R.string.error_no_diagnostic),
                    Snackbar.LENGTH_SHORT,
                    null,
                    R.drawable.ic_error_outline,
                    null,
                    getColor(R.color.accent)
            )).show();
            return;
        }
        btnDiagnose.setEnabled(false);
        int generalMedicDiagnosticIndex = npDiagnostics.getValue() - 1;
        boolean success = request.getDiagnosedLabelIndex() == generalMedicDiagnosticIndex;
        requestDetailViewModel.diagnose(request,
                generalMedicDiagnosticIndex,
                result -> {
                    onRequestUpdated.onRequestUpdated(result);
                    setDiagnosticsValues();
                    requestDetailViewModel.persistUserData();
                    Objects.requireNonNull(CustomSnackbar.make(clContainer,
                            getString(R.string.specialist_diagnostic_with_security, Math.round(request.getDiagnosticSecurity() * 100)),
                            BaseTransientBottomBar.LENGTH_SHORT,
                            null,
                            success ? R.drawable.ic_check_circle_outline : R.drawable.ic_error_outline,
                            null,
                            success ? getColor(R.color.success) : getColor(R.color.accent)
                    )).addCallback(new BaseTransientBottomBar.BaseCallback<CustomSnackbar>() {
                        @Override
                        public void onDismissed(CustomSnackbar transientBottomBar, int event) {
                            goToNextRequest();
                        }
                    }).show();
                }
        );
    };


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(RequestDetailActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

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