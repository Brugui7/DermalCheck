package com.brugui.dermalcheck.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser implements Serializable {

    private String uid;
    private String displayName, role, email, password, nick;
    private int matchingRequestsDiagnosed, requestsDiagnosed;

    public LoggedInUser(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public LoggedInUser(String uid, String email, String displayName, String role) {
        this.uid = uid;
        this.displayName = displayName;
        this.role = role;
    }

    public LoggedInUser() {
    }

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRequestsDiagnosed() {
        return requestsDiagnosed;
    }

    public void setRequestsDiagnosed(int requestsDiagnosed) {
        this.requestsDiagnosed = requestsDiagnosed;
    }

    public int getMatchingRequestsDiagnosed() {
        return matchingRequestsDiagnosed;
    }

    public void setMatchingRequestsDiagnosed(int matchingRequestsDiagnosed) {
        this.matchingRequestsDiagnosed = matchingRequestsDiagnosed;
    }

    @Override
    public String toString() {
        return "LoggedInUser{" +
                "uid='" + uid + '\'' +
                ", displayName='" + displayName + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("displayName", this.displayName);
        map.put("nick", this.nick);
        return map;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}