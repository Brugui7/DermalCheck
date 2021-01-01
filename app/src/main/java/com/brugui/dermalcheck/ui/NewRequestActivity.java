package com.brugui.dermalcheck.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.ui.components.ImageDetailActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class NewRequestActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageSwitcher imageSwitcher;
    private FloatingActionButton fabAddImage;
    private ArrayList<Uri> images;
    private ImageButton ibtnPrevious, ibtnNext;
    private Button btnAnalyze;
    private int imageSwitcherPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_request);
        imageSwitcher = findViewById(R.id.imgsPreview);
        fabAddImage = findViewById(R.id.fabAddImage);
        fabAddImage.setOnClickListener(listenerFabAddImage);
        ibtnNext = findViewById(R.id.ibtnNext);
        ibtnPrevious = findViewById(R.id.ibtnPrevious);
        btnAnalyze = findViewById(R.id.btnAnalyze);

        images = new ArrayList<>();
        imageSwitcher.setFactory(() -> new ImageView(getApplicationContext()));
        ibtnPrevious.setOnClickListener(listenerIbtnPrevious);
        ibtnNext.setOnClickListener(listenerIbtnNext);
        btnAnalyze.setOnClickListener(listenerBtnAnalyze);
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
            if (data.getClipData() != null){
                //multiple imgs
                int imgCount = data.getClipData().getItemCount();
                for (int i = 0; i < imgCount; i++) {
                    images.add(data.getClipData().getItemAt(i).getUri());
                }
            } else {
                //single imgs
                images.add(data.getData());
            }
            imageSwitcher.setImageURI(images.get(0));
            imageSwitcherPosition = 0;
        }
    }


    //########## LISTENERS ##########

    private View.OnClickListener listenerFabAddImage = view -> {
      dispatchTakePictureIntent();
    };

    private View.OnClickListener listenerIbtnPrevious = view -> {
        if (imageSwitcherPosition > 0) {
            imageSwitcher.setImageURI(images.get(imageSwitcherPosition--));
        }
    };

    private View.OnClickListener listenerIbtnNext = view -> {
        if (imageSwitcherPosition < images.size() - 1) {
            imageSwitcher.setImageURI(images.get(imageSwitcherPosition++));
        }
    };

    private View.OnClickListener listenerBtnAnalyze = view -> {
      startActivity(new Intent(NewRequestActivity.this, ImageDetailActivity.class));
    };

}