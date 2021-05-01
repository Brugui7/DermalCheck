package com.brugui.dermalcheck.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {
    private static final String[] PERMISSIONS_CAMERA = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int REQUEST_CAMERA = 2;
    private static final String TAG = "Logger Common";


    /* PERMISSIONS STUFF */
    public static boolean hasCameraPermissions(Activity activity) {
        // Checks if the app has camera permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // The app doesn't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_CAMERA,
                    REQUEST_CAMERA
            );
            return false;
        }
        return true;
    }

    /**
     * Creates an image file on the pictures directory of the external storage
     *
     * @param context Context
     * @return File the file created
     * @throws IOException if there is an error creating the file
     */
    @NotNull
    public static File createNewImageFile(@NotNull Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d(TAG, "Storage dir: " + storageDir.getPath());
        if (!storageDir.exists()){
            storageDir.mkdir();
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

}
