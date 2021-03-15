package com.brugui.dermalcheck.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.model.ImageProbability;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;

public class Classifier {

    public static final int[] LABELS =
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

    public static final int[] BODY_PARTS = new int[]{
            R.string.unknown,
            R.string.back,
            R.string.lower_extremity,
            R.string.upper_extremity,
            R.string.abdomen,
            R.string.chest,
            R.string.scalp,
            R.string.face,
            R.string.ear,
            R.string.neck,
            R.string.hand,
            R.string.foot,
            R.string.genital,
    };

    /**
     * @param context  Context
     * @param imageUri Uri
     * @return float[] probabilities
     * @throws IOException Exception
     */
    private static float[] predict(Context context, Uri imageUri) throws IOException {
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
        tImage = imageProcessor.process(tImage);
        tImage.getTensorBuffer();

        TensorBuffer probabilityBuffer =
                TensorBuffer.createFixedSize(new int[]{1, 9}, DataType.FLOAT32);

        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, "model.tflite");


        Interpreter tflite = new Interpreter(tfliteModel);

        // Running inference
        tflite.run(tImage.getBuffer(), probabilityBuffer.getBuffer().rewind());
        tflite.close();

        return probabilityBuffer.getFloatArray();

    }

    public static ImageProbability getImageProbabilityPrediction(Context context, Uri imageUri) throws IOException {
        float[] probabilities = predict(context, imageUri);
        int largestIndex = 0;
        float largestValue = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > largestValue) {
                largestValue = probabilities[i];
                largestIndex = i;
            }
        }

        return new ImageProbability(LABELS[largestIndex],
                ((int) ((largestValue + 0.005f) * 10000)) / 100f,
                imageUri,
                largestIndex
        );
    }

}
