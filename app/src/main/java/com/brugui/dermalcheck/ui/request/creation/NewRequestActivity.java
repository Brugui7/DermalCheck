package com.brugui.dermalcheck.ui.request.creation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.brugui.dermalcheck.BuildConfig;
import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.Result;
import com.brugui.dermalcheck.data.interfaces.OnRequestCreated;
import com.brugui.dermalcheck.data.model.ImageProbability;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
import com.brugui.dermalcheck.ui.MainActivity;
import com.brugui.dermalcheck.ui.adapters.ImageProbabilityAdapter;
import com.brugui.dermalcheck.ui.components.snackbar.CustomSnackbar;
import com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity;
import com.brugui.dermalcheck.utils.Classifier;
import com.brugui.dermalcheck.utils.Common;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

import static com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity.IMAGES_ARRAY;
import static com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity.REQUEST;

public class NewRequestActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICK = 2;
    private ImageProbability imageProbability;
    private Request newRequest;
    private EditText etPatientId;
    private Button btnSendRequest;
    private ImageView ivImage;
    private DonutProgressView dpvChart;
    private TextView tvEstimatedProbability, tvLabel;
    private LoggedInUser userLogged;
    private ConstraintLayout clContainer;
    private Uri currentPhotoUri;
    private static final String TAG = "Logger NewRequestAc";
    private NewRequestViewModel newRequestViewModel;
    private RatingBar rbSecurity;
    private Spinner spDiagnostics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);
        setTitle(R.string.new_request);
        FloatingActionButton fabAddImage = findViewById(R.id.fabAddImage);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        clContainer = findViewById(R.id.clContainer);
        dpvChart = findViewById(R.id.dpvChart);
        tvEstimatedProbability = findViewById(R.id.tvEstimatedProbability);
        tvLabel = findViewById(R.id.tvLabel);
        ivImage = findViewById(R.id.ivImage);
        etPatientId = findViewById(R.id.etPatientId);
        rbSecurity = findViewById(R.id.rbSecurity);
        spDiagnostics = findViewById(R.id.spDiagnostics);

        FirebaseUser userTmp = FirebaseAuth.getInstance().getCurrentUser();
        userLogged = new LoggedInUser(userTmp.getUid(), userTmp.getDisplayName());

        fabAddImage.setOnClickListener(listenerFabAddImage);
        btnSendRequest.setOnClickListener(listenerBtnSendRequest);
        newRequestViewModel = new NewRequestViewModel();
        newRequestViewModel.getRequestNumber().observe(this, requestNumber -> {
            etPatientId.setText(String.valueOf(requestNumber));
        });
        newRequestViewModel.fetchNewRequestNumber();
    }

    private void dispatchPictureIntent() {
        final CharSequence[] options = {
                getString(R.string.choose_images_from_camera),
                getString(R.string.choose_images_from_gallery),
                getString(R.string.cancel)
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(NewRequestActivity.this);
        builder.setTitle(R.string.select_new_image);
        builder.setItems(options, (dialog, item) -> {
            if (item == 0 && Common.hasCameraPermissions(NewRequestActivity.this)) {
                //Take photo
                dispatchTakePictureIntent();
            } else if (item == 1) {
                //pick image
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        Uri uri = null;
        if (requestCode == REQUEST_IMAGE_PICK) {
            if (data.getData() != null) {
                //single img
                uri = data.getData();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // Shows the thumbnail on ImageView
            uri = currentPhotoUri;

            // ScanFile so it will be appeared on Gallery
            MediaScannerConnection.scanFile(NewRequestActivity.this,
                    new String[]{currentPhotoUri.getPath()}, null, null);
        }

        if (uri != null){
            showPredictions(uri);
        }
    }

    /**
     * @param imageUri Uri image
     */
    private void showPredictions(Uri imageUri) {
        //TODO: load spinner
        new Thread(() -> {
            try {
                imageProbability = Classifier.getImageProbabilityPrediction(NewRequestActivity.this, imageUri);
                runOnUiThread(() -> {
                    ivImage.setImageURI(imageProbability.getImageUri());
                    tvLabel.setText(imageProbability.getLabel());
                    setChartValues(imageProbability);
                });
            } catch (IOException exception) {
                //todo show error snackbar
            }
        }).start();


    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensures that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Creates the File where the photo should go
            File photoFile = null;
            try {
                photoFile = Common.createNewImageFile(NewRequestActivity.this);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                currentPhotoUri = Uri.fromFile(photoFile);

                if (Build.VERSION.SDK_INT >= 24) {
                    currentPhotoUri = FileProvider.getUriForFile(this,
                            BuildConfig.APPLICATION_ID + ".provider",
                            photoFile);
                }

                if (currentPhotoUri != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    return;
                }

                Objects.requireNonNull(CustomSnackbar.make(clContainer,
                        getString(R.string.error_taking_photo),
                        BaseTransientBottomBar.LENGTH_SHORT,
                        null,
                        R.drawable.ic_error_outline,
                        null,
                        getColor(R.color.accent)
                )).show();

            }
        }
    }

    //########## LISTENERS ##########


    private final View.OnClickListener listenerFabAddImage = view -> {
        dispatchPictureIntent();
    };


    //TODO formstate y al viewmodel
    private boolean validateInput() {
        etPatientId.setError(null);

        if (imageProbability == null) {
            CustomSnackbar.Companion.make(clContainer, getString(R.string.error_no_images),
                    Snackbar.LENGTH_SHORT,
                    null,
                    R.drawable.ic_error_outline,
                    null,
                    getColor(R.color.accent)
            ).show();
            return false;
        }

        if (spDiagnostics.getSelectedItemPosition() == 0){
            CustomSnackbar.Companion.make(clContainer, getString(R.string.error_no_diagnostic),
                    Snackbar.LENGTH_SHORT,
                    null,
                    R.drawable.ic_error_outline,
                    null,
                    getColor(R.color.accent)
            ).show();
            return false;
        }

        if (etPatientId.getText().toString().trim().length() == 0) {
            etPatientId.setError(getString(R.string.required_field));
            etPatientId.requestFocus();
            return false;
        }

        return true;
    }

    /**
     *
     * @param image ImageProbability
     */
    private void setChartValues(ImageProbability image) {
        float estimatedProbability = image.getEstimatedProbability();
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
        /*NotificationRequestsQueue
                .getInstance(getApplicationContext())
                .addToRequestQueue(((Result.Success<JsonObjectRequest>) result).getData());*/


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
                        Intent intent = new Intent(NewRequestActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .show();

    };

    private final View.OnClickListener listenerBtnSendRequest = view -> {
        if (!validateInput()) {
            return;
        }

        newRequest = new Request(
                imageProbability.getEstimatedProbability(),
                -1,
                null,
                false,
                false,
                -1,
                null,
                etPatientId.getText().toString(),
                userLogged.getUid(),
                null, //automatically assigned before sending
                Status.PENDING_STATUS_NAME,
                Calendar.getInstance().getTime(),
                imageProbability.getLabelIndex()
        );

        newRequest.setDiagnosedLabelIndex(spDiagnostics.getSelectedItemPosition() - 1);
        newRequest.setDiagnosticSecurity(rbSecurity.getRating() / rbSecurity.getNumStars());
        newRequest.setDiagnosticDate(Calendar.getInstance().getTime());

        //Por si hay que poder pasar más de una foto en algún momento
        ArrayList<Uri> imagesTmp = new ArrayList<>();
        imagesTmp.add(imageProbability.getImageUri());
        btnSendRequest.setEnabled(false);
        newRequestViewModel.sendRequest(newRequest, imagesTmp, onRequestCreated);
    };
}