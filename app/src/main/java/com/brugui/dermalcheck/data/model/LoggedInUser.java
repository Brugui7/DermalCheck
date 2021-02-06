package com.brugui.dermalcheck.data.model;

import java.io.Serializable;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser implements Serializable {

    private String userId;
    private String displayName, role, email;

    public LoggedInUser(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public LoggedInUser(String userId, String email, String displayName, String role) {
        this.userId = userId;
        this.displayName = displayName;
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}