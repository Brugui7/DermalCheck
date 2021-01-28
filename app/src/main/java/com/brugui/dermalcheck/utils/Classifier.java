package com.brugui.dermalcheck.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.ui.NewRequestActivity;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Classifier {

    public static final int[] labels =
            new int[]{
                    R.string.label1,
                    R.string.label2,
                    R.string.label3,
                    R.string.label4,
                    R.string.label5,
                    R.string.label6,
                    R.string.label7,
                    R.string.label8,
                    R.string.label9,
            };

    /**
     * @param context
     * @param imageUri
     * @return
     * @throws IOException
     */
    private float[] predict(Context context, Uri imageUri) throws IOException {
        Bitmap bitmap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.getContentResolver(), imageUri));
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        } else {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
        }

        // Create an ImageProcessor with all ops required.
        ImageProcessor imageProcessor =
                new ImageProcessor.Builder()
                        .add(new ResizeOp(224,
                                224,
                                ResizeOp.ResizeMethod.BILINEAR
                        ))
                        .build();

        // Creates a TensorImage object. This creates the tensor of the corresponding
        // tensor type (float32 in this case) that the TensorFlow Lite interpreter needs.
        TensorImage tImage = new TensorImage(DataType.FLOAT32);

        // Preprocess the image
        tImage.load(bitmap);
        tImage.getTensorBuffer();
        tImage = imageProcessor.process(tImage);

        TensorBuffer probabilityBuffer =
                TensorBuffer.createFixedSize(new int[]{1, 9}, DataType.FLOAT32);

        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, "model.tflite");


        Interpreter tflite = new Interpreter(tfliteModel);

        // Running inference
        tflite.run(tImage.getBuffer(), probabilityBuffer.getBuffer().rewind());
        tflite.close();

        return probabilityBuffer.getFloatArray();

    }

}
