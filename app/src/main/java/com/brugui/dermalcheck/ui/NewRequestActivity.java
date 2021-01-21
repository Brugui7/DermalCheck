package com.brugui.dermalcheck.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.model.LoggedInUser;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
import com.brugui.dermalcheck.ui.components.ImageDetailActivity;
import com.brugui.dermalcheck.ui.components.snackbar.CustomSnackbar;
import com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ThreadLocalRandom;

import static com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity.IMAGES_ARRAY;
import static com.brugui.dermalcheck.ui.request.detail.RequestDetailActivity.REQUEST;

public class NewRequestActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageSwitcher imageSwitcher;
    private ArrayList<Uri> images;
    private int imageSwitcherPosition;
    private Request newRequest;
    private EditText etPhototype, etPatientId, etNotes;
    private LoggedInUser userLogged;
    private CheckBox chPersonalAntecedents, chFamiliarAntecedents;
    private ConstraintLayout clContainer;

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

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(Intent.createChooser(intent, "Selecciona imágenes"), REQUEST_IMAGE_CAPTURE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            images.clear();
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
            imageSwitcher.setImageURI(images.get(0));
            imageSwitcherPosition = 0;
        }
    }


    //########## LISTENERS ##########

    private final View.OnClickListener listenerFabAddImage = view -> {
        dispatchTakePictureIntent();
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

        if (!validateInput()){
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

        if (images.size() == 0){
            CustomSnackbar.Companion.make(clContainer, getString(R.string.error_no_images),
                    Snackbar.LENGTH_SHORT,
                    null,
                    R.drawable.ic_error_outline,
                    null,
                    getColor(R.color.accent)
            ).show();
            return false;
        }

        if (etPatientId.getText().toString().trim().length() == 0){
            etPatientId.setError(getString(R.string.required_field));
            etPatientId.requestFocus();
            return false;
        }

        String stringPhototype = etPhototype.getText().toString().trim();
        if (stringPhototype.length() == 0){
            etPhototype.setError(getString(R.string.required_field));
            etPhototype.requestFocus();
            return false;
        }

        try {
            int phototype = Integer.parseInt(stringPhototype);
            if (phototype < 1 || phototype > 7){
                etPhototype.setError(getString(R.string.invalid_phototype));
                etPhototype.requestFocus();
                return false;
            }
        } catch (NumberFormatException e){
            etPhototype.setError(getString(R.string.invalid_phototype));
            etPhototype.requestFocus();
            return false;
        }

        return true;
    }

}