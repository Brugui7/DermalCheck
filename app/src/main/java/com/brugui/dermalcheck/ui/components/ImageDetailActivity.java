package com.brugui.dermalcheck.ui.components;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.widget.ImageView;

import com.brugui.dermalcheck.R;
import com.igreenwood.loupe.Loupe;

import org.jetbrains.annotations.NotNull;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

public class ImageDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        ImageView imageView = findViewById(R.id.ivImage);
        ConstraintLayout clContainer = findViewById(R.id.clContainer);
        Loupe loupe = Loupe.Companion.create(imageView, clContainer,  (Function1)(new Function1() {

            public Object invoke(Object var1) {
                this.invoke((Loupe)var1);
                return Unit.INSTANCE;
            }

            public final void invoke(@NotNull Loupe $this$create) {
                Intrinsics.checkNotNullParameter($this$create, "$receiver");
                $this$create.setOnViewTranslateListener((Loupe.OnViewTranslateListener)(new Loupe.OnViewTranslateListener() {
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
                }));
            }
        }));
        /*val loupe = Loupe.create(imageView, container) { // imageView is your ImageView
            onViewTranslateListener = object : Loupe.OnViewTranslateListener {

                override fun onStart(view: ImageView) {
                    // called when the view starts moving
                }

                override fun onViewTranslate(view: ImageView, amount: Float) {
                    // called whenever the view position changed
                }

                override fun onRestore(view: ImageView) {
                    // called when the view drag gesture ended
                }

                override fun onDismiss(view: ImageView) {
                    // called when the view drag gesture ended
                    finish()
                }
            }
        }*/
    }
}