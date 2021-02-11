package com.brugui.dermalcheck.ui.request.creation;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
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
import android.widget.RadioGroup;

import com.brugui.dermalcheck.BuildConfig;
import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.model.ImageProbability;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
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
import java.util.List;
import java.util.Objects;

import static com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity.IMAGES_ARRAY;
import static com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity.REQUEST;

public class NewRequestActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICK = 2;
    private ArrayList<Uri> images;
    private ArrayList<ImageProbability> imageProbabilities;
    private Request newRequest;
    private EditText etPhototype, etPatientId, etNotes, etAge;
    private LoggedInUser userLogged;
    private CheckBox chPersonalAntecedents, chFamiliarAntecedents;
    private ConstraintLayout clContainer;
    private Uri currentPhotoUri;
    private static final String TAG = "Logger NewRequestAc";
    private RecyclerView rvList;
    private RadioGroup rgSex;
    private ImageProbabilityAdapter adapter;
    private NewRequestViewModel newRequestViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);
        setTitle(R.string.new_request);
        FloatingActionButton fabAddImage = findViewById(R.id.fabAddImage);
        rvList = findViewById(R.id.rvList);
        Button btnAnalyze = findViewById(R.id.btnAnalyze);
        chFamiliarAntecedents = findViewById(R.id.chFamiliarAntecedents);
        chPersonalAntecedents = findViewById(R.id.chPersonalAntecedents);
        etPhototype = findViewById(R.id.etPhototype);
        etPatientId = findViewById(R.id.etPatientId);
        etAge = findViewById(R.id.etAge);
        etNotes = findViewById(R.id.etNotes);
        rgSex = findViewById(R.id.rgSex);
        clContainer = findViewById(R.id.clContainer);
        FirebaseUser userTmp = FirebaseAuth.getInstance().getCurrentUser();
        userLogged = new LoggedInUser(userTmp.getUid(), userTmp.getDisplayName());

        fabAddImage.setOnClickListener(listenerFabAddImage);
        images = new ArrayList<>();
        imageProbabilities = new ArrayList<>();
        rvList.setHasFixedSize(true);
        rvList.setItemAnimator(new DefaultItemAnimator());
        adapter = new ImageProbabilityAdapter(imageProbabilities, null);
        rvList.setAdapter(adapter);
        btnAnalyze.setOnClickListener(listenerBtnAnalyze);
        newRequestViewModel = new NewRequestViewModel();
    }

    private void dispatchPictureIntent() {
        final CharSequence[] options = {
                getString(R.string.choose_images_from_camera),
                getString(R.string.choose_images_from_gallery),
                getString(R.string.cancel)
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(NewRequestActivity.this);
        if (images.size() == 3) {
            builder.setTitle(R.string.select_new_images);
        } else {
            builder.setTitle(getString(R.string.remaining_images_select, 3 - images.size()));
        }

        builder.setItems(options, (dialog, item) -> {
            if (item == 0 && Common.hasCameraPermissions(NewRequestActivity.this)) {
                //Take photo
                dispatchTakePictureIntent();
            } else if (item == 1) {
                //pick image
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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
        List<Uri> images = new ArrayList<>();

        if (requestCode == REQUEST_IMAGE_PICK) {
            if (data.getClipData() != null) {
                //multiple imgs
                int imgCount = data.getClipData().getItemCount();
                for (int i = 0; i < imgCount; i++) {
                    images.add(data.getClipData().getItemAt(i).getUri());
                }
            } else {
                //single img
                images.add(data.getData());
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // Shows the thumbnail on ImageView
            images.add(currentPhotoUri);

            // ScanFile so it will be appeared on Gallery
            MediaScannerConnection.scanFile(NewRequestActivity.this,
                    new String[]{currentPhotoUri.getPath()}, null, null);
        }

        showPredictions(images);
    }

    /**
     *
     * @param images List<Uri> image uris
     */
    private void showPredictions(List<Uri> images) {
        //TODO: load spinner
        new Thread(() -> {
            try {
                for (Uri image : images) {
                    imageProbabilities.add(
                            Classifier.getImageProbabilityPrediction(NewRequestActivity.this, image)
                    );
                    if (imageProbabilities.size() > 3) {
                        imageProbabilities.remove(0);
                    }
                }
                runOnUiThread(() -> adapter.notifyDataSetChanged());
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

                if (currentPhotoUri != null){
                    //aquí
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

    private final View.OnClickListener listenerBtnAnalyze = view -> {

        if (!validateInput()) {
            return;
        }

        ImageProbability imageSelected = imageProbabilities.get(adapter.getPositionSelected());
        String sex = rgSex.getCheckedRadioButtonId() == R.id.rbMale ? "male" : "female";

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
                null, //automatically assigned before sending
                Status.PENDING_STATUS_NAME,
                Calendar.getInstance().getTime(),
                imageSelected.getLabel()
        );


        Intent intent = new Intent(NewRequestActivity.this, RequestDetailActivity.class);
        intent.putExtra(REQUEST, newRequest);
        //por si hay que poder pasar más de una foto en algún momento
        ArrayList<Uri> imagesTmp = new ArrayList<>();
        imagesTmp.add(imageSelected.getImageUri());
        intent.putExtra(IMAGES_ARRAY, imagesTmp);
        startActivity(intent);
    };


    //TODO formstate y al viewmodel
    private boolean validateInput() {
        etPatientId.setError(null);
        etPhototype.setError(null);
        etAge.setError(null);

        if (rgSex.getCheckedRadioButtonId() == -1) {
            CustomSnackbar.Companion.make(clContainer, getString(R.string.error_no_sex),
                    Snackbar.LENGTH_SHORT,
                    null,
                    R.drawable.ic_error_outline,
                    null,
                    getColor(R.color.accent)
            ).show();
            return false;
        }

        if (adapter.getPositionSelected() == -1) {
            CustomSnackbar.Companion.make(clContainer, getString(R.string.error_no_images),
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

        String stringPhototype = etPhototype.getText().toString().trim();
        if (stringPhototype.length() == 0) {
            etPhototype.setError(getString(R.string.required_field));
            etPhototype.requestFocus();
            return false;
        }

        String stringAge = etAge.getText().toString().trim();
        if (stringAge.length() == 0) {
            etAge.setError(getString(R.string.required_field));
            etAge.requestFocus();
            return false;
        }

        try {
            int phototype = Integer.parseInt(stringPhototype);
            if (phototype < 1 || phototype > 7) {
                etPhototype.setError(getString(R.string.invalid_phototype));
                etPhototype.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etPhototype.setError(getString(R.string.invalid_phototype));
            etPhototype.requestFocus();
            return false;
        }

        return true;
    }


}