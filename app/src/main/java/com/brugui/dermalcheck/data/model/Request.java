package com.brugui.dermalcheck.data.model;

import android.os.Parcelable;

import com.google.firebase.Timestamp;
import java.util.Date;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private double estimatedProbability;
    private boolean familiarAntecedents, personalAntecedents;
    private int phototype;
    private String notes;
    private LoggedInUser sender; //todo receiver
    private Status status;
    private Date creationDate;

    public Request(double estimatedProbability, boolean familiarAntecedents, boolean personalAntecedents, int phototype, String notes, LoggedInUser sender, Status status, Date creationDate) {
        this.estimatedProbability = estimatedProbability;
        this.familiarAntecedents = familiarAntecedents;
        this.personalAntecedents = personalAntecedents;
        this.phototype = phototype;
        this.notes = notes;
        this.sender = sender;
        this.status = status;
        this.creationDate = creationDate;
    }
    
    public Map<String, Object> toMap(){
     Map<String, Object> map = new HashMap<>();
        map.put("estimatedProbability", this.estimatedProbability);
        map.put("familiarAntecedents", this.familiarAntecedents);
        map.put("personalAntecedents", this.personalAntecedents);
        map.put("phototype", this.phototype);
        map.put("notes", this.notes);
        map.put("sender", this.sender);
        map.put("status", this.status.getName());
        map.put("creationDate", this.creationDate);
        return map;
    }

    public double getEstimatedProbability() {
        return estimatedProbability;
    }

    public void setEstimatedProbability(double estimatedProbability) {
        this.estimatedProbability = estimatedProbability;
    }

    public boolean isFamiliarAntecedents() {
        return familiarAntecedents;
    }

    public void setFamiliarAntecedents(boolean familiarAntecedents) {
        this.familiarAntecedents = familiarAntecedents;
    }

    public boolean isPersonalAntecedents() {
        return personalAntecedents;
    }

    public void setPersonalAntecedents(boolean personalAntecedents) {
        this.personalAntecedents = personalAntecedents;
    }

    public int getPhototype() {
        return phototype;
    }

    public void setPhototype(int phototype) {
        this.phototype = phototype;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LoggedInUser getSender() {
        return sender;
    }

    public void setSender(LoggedInUser sender) {
        this.sender = sender;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
