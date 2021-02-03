package com.brugui.dermalcheck.ui.components;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.brugui.dermalcheck.R;
import com.bumptech.glide.Glide;
import com.igreenwood.loupe.Loupe;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class ImageDetailActivity extends AppCompatActivity {

    public static final String IMAGE_URI = "IMAGE_URI";
    public static final String IMAGE_URL = "IMAGE_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        ImageView imageView = findViewById(R.id.ivImage);
        ConstraintLayout clContainer = findViewById(R.id.clContainer);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getParcelable(IMAGE_URI) != null){
                imageView.setImageURI(bundle.getParcelable(IMAGE_URI));
            } else {
                Glide.with(this).load(bundle.getString(IMAGE_URL)).into(imageView);
            }
        }

        Loupe loupe = Loupe.Companion.create(imageView, clContainer, new Function1() {

            public Object invoke(Object var1) {
                this.invoke((Loupe)var1);
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull Loupe $this$create) {
                Intrinsics.checkNotNullParameter($this$create, "$receiver");
                $this$create.setOnViewTranslateListener(new Loupe.OnViewTranslateListener() {
                    public void onStart(@NotNull ImageView view) {
                        Intrinsics.checkNotNullParameter(view, "view");
                    }

                    public void onViewTranslate(@NotNull ImageView view, float amount) {
                        Intrinsics.checkNotNullParameter(view, "view");
                    }

                    public void onRestore(@NotNull ImageView view) {
                        Intrinsics.checkNotNullParameter(view, "view");
                    }

                    public void onDismiss(@NotNull ImageView view) {
                        Intrinsics.checkNotNullParameter(view, "view");
                        ImageDetailActivity.this.finish();
                    }
                });
            }
        });
    }
}