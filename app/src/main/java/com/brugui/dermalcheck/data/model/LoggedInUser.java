package com.brugui.dermalcheck.data.model;

import java.io.Serializable;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser implements Serializable {

    private String userId;
    private String displayName, rol, email;

    public LoggedInUser(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public LoggedInUser(String userId, String email, String displayName, String rol) {
        this.userId = userId;
        this.displayName = displayName;
        this.rol = rol;
    }

    public LoggedInUser() {
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}