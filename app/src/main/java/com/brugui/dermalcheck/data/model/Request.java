package com.brugui.dermalcheck.data.model;

import com.google.firebase.Timestamp;

public class Request {
    private double estimatedProbability;
    private boolean familiarAntecedents, personalAntecedents;
    private int phototype;
    private String notes;
    private LoggedInUser sender; //todo receiver
    private Status status;
    private Timestamp creationDate;

}
