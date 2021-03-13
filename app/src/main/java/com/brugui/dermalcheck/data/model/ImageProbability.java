package com.brugui.dermalcheck.data.model;

import android.net.Uri;

public class ImageProbability {
    private int label, labelIndex;
    private float estimatedProbability;
    private Uri imageUri;

    /**
     *
     * @param label int reference to the label on the strings.xml file
     * @param estimatedProbability
     * @param imageUri
     * @param labelIndex Index of the label on the possible labels array
     */
    public ImageProbability(int label, float estimatedProbability, Uri imageUri, int labelIndex) {
        this.label = label;
        this.estimatedProbability = estimatedProbability;
        this.imageUri = imageUri;
        this.labelIndex = labelIndex;
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

    public int getLabelIndex() {
        return labelIndex;
    }

    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }
}
