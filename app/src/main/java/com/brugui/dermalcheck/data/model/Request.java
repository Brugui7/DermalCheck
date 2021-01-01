package com.brugui.dermalcheck.data.model;

import android.os.Parcelable;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private String id;
    private double estimatedProbability;
    private boolean familiarAntecedents, personalAntecedents;
    private int phototype;
    private String notes, patientId, status;
    private String sender, receiver;
    private Date creationDate;

    public Request(double estimatedProbability, boolean familiarAntecedents, boolean personalAntecedents, int phototype, String notes, String patientId, String sender, String receiver, String status, Date creationDate, String id) {
        this.estimatedProbability = estimatedProbability;
        this.familiarAntecedents = familiarAntecedents;
        this.personalAntecedents = personalAntecedents;
        this.phototype = phototype;
        this.notes = notes;
        this.patientId = patientId;
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
        this.creationDate = creationDate;
    }

    public Request() {}

    public Map<String, Object> toMap(){
     Map<String, Object> map = new HashMap<>();
        map.put("estimatedProbability", this.estimatedProbability);
        map.put("familiarAntecedents", this.familiarAntecedents);
        map.put("personalAntecedents", this.personalAntecedents);
        map.put("phototype", this.phototype);
        map.put("notes", this.notes);
        map.put("sender", this.sender);
        map.put("receiver", this.receiver);
        map.put("status", this.status);
        map.put("creationDate", this.creationDate);
        map.put("id", this.id);
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getPatientId() {
        return patientId;
    }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Request{" +
                "estimatedProbability=" + estimatedProbability +
                ", familiarAntecedents=" + familiarAntecedents +
                ", personalAntecedents=" + personalAntecedents +
                ", phototype=" + phototype +
                ", notes='" + notes + '\'' +
                ", patientId='" + patientId + '\'' +
                ", sender=" + sender +
                ", receiver=" + receiver +
                ", status=" + status +
                ", creationDate=" + creationDate +
                '}';
    }



}
