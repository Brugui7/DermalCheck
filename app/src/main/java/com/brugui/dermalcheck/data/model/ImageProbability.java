package com.brugui.dermalcheck.data.model;

import android.net.Uri;

public class ImageProbability {
    private int label;
    private float estimatedProbability;
    private Uri imageUri;

    public ImageProbability(int label, float estimatedProbability, Uri imageUri) {
        this.label = label;
        this.estimatedProbability = estimatedProbability;
        this.imageUri = imageUri;
    }

    public ImageProbability() {
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public float getEstimatedProbability() {
        return estimatedProbability;
    }

    public void setEstimatedProbability(float estimatedProbability) {
        this.estimatedProbability = estimatedProbability;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}
