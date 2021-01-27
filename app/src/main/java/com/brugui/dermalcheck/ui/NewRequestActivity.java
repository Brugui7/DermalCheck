package com.brugui.dermalcheck.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import com.brugui.dermalcheck.BuildConfig;
import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
import com.brugui.dermalcheck.ui.components.ImageDetailActivity;
import com.brugui.dermalcheck.ui.components.snackbar.CustomSnackbar;
import com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity;
import com.brugui.dermalcheck.utils.Common;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity.IMAGES_ARRAY;
import static com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity.REQUEST;

public class NewRequestActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_IMAGE_PICK = 2;
    private ImageSwitcher imageSwitcher;
    private ArrayList<Uri> images;
    private int imageSwitcherPosition;
    private Request newRequest;
    private EditText etPhototype, etPatientId, etNotes;
    private LoggedInUser userLogged;
    private CheckBox chPersonalAntecedents, chFamiliarAntecedents;
    private ConstraintLayout clContainer;
    private Uri currentPhotoUri;
    private static final String TAG = "Logger NewRequestAc";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);
        setTitle(R.string.new_request);
        imageSwitcher = findViewById(R.id.imgsPreview);
        FloatingActionButton fabAddImage = findViewById(R.id.fabAddImage);
        ImageButton ibtnNext = findViewById(R.id.ibtnNext);
        ImageButton ibtnPrevious = findViewById(R.id.ibtnPrevious);
        Button btnAnalyze = findViewById(R.id.btnAnalyze);
        chFamiliarAntecedents = findViewById(R.id.chFamiliarAntecedents);
        chPersonalAntecedents = findViewById(R.id.chPersonalAntecedents);
        etPhototype = findViewById(R.id.etPhototype);
        etPatientId = findViewById(R.id.etPatientId);
        etNotes = findViewById(R.id.etNotes);
        clContainer = findViewById(R.id.clContainer);
        FirebaseUser userTmp = FirebaseAuth.getInstance().getCurrentUser();
        userLogged = new LoggedInUser(userTmp.getUid(), userTmp.getDisplayName());

        fabAddImage.setOnClickListener(listenerFabAddImage);
        images = new ArrayList<>();
        imageSwitcher.setFactory(() -> new ImageView(getApplicationContext()));
        ibtnPrevious.setOnClickListener(listenerIbtnPrevious);
        ibtnNext.setOnClickListener(listenerIbtnNext);
        btnAnalyze.setOnClickListener(listenerBtnAnalyze);
        imageSwitcher.setOnClickListener(listenerImgSwitcher);
    }

    private void dispatchPictureIntent() {
        final CharSequence[] options = {
                getString(R.string.choose_images_from_camera),
                getString(R.string.choose_images_from_gallery),
                getString(R.string.cancel)
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(NewRequestActivity.this);
        if (images.size() == 3){
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
        Log.d(TAG, "Result: " + resultCode);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_IMAGE_PICK) {
            if (data.getClipData() != null) {
                //multiple imgs
                int imgCount = data.getClipData().getItemCount();
                for (int i = 0; i < imgCount; i++) {
                    images.add(data.getClipData().getItemAt(i).getUri());
                    if (images.size() > 3){
                        images.remove(0);
                    }
                }
                return;
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
                    new String[]{currentPhotoUri.getPath()}, null,
                    (path, uri) -> {
                    });
        }

        if (images.size() > 3){
            images.remove(0);
        }
        imageSwitcher.setImageURI(images.get(0));
        imageSwitcherPosition = 0;
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensures that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Creates the File where the photo should go
            File photoFile = null;
            try {
                photoFile = Common.createNewImageFile(NewRequestActivity.this);
            } catch (IOException ex) {
                //TODO error
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    //########## LISTENERS ##########


    private final View.OnClickListener listenerFabAddImage = view -> {
        dispatchPictureIntent();
    };

    private final View.OnClickListener listenerIbtnPrevious = view -> {
        if (imageSwitcherPosition > 0) {
            imageSwitcher.setImageURI(images.get(--imageSwitcherPosition));
        }
    };

    private final View.OnClickListener listenerIbtnNext = view -> {
        if (imageSwitcherPosition < images.size() - 1) {
            imageSwitcher.setImageURI(images.get(++imageSwitcherPosition));
        }
    };

    private final View.OnClickListener listenerImgSwitcher = view -> {
        if (images.size() == 0) {
            return;
        }
        Intent intent = new Intent(NewRequestActivity.this, ImageDetailActivity.class);
        intent.putExtra(ImageDetailActivity.IMAGE_URI, images.get(imageSwitcherPosition));
        startActivity(intent);
    };

    private final View.OnClickListener listenerBtnAnalyze = view -> {

        if (!validateInput()) {
            return;
        }

        newRequest = new Request(
                Math.round(ThreadLocalRandom.current().nextDouble(0, 100) * 100.0) / 100.0,
                chFamiliarAntecedents.isChecked(),
                chPersonalAntecedents.isChecked(),
                Integer.parseInt(etPhototype.getText().toString()), //TODO campo obligatorio
                etNotes.getText().toString(),
                etPatientId.getText().toString(),
                userLogged.getUserId(),
                userLogged.getUserId(), //TODO receiver
                Status.PENDING_STATUS_NAME,
                Calendar.getInstance().getTime(),
                null
        );


        Intent intent = new Intent(NewRequestActivity.this, RequestDetailActivity.class);
        intent.putExtra(REQUEST, newRequest);
        intent.putExtra(IMAGES_ARRAY, images);
        startActivity(intent);
    };

    private boolean validateInput() {
        etPatientId.setError(null);
        etPhototype.setError(null);

        if (images.size() == 0) {
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